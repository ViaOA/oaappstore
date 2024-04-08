<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oadebug.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }

    String forwardPage = null;
    formId = request.getParameter("oaform");
    if (formId != null) form = oasession.getForm(formId);
    
    if (form == null) {
        form = oasession.getCurrentForm();
        if (form != null) formId = form.getId(); 
    }
    
    if (form != null) {
        forwardPage = form.getUrl();
        form.setDebug(!form.getDebug());

        if (false || form.getCalcDebug()) {
            System.out.println("JSP oadebug.jsp, formId="+formId+", debug=" + form.getDebug() + ", forward="+forwardPage);
        }
        
        if (forwardPage == null) forwardPage = formId;
        response.sendRedirect(forwardPage);
    }
    else {
        System.out.println("JSP oadebug.jsp error, form "+formId+" not found for user session, will forward to login.jsp");
        response.sendRedirect("login.jsp");
    }
%>
