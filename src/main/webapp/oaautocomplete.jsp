<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 

<%
/**

qqqqqqqqqqqqqqqqqqqqqq replaced with oagetjson.jsp qqqqqqqqqqqqqqqqqqqqqqqqqqqqq

    Used by JqAutocompleteText to forward jquery ajax to
    getAutoCompleteText(String termToLookup "term");
*/
    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oaautocomplete.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }

    formId = request.getParameter("oaform");
    if (formId != null) form = oasession.getForm(formId);
    if (form != null) {
        form.processClientRequest(oasession);
    }
%>

