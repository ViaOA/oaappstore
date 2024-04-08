<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@page trimDirectiveWhitespaces="true"%>
<%@page language="java" isErrorPage="true"%>
<%@page import="java.io.*, java.util.*, java.awt.*, java.util.logging.*"%>

<%!static Logger LOG = Logger.getLogger("oajsp");%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>System Error</title>
<link href="css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>

<div class="center-container">
<p class="big">Oops!</p>
<p class="xx-large">System error on this page.  Tech Support is currently being notified ... Please try again later ...</p>
<p><a href="index.jsp" class="button-cta-blue"><strong>Return to the Home Page</strong></a></p>
<p class="x-large">or</p>
<p><a href="contact-us.jsp" class="button"><strong>CONTACT US</strong></a></p>
</div>

    <PRE style="color: orange;">
        <% 
        
        exception.printStackTrace(new java.io.PrintWriter(out)); 
        LOG.log(Level.WARNING, "JSP Error", exception);        
        
        %>
    </PRE>




</body>
</html>

