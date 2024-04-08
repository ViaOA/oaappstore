<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaHtmlButton";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);
    
    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    
    OAHtmlButton comp = new OAHtmlButton("cmdNext", hub, HtmlButton.Type.Button, OAUICommandController.Command.Next) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted " + getHub().getAO());
        }
    };
    comp.setButtonText("Next Button here");
    comp.setAjaxSubmit(true);
    form.add(comp);
    
    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);

    comp = new OAHtmlButton("cmdPrevious", hub, HtmlButton.Type.Submit, OAUICommandController.Command.Previous);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);
    
    comp = new OAHtmlButton("cmdFirst", hub, HtmlButton.Type.Submit, OAUICommandController.Command.First);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdLast", hub, HtmlButton.Type.Submit, OAUICommandController.Command.Last);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdClearAO", hub, HtmlButton.Type.Button, OAUICommandController.Command.ClearAO);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdAddNew", hub, HtmlButton.Type.Submit, OAUICommandController.Command.AddNew);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdDelete", hub, HtmlButton.Type.Submit, OAUICommandController.Command.Delete);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdNewManual", hub, HtmlButton.Type.Submit, OAUICommandController.Command.NewManual) {
        protected Object getManualObject() {
            AppUser au = new AppUser();
            au.setLastName("Manually created");
            return au;
        }
    };
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAHtmlButton("cmdGoTo", hub, HtmlButton.Type.Submit, OAUICommandController.Command.GoTo) {
        protected Object getManualObject() {
            AppUser au = (AppUser) getHub().getAt(2);
            return au;
        }
    };
    comp.setAjaxSubmit(true);
    form.add(comp);
    
    OAHtmlElement comp2 = new OAHtmlElement("eleX", hub, AppUser.P_LastName);
    form.add(comp2);
    
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
  <legend>Demo OAHtmlButton</legend>
  <label id="lbl">Test ClearAO Button <button id="cmdClearAO" type="">ClearAO</button></label> <br>
  <label id="lbl1">Test Next Button <button id="cmdNext" type="">Next</button></label> <br>
  <label id="lbl2">Test Previous Button <button id="cmdPrevious" type="">Previous</button></label> <br>
  <label id="lbl3">Test First Button <button id="cmdFirst" type="">First</button></label> <br>
  <label id="lbl4">Test Last Button <button id="cmdLast" type="">Last</button></label> <br>
  <label id="lbla">Test GoTo Button <button id="cmdGoTo" type="">GoTo 3rd one</button></label> <br>

  <label id="lbl5">Test AddNew Button <button id="cmdAddNew" type="">AddNew</button></label> <br>
  <label id="lbl6">Test Delete Button <button id="cmdDelete" type="">Delete</button></label> <br>
  
  <label id="lbl5">Test NewManual Button <button id="cmdNewManual" type="">AddNew</button></label> <br>

  
  <label id="lbl7">OAHtmlElement LastName: <span id="eleX">this should be replaced</span></label> <br>
  
  
  
<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>



