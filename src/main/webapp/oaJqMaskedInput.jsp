<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

formId = "oaJqMaskedInput";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    
    OAJqMaskedInput comp = new OAJqMaskedInput("txt", hub, AppUser.P_CurrentTime) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
           processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };
    
    // comp.setValue(""); 
    comp.setInputMask(OAJqMaskedInput.MaskInput_TimeHMS);
    comp.setLabelId("lbl");
    comp.setToolTip("OAJqMaskedInput is "+ comp.getInputMask());
    // comp.setDebug(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp); 
}
%>

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, OAJqMaskedInput comp, int cnt) {
    System.out.println((++cnt)+") onSubmitCompleted, value=" + comp.getValue() );
    OAForm form = formSubmitEvent.getForm();
    
    // allow using Pattern to change the inputMask
    String s = comp.getPattern();
    System.out.println("txtPattern ===========> "+s);//qqqqqqqq            
    if (OAStr.isNotEmpty(s)) {
        comp.setInputMask(s);
        comp.setPattern(null);
        form.getInputText("txtPattern", false).setValue(null);                
        comp.setToolTip("MaskedInput is "+ s);
        form.getInputText("txtToolTipText", false).setValue(comp.getToolTip());                
    }
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
  <legend>Demo OAJqMaskedInput</legend>
  <label id="lbl">Test OAJqMaskedInput <input id="txt" type="Text"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


