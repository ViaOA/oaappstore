<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "htmlSelect";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    HtmlSelect comp = new HtmlSelect("sel") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted"); 
        }
    };
    comp.setLabelId("lbl");
    comp.setDebug(true);
    
    comp.add(new HtmlOptionGroup("This is group1"));
    HtmlOption ho = new HtmlOption("test1", "Pick Test One", false);
    comp.add(ho);
    comp.add(new HtmlOptionGroup("This is group2"));
    ho = new HtmlOption("test2", "Pick Test Two", false);
    comp.add(ho);
    comp.add(new HtmlOptionGroup("This is group3"));
    ho = new HtmlOption("test3", "Pick Test Three", false);
    comp.add(ho);
    
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
  <legend>Demo HtmlSelect</legend>
  <label id="lbl">Test HtmlSelect <select id="sel"></select></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


