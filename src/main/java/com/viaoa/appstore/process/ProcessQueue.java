// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;


/**
 * Uses a single thread to process objects put in a queue.
 * @author vvia
 */
public abstract class ProcessQueue {
    private static Logger LOG = Logger.getLogger(ProcessQueue.class.getName());

    private LinkedBlockingQueue<OAObject> queue = new LinkedBlockingQueue<OAObject>();
    private final AtomicInteger aiStartStop = new AtomicInteger(0);
    private final AtomicInteger aiThreadId = new AtomicInteger(0);
    private Thread thread;
    
    public void queue(OAObject obj) {
        queue.offer(obj);
    }

    protected abstract void process(OAObject obj) throws Exception;
    
    public void start() {
        aiStartStop.incrementAndGet();

        LOG.fine("start called, aiStartStop=" + aiStartStop);
        thread = new Thread() {
            @Override
            public void run() {
                processQueue();
            }
        };
        thread.setName("ProcessQueue." + aiThreadId.incrementAndGet());
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        aiStartStop.incrementAndGet();
        LOG.fine("stop called, aiStartStop=" + aiStartStop);
        // que.offer(new OAObject());
    }
    
    
    protected void processQueue() {
        final int cntStartStop = aiStartStop.get();
        LOG.fine("created queue processor, cntStartStop=" + cntStartStop + ", thread name=" + Thread.currentThread().getName());
        for (;;) {
            try {
                boolean bDone = (cntStartStop != aiStartStop.get());
                if (bDone) break;
                
                OAObject obj = queue.poll(1, TimeUnit.MINUTES);
                if (obj == null) {
                    continue;
                }
                
                process(obj);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error processing from queue", e);
            }
        }
        LOG.fine("stopped queue processor, cntStartStop=" + cntStartStop + ", thread name=" + Thread.currentThread().getName());
    }
}
