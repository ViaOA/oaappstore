<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oaenable.jsp");
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
        form.setEnabled(!form.getEnabled());
        if (forwardPage == null) forwardPage = formId;
        if (false || form.getCalcDebug()) {
            System.out.println("JSP oaenable.jsp, formId="+formId+", enabled=" + form.getEnabled() + ", forward="+forwardPage);
        }
        response.sendRedirect(forwardPage);
    }
    else {
        System.out.println("JSP oaenable.jsp error, form "+formId+" not found for user session, will forward to login.jsp");
        response.sendRedirect("login.jsp");
    }
%>
