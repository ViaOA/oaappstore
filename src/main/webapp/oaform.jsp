<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
//qqqqqqqqqqqqq
    if (true || oasession.getCalcDebug()) {
        System.out.println("JSP oaform.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }

    String forwardPage = null;
    formId = request.getParameter("oaform");
    if (OAString.isNotEmpty(formId)) form = oasession.getRequestForm(formId);
    if (form != null) {
        forwardPage = form.processSubmit(oasession, request, response, false);
        if (forwardPage == null) forwardPage = formId;
        if (false || form.getCalcDebug()) {
            System.out.println("JSP oaform.jsp, formId="+formId+", forward="+forwardPage);
        }
        
        response.sendRedirect(forwardPage);
    }
    else {
    	System.out.println("JSP oaform.jsp error, form "+formId+" not found for user session, will forward to login.jsp");
        response.sendRedirect("login.jsp");
    }
%>
