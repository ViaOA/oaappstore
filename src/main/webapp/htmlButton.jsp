<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "htmlButton";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);
    
    HtmlButton comp = new HtmlButton("cmd1", HtmlButton.Type.Button) {
    };
    // comp.setButtonText(comp.getId());
    form.add(comp);
    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);

    comp = new HtmlButton("cmd2", HtmlButton.Type.Submit) {
    };
    // comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new HtmlButton("cmd3", HtmlButton.Type.Reset) {
    };
    // comp.setButtonText(comp.getId());
    comp.setEnabled(false);
    String s = "this button is disabled.";
    comp.setTitle(s);
    comp.setToolTip(s);
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
  <legend>Demo Html Button</legend>
  <label id="lbl">Test Button <button id="cmd1" type="XXX">Button <i>styled</i> Text</button></label> <br>
  <label id="lbl">Test Submit Button <button id="cmd2" type="XXX">Submit Button <i>styled</i> Text</button></label> <br>
  <label id="lbl">Test Reset Button <button id="cmd3" type="XXX">Reset Button <i>styled</i> Text</button></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>



