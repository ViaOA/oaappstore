<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "htmlScrollPanel";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    HtmlScrollPanel comp = new HtmlScrollPanel("pan") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
    System.out.println((++cnt)+") onSubmitCompleted"); 
        }
    };
    comp.setDebug(true);
    
    comp.setOverflow(OAHtmlComponent.OverflowType.Auto);
    comp.setHeight("150px");
    comp.setWidth("150px");
    form.add(comp);

    
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);
}
%>


<%@ include file="include/htmlHeader.jspf"%>
<style>
body {
  margin-left: 15px;
}

#div {
  border: solid thin;
}
</style>

<span id="cmdReloadMessage">Page reload is needed</span> 
<a id="cmdOAReset" href="oareset.jsp">oareset</a> &nbsp;&nbsp;&nbsp;
<input type=submit value="submit">
<input id="cmdDoAjaxSubmit" type=button value="xxx">
debug <input id="chkFormDebug" type="checkbox">

<fieldset>
  <legend>Demo HtmlScrollPanel</legend>
  <div id="pan">
  <%=OAString.getDummyText(400, 200, 580) %>
  </div>
   <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


