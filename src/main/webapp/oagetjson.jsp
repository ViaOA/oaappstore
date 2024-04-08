<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 

<%
/**
    Used by components to forward jquery ajax to get JSON reponse from a single component on an OAForm.
*/
    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oagetjson.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }

    String json = null;
    formId = request.getParameter("oaform");
    if (formId != null) form = oasession.getForm(formId);
    if (form != null) {
        json = form.processGetJson(oasession);
    }
    if (json == null) json = "";
%>

<%= json %>
