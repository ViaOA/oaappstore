// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.viaoa.appstore.control.StartupController;
import com.viaoa.appstore.control.server.ServerController;
import com.viaoa.appstore.control.webserver.WebserverController;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OAString;


/**
 * Can be used by servlet engine (tomcat) to start OA as a server or client.
 * @author vvia
 */
public class StartupServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(StartupServlet.class.getName());
    
    private static final long serialVersionUID = 1L;
    private static StartupServlet startupServlet;

    private StartupController controlStartup;
    
    public StartupServlet() {
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        if (controlStartup != null) return;

        if (startupServlet != null) return;
        startupServlet = this;
        
        LOG.fine("Starting core server");

        int runType = Resource.RUNTYPE_Service;
        String s = getValue("runType", config);
        if (s != null) {
            for (int i = 0; i < Resource.STARTUP_TYPES.length; i++) {
                if (s.equalsIgnoreCase(Resource.STARTUP_TYPES[i])) {
                    runType = i;
                    break;
                }
            }
        }

        ArrayList<String> alArgs = new ArrayList<>(); 
        Enumeration<String> enumx = config.getInitParameterNames();
        for ( ; enumx.hasMoreElements(); ) {
            String name = enumx.nextElement();
            if (OAString.isEmpty(name)) continue;
            String val = config.getInitParameter(name);
            alArgs.add(name+"="+val);
        }        
        
        String[] args = new String[alArgs.size()];
        alArgs.toArray(args);
        controlStartup = new StartupController(runType, args);
    }
    
    public static String getValue(String name, ServletConfig config) {
        Enumeration<String> enumx = config.getInitParameterNames();
        for ( ; enumx.hasMoreElements(); ) {
            String s = enumx.nextElement();
            if (name.equals(s)) {
                name = s;
                break;
            }
        }
        return config.getInitParameter(name);
    }
    
    // make sure that server is shutdown properly
    @Override
    public void destroy() {
        super.destroy();
        if (controlStartup == null) return;
        
        LOG.fine("Stopping core server");
        
        try {
            ServerController sc = controlStartup.getServerController();
            if (sc != null) sc.close();
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception while stopping core server", e);
        }
    }
}
