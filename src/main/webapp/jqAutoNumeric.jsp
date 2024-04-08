<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

formId = "jqAutoNumeric";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    JqAutoNumeric comp = new JqAutoNumeric("txtn") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
    processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };
    
    comp.setNumeric('$', true, 2);
    
    comp.setLabelId("lbl");
    
    
    // comp.setDebug(true);
    form.add(comp);
 
    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp); 
}
%> 

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, JqAutoNumeric comp, int cnt) {
    System.out.println((++cnt)+") onSubmitCompleted, value=" + comp.getValue() );
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
  <legend>Demo JqAutoNumeric</legend>
  <label id="lbl">Test JqAutoNumeric <input id="txtn" type="Text"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


