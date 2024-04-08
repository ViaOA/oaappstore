<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaBsTypeAhead";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUserLogin> hubAppUserLogin = new Hub(AppUserLogin.class);
    hubAppUserLogin.select();

    OAHtmlSelect compx = new OAHtmlSelect("selx", hubAppUserLogin, AppUserLogin.P_HostName);
    compx.setAjaxSubmit(true);
    form.add(compx);

    
    
    OATypeAhead.OATypeAheadParams tp = new OATypeAhead.OATypeAheadParams();
    tp.finderPropertyPath = "";
    tp.matchPropertyPath = AppUser.P_FullName;
    tp.displayPropertyPath = AppUser.P_FullName;
    tp.dropDownDisplayPropertyPath = AppUser.P_FullName;
    tp.minInputLength = 1;
    tp.maxResults = 35;
    tp.showHint = true;

    // hub to use searches
    OATypeAhead typeAhead = new OATypeAhead(ModelDelegate.getAppUsers(), tp);

    OABsTypeAhead comp = new OABsTypeAhead("txtta", hubAppUserLogin, AppUserLogin.P_AppUser, typeAhead) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };

    // comp.setValue(""); 
    comp.setLabelId("lbl");
    // comp.setDebug(true);
    form.add(comp);

    // HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp); 
}
%>

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, BsTypeAhead comp, int cnt) {
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
  <legend>Demo OABsTypeAhead</legend>
  <d>demo: to show using typeahead change Hub.AO.linkProperty</d><p>

  <label id="lbl">Select AppUserLogin <select id="selx"></select></label> <br> <br> <label id="lbl">Test OABsTypeAhead <input id="txtta" type="Text"></label>
  <br>

  <p>
    <br>
    <br>
    <%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


