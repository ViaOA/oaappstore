<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
//qqqqqqqqqqqqq
    if (true || oasession.getCalcDebug()) {
        System.out.println("JSP oaajax.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }
    
    formId = request.getParameter("oaform");
    if (formId != null) form = oasession.getForm(formId);
    if (form != null) {
        String forwardPage = form.processSubmit(oasession, request, response, true);
        if (forwardPage != null && !forwardPage.equals(form.getUrl())) {
            form.addScript("window.location = '"+forwardPage+"';");
        }
        String js = form.getAjaxScript();
//qqqqqqqqqqqqqqqq        
        if (true || form.getCalcDebug()) {
            System.out.println("JSP oaajax.jsp, returning js="+js);
        }
        out.print(js);
    }
    else {
        System.out.println("JSP oaajax.jsp error, form "+formId+" not found for user session, will forward to login.jsp");
        out.println("window.location = 'login.jsp';");        
    }
%>

