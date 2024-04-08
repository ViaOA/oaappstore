<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@page language="java"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="java.io.*, java.util.*"%> 


<%
    request.setCharacterEncoding("UTF-8");

    response.setHeader("Pragma:", "no-cache");
    response.setHeader("Expires:", "Thu, 16 Dec 1999 16:00:00 GMT");


    Enumeration enumx;

    System.out.println("---------------- START -------------");
    System.out.println("realPath ="+application.getRealPath("TEST"));
    System.out.println("servletPath ="+request.getServletPath());
    System.out.println("pathInfo ="+request.getPathInfo());
    System.out.println("pathTranslated ="+request.getPathTranslated());
    System.out.println("requestURI ="+request.getRequestURI());
    System.out.println("serverName ="+request.getServerName());
    System.out.println("serverPort ="+request.getServerPort());
    
    enumx = request.getParameterNames();

    String msg = "";
    
    while ( enumx.hasMoreElements()) {
        String name = (String) enumx.nextElement();
        String[] values = request.getParameterValues(name);
        msg += "<br>Z>"+name + "=" + request.getParameter(name);
        msg += "<br>X>"+name + "=" + ((values.length==0)?"Nil":values[0]);

        System.out.println( "--> name=" + name + "  value=" + ((values.length==0)?"":values[0]) );
    }

    msg += "<br>A>"+request.getAttribute("command"); // null
    msg += "<br>B>"+request.getParameter("command");
%>

<%= msg %>




