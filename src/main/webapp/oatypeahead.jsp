<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspImport.jspf"%> 
<%
/**


qqqqqqqqqqqqqqqqqqqqqq replaced with oagetjson.jsp qqqqqqqqqqqqqqqqqqqqqqqqqqqqq


    Used by OATextField.autoComplete to forward jquery ajax to OATextField.getAutoCompleteText
*/
    response.setContentType("application/json");

    if (false || oasession.getCalcDebug()) {
        System.out.println("JSP oatypeahead.jsp");
        OAWebUtil.debug(application, oasession, request, response);
    }
    
    String forwardPage = null;
    String id = request.getParameter("oaform");
    if (id != null) form = oasession.getForm(id);
    String values = "";
    
    if (form != null) {
        id = request.getParameter("id");
        OATextField txt = form.getTextField(id);
        if (txt != null) {
            String value = request.getParameter("term");
            values = txt.getTypeAheadJson(value);
            if (values == null) values = "";
        }
        if (false || form.getCalcDebug()) {
            System.out.println("JSP oatypeahead.jsp, returning values="+values+", txt="+txt);
        }
    }
    out.print("["+values+"]");
%>
