<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "inputButton";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);
    // verify form.url

    InputButton comp = new InputButton("cmd") {
        int cnt;
        int cnt2;
        @Override
        public void onSubmitBeforeLoadValues(OAFormSubmitEvent formSubmitEvent) {
            // if ((++cnt2 % 2) == 0) formSubmitEvent.setCancel(true);            
        }
        @Override
        public void onSubmit(OAFormSubmitEvent formSubmitEvent) {
            formSubmitEvent.getForm().addMessage("Message #"+(++cnt));            
        }
    };
    comp.setValue("Button Here!");
    comp.setAjaxSubmit(true);
    
    form.addMessage("this is a test initialize msg");

    // comp.setHidden(true);
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

  <legend>Demo InputButton</legend>
  <label><input id="cmd" type="button" value="Wrong text"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>



