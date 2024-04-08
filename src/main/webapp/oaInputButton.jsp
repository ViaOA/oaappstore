<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaInputButton";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    
    OAInputButton comp = new OAInputButton("cmdNext", hub, OAUICommandController.Command.Next) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted " + getHub().getAO());
        }
    };
    comp.setValue("Next");
    comp.setAjaxSubmit(true);
    form.add(comp);

    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);
    
    comp = new OAInputButton("cmdPrevious", hub, OAUICommandController.Command.Previous);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);
    
    comp = new OAInputButton("cmdFirst", hub, OAUICommandController.Command.First);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdLast", hub, OAUICommandController.Command.Last);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdClearAO", hub, OAUICommandController.Command.ClearAO);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdAddNew", hub, OAUICommandController.Command.AddNew);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdDelete", hub, OAUICommandController.Command.Delete);
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdNewManual", hub, OAUICommandController.Command.NewManual) {
        protected Object getManualObject() {
            AppUser au = new AppUser();
            au.setLastName("Manually created");
            return au;
        }
    };
    comp.setAjaxSubmit(true);
    comp.setButtonText(comp.getId());
    form.add(comp);

    comp = new OAInputButton("cmdGoTo", hub, OAUICommandController.Command.GoTo) {
        protected Object getManualObject() {
            AppUser au = (AppUser) getHub().getAt(2);
            return au;
        }
    };
    comp.setButtonText(comp.getId());
    comp.setAjaxSubmit(true);
    form.add(comp);
    
    OAHtmlElement comp2 = new OAHtmlElement("ele", hub, AppUser.P_LastName);
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
  <legend>Demo OAInputButton</legend>
  <label id="lbl">Test ClearAO Button <input type=button id="cmdClearAO" value="ClearAO"></label> <br>
  <label id="lbl1">Test Next Button <input type=button id="cmdNext" value="Next"></label> <br>
  <label id="lbl2">Test Previous Button <input type=button id="cmdPrevious" value="Previous"></label> <br>
  <label id="lbl3">Test First Button <input type=button id="cmdFirst" value="First"></label> <br>
  <label id="lbl4">Test Last Button <input type=button id="cmdLast" value="Last"></label> <br>
  <label id="lbla">Test GoTo Button <input type=button id="cmdGoTo" value="GoTo 3rd one"></label> <br>

  <label id="lbl5">Test AddNew Button <input type=button id="cmdAddNew" value="AddNew"></label> <br>
  <label id="lbl6">Test Delete Button <input type=button id="cmdDelete" value="Delete"></label> <br>
  
  <label id="lbl5">Test NewManual Button <input type=button id="cmdNewManual" value="AddNew"></label> <br>

  
  <label id="lbl7">OAHtmlElement LastName: <span id="ele" value="this should be replaced</span></label> <br>
  
  
  
<p><br><br>
<%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>

