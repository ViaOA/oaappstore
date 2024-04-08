// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.delegate;

import com.viaoa.appstore.control.LogController;
import com.viaoa.util.OADateTime;

public class LogDelegate {
    
    private static LogController logController;
    public static LogController getLogController() {
        return logController;
    }
    public static void setLogController(LogController lc) {
        logController = lc;
    }
    
    public static void fastLog(String msg) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(msg);
    }

    public static void fastLog(String msg, Exception ex) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(msg, ex);
    }

    public static void fastLog(OADateTime dt, String msg, Exception ex) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(dt, msg, ex);
    }
    public static void fastLog(OADateTime dt, String msg) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(dt, msg);
    }

    
    public static void fastLog(long ts, String msg, Exception ex) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(ts, msg, ex);
    }
    public static void fastLog(long ts, String msg) {
        if (logController == null) {
            throw new RuntimeException("logController has not been assigned");
        }
        logController.fastLog(ts, msg);
    }
}
