<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

formId = "oaBsTypeAheadMultiSelect";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    
    OATypeAhead.OATypeAheadParams tp = new OATypeAhead.OATypeAheadParams();
    tp.finderPropertyPath = "";
    tp.matchPropertyPath = AppUser.P_FullName;
    tp.displayPropertyPath = AppUser.P_FullName;
    tp.dropDownDisplayPropertyPath = AppUser.P_FullName;
    tp.minInputLength = 1;
    tp.maxResults = 35;
    tp.showHint = true;

    OATypeAhead typeAhead = new OATypeAhead(ModelDelegate.getAppUsers(), tp);

    Hub<AppUser> hubAppUserTest = new Hub(AppUser.class);
    
    OABsTypeAheadMultiSelect comp = new OABsTypeAheadMultiSelect("txtta", hubAppUserTest, typeAhead) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
    };
//    comp.setAjaxSubmit(true); //qqqqqqqqqqqq if true then sends id ... if not true, then it sends text.value=fullname ......qqqqqqqqq
    
    // comp.setValue(""); 
    comp.setLabelId("lbl");
    // comp.setDebug(true);
    form.add(comp);

//    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp); 
}
%>

<%!
public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, BsTypeAhead comp, int cnt) {
    System.out.println((++cnt)+") onSubmitCompleted, value=" + comp.getValue() );
    OAForm form = formSubmitEvent.getForm();
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
  <legend>Demo OABsTypeAheadMultiSelect</legend>
  <d>demo: to show using typeahead for selecting multiple objects in a hub</d><p>
  
  <label id="lbl">Test OABsTypeAheadMultiSelect <input id="txtta" type="Text"></label> <br>

<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


