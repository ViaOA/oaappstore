<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaHtmlMultiSelect";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    Hub<AppUser> hubSelect = new Hub<>();
    
    OAHtmlMultiSelect comp = new OAHtmlMultiSelect("sel", hub, hubSelect, AppUser.P_FullName) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted"); 
        }
    };
    comp.setLabelId("lbl");
    comp.setDisplayRows(8);
    comp.setDebug(true);
    // comp.setAjaxSubmit(true);
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
  <legend>Demo OAHtmlMultiSelect</legend>
  <label id="lbl">Test OAHtmlMultiSelect <select id="sel"></select></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


