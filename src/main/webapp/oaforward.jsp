<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oaforward.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }

    String forwardPage = null;
    formId = request.getParameter("oaform");
    if (formId != null) form = oasession.getForm(formId);
    if (form != null) {
        forwardPage = form.processForward(oasession, request, response);
        if (forwardPage == null) forwardPage = formId;
        if (false || form.getCalcDebug()) {
            System.out.println("JSP oaforward.jsp, formId="+formId+", forward="+forwardPage);
        }
        response.sendRedirect(forwardPage);
    }
    else {
        System.out.println("JSP oaforward.jsp error, form "+formId+" not found for user session");
        response.sendRedirect("/");
    }
%>
