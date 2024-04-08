// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.webservice.server;

import java.rmi.Remote;

public interface HelloInterface extends Remote {
    public String getHello(String name);
}
