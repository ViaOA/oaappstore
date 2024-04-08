<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

formId = "oaHtmlElement";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    if (hub.getAO() == null) hub.setPos(0);
    
    OAHtmlElement comp = new OAHtmlElement("ele", hub, AppUser.P_LastName) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
    eleOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };
    // comp.setValue(""); 
    // comp.setLabelId("lbl");
    comp.setDebug(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);
}
%>

<%!public void eleOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, OAHtmlElement comp, int cnt) {
    System.out.println((++cnt)+") onSubmitCompleted");
    OAForm form = formSubmitEvent.getForm();
}%>
    



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
  <legend>Demo OAHtmlElement</legend>
  <label id="lbl">Test OAHtmlElement <span id="ele">this should be replaced</span></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


