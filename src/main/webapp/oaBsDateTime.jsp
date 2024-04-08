<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaBsDateTime";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    if (hub.getAO() == null) hub.setPos(0);

    OABsDateTime comp = new OABsDateTime("txtdt", hub, AppUser.P_Now) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };

    // comp.setValue(""); 
    comp.setLabelId("lbl");
    // comp.setDebug(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);
}
%>

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, BsDateTime comp, int cnt) {
    System.out.println((++cnt) + ") onSubmitCompleted, value=" + comp.getValue());
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
<a id="cmdOAReset" href="oareset.jsp">oareset</a>
&nbsp;&nbsp;&nbsp;
<input type=submit value="submit">
<input id="cmdDoAjaxSubmit" type=button value="xxx">
debug
<input id="chkFormDebug" type="checkbox">
<fieldset>
  <legend>Demo OABsDateTime</legend>


  <label id="lbl" style="position: relative">Test OABsDateTime <input id="txtdt" type="Text"></label> <br>

  <p>
    <br>
    <br>
    <%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>
<%@ include file="include/htmlFooter.jspf"%>


