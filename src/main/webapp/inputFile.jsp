<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "inputFile";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    InputFile comp = new InputFile("file") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted, value="+getValue());
        }
        public OutputStream onSubmitGetFileOutputStream(String fname, long contentLength) {
            System.out.println("onSubmitGetFileOutputStream,fname="+fname+", contentLength="+contentLength);
            return new OutputStream() {
                public void write(int ch) throws IOException {
                    System.out.print((char) ch);
                }
            };
        }
    };
    // comp.setValue(""); // 
    comp.setMultiple(true);
    comp.setLabelId("lbl");
    comp.setDebug(true);
    comp.setAccept(".jpg, .png, .gif");
    comp.setMaxFileSize(2000);
    comp.setRequired(true);
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
  <legend>Demo InputFile</legend>
  <label id="lbl">Test InputFile <input id="file" type="file" accept="abc, xyz" multiple="multiple"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


