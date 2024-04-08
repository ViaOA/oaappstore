// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.webservice.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * 
 * Must use an interface that extends Remote
 * 
 * Example;
 * Hello wsHello = new Hello();
 * jc.addWebservice("http://localhost:8081/ws/hello", wsHello);
 *
 * To view wsdl, use browser:
 *      http://localhost:8081/ws/hello?WSDL
 *
 *
 *  HelloClient.java is the sample client
 */
@WebService
public class Hello implements HelloInterface {

    @WebMethod()
    @Override
    public String getHello(String name) {
        return "Hello " + name;
    }
    
}
