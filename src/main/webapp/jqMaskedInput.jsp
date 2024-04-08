<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

formId = "jqMaskedInput";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    JqMaskedInput comp = new JqMaskedInput("txtMit") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
    processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };
    
    // comp.setValue(""); 
    comp.setInputMask(JqMaskedInput.MaskInput_TimeHMS);
    comp.setLabelId("lbl");
    comp.setToolTip("JqMaskedInput is "+ comp.getInputMask());
    // comp.setDebug(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp); 
}
%>

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, JqMaskedInput comp, int cnt) {
    System.out.println((++cnt)+") onSubmitCompleted, value=" + comp.getValue() );
    OAForm form = formSubmitEvent.getForm();
    
    // allow using Pattern to change the inputMask
    String s = comp.getPattern();
    System.out.println("txtPattern ===========> "+s);//qqqqqqqq
    /*qq
    if (OAStr.isNotEmpty(s)) {
        comp.setInputMask(s);
        comp.setPattern(null);
        form.getComponent("txtPattern").setValue(null);                
        comp.setToolTip("MaskedInput is "+ s);
        form.getComponent("txtToolTipText").setValue(comp.getToolTip());                
    }
    */
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
  <legend>Demo JqMaskedInput</legend>
  <label id="lbl">Test JqMaskedInput <input id="txtMit" type="Text"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


