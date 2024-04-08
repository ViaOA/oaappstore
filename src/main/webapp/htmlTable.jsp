<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "htmlTable";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    String compId = "table";
    HtmlTable comp = new HtmlTable(compId) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt) + ") onSubmitCompleted");
        }
    };
    
    HtmlColGroup colGroup = new HtmlColGroup(compId+"cg");
    int cols = 3;
    int rows = 10;
    
    
    for (int i=0; i<cols; i++) {
        HtmlCol hc = new HtmlCol(compId+"hc"+i);
        hc.addStyle("width", "12em");
        colGroup.addCol(hc);
    }
    comp.setColGroup(colGroup);    
    
    HtmlTR tr = new HtmlTR(compId+"thead");
    comp.addTHeadRow(tr);
    
    for (int i=0; i<cols; i++) {
        HtmlTH th = new HtmlTH();
        th.setInnerHtml("thead"+i);
        tr.addTableData(th);
    }
    
    
    for (int row=0; row<rows; row++) {
        tr = new HtmlTR(compId+"tbody");
        comp.addTHeadRow(tr);
        for (int col=0; col<cols; col++) {
            HtmlTD td = new HtmlTD(compId+"tbodyR"+row+"C"+col);
            td.setInnerHtml("R"+row+",R"+row);
            tr.addTableData(td);
        }
    }
    
    tr = new HtmlTR(compId+"tfoot");
    comp.addTHeadRow(tr);
    
    for (int i=0; i<cols; i++) {
        HtmlTH th = new HtmlTH(compId+"tfootC"+i);
        th.setInnerHtml("tfoot"+i);
        tr.addTableData(th);
    }
    
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
<a id="cmdOAReset" href="oareset.jsp">oareset</a>
&nbsp;&nbsp;&nbsp;
<input type=submit value="submit">
<input id="cmdDoAjaxSubmit" type=button value="xxx">
debug
<input id="chkFormDebug" type="checkbox">

<fieldset>
  <legend>Demo HtmlTable</legend>
  <label id="lbl">Test HtmlTable
    <table id="table" border="2">
    </table>
  </label> <br>

  <p>
    <br>
    <br>
    <%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>



