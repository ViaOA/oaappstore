// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.webservice.client.hello.wsimport;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.templaze.webservice.client.hello.wsimport package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetHello_QNAME = new QName("http://server.webservice.templaze.com/", "getHello");
    private final static QName _GetHelloResponse_QNAME = new QName("http://server.webservice.templaze.com/", "getHelloResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.templaze.webservice.client.hello.wsimport
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetHello }
     * 
     */
    public GetHello createGetHello() {
        return new GetHello();
    }

    /**
     * Create an instance of {@link GetHelloResponse }
     * 
     */
    public GetHelloResponse createGetHelloResponse() {
        return new GetHelloResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHello }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webservice.templaze.com/", name = "getHello")
    public JAXBElement<GetHello> createGetHello(GetHello value) {
        return new JAXBElement<GetHello>(_GetHello_QNAME, GetHello.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHelloResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webservice.templaze.com/", name = "getHelloResponse")
    public JAXBElement<GetHelloResponse> createGetHelloResponse(GetHelloResponse value) {
        return new JAXBElement<GetHelloResponse>(_GetHelloResponse_QNAME, GetHelloResponse.class, null, value);
    }

}
