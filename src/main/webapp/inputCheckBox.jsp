<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "inputCheckBox";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    InputCheckBox comp = new InputCheckBox("chk") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted, checked="+getChecked()+", value="+getValue());
        }
    };
    comp.setValue("test");
    comp.setChecked(true);
    comp.setLabelId("lbl");
    comp.setDebug(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);
    
    comp = new InputCheckBox("chk2") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted, chk2 checked="+getChecked()+", value="+getValue());
        }
    };
    comp.setChecked(true);
    comp.setLabelId("lbl2");
    form.add(comp);
    
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

  <legend>Demo InputCheckBox</legend>
  <label id="lbl">Test InputCheckBox <input id="chk" type="checkbox" value="CheckBoxValue"></label> <br>
  <label id="lbl2">Test InputCheckBox 2 <input id="chk2" type="checkbox" value="CheckBoxValue2"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>

