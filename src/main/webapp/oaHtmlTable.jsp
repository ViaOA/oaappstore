<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaHtmlTable";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUser> hubAppUser = ModelDelegate.getAppUsers();
    hubAppUser.DEBUG = true;

    OAHtmlTable table = new OAHtmlTable("tableAppUser", hubAppUser) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
             System.out.println((++cnt) + ") onSubmitCompleted");
        }
    };
  
//qqqqqqqqqqqqqqq    
//    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, table);
    
    form.add(table);
    table.addCounterColumn("#");

    OAInputText txt = new OAInputText("tableAppUserFirstName", hubAppUser, AppUser.P_FirstName);
    table.addColumn("First", txt, 12);

    txt = new OAInputText("tableAppUserLastName", hubAppUser, AppUser.P_LastName);
    table.addColumn("Last", txt, 15);
 
    OAHtmlElement ele = new OAHtmlElement("tableAppUserFullName", hubAppUser, AppUser.P_FullName);
    table.addColumn("Full Name", ele, 28);

    
    // second table
    
    Hub<AppUserLogin> hubUserLogin = hubAppUser.getDetailHub(AppUser.P_AppUserLogins);
    table = new OAHtmlTable("tableAppUserLogin", hubUserLogin);
    form.add(table);

    table.addCounterColumn("#");

    txt = new OAInputText("tableAppUserLoginHostName", hubUserLogin, AppUserLogin.P_HostName);
    table.addColumn("Host Name", txt, 14);

    ele = new OAHtmlElement("tableAppUserLoginCreated", hubUserLogin, AppUserLogin.P_Created);
    // ele.setToolTip("display/readonly version");
    table.addColumn("Created", ele, 14);

    OAInputDateTime dt = new OAInputDateTime("tableAppUserLoginCreated2", hubUserLogin, AppUserLogin.P_Created);
    // dt.setToolTip("html input datetime");
    table.addColumn("Created2", dt, 14);

/*    
    OABsDateTime bsdt = new OABsDateTime("tableAppUserLoginCreated", hubUserLogin, AppUserLogin.P_Created);
    bsdt.setToolTip("bootstrap datetime");
    table.addColumn("bsCreated", bsdt, 14);
*/    
    
/* qqqqqqqqq    
OABsDateTime bsdt2 = new OABsDateTime("tcbdtzCreated2", hubUserLogin, AppUserLogin.P_Created);
form.add(bsdt2);

for (int i=0; hubUserLogin.size()<5; i++) {
    AppUserLogin ul = new AppUserLogin();
    ul.setConnectionId(i);
    ul.setCreated(new OADateTime());
    hubUserLogin.add(ul);
}
*/
/*
    ele = new OAHtmlElement("tableAppUserLoginConnectionId", hubUserLogin, AppUserLogin.P_ConnectionId); 
    table.addColumn("ConnectId#", ele, 12);
*/

OAInputButton comp = new OAInputButton("cmdAddNewUserLogin", hubUserLogin, OAUICommandController.Command.AddNew);
comp.setAjaxSubmit(true);
comp.setButtonText("New");
form.add(comp);


    Hub<AppUser> hub = hubAppUser.createShared();
    hub.setLinkHub(hubUserLogin, AppUserLogin.P_AppUser);
    
    OAHtmlSelect sel = new OAHtmlSelect("tableAppUserLoginAppUser", hub, AppUser.P_FullName);
    table.addColumn("User", sel, 20);
    sel.setDebug(true);
    
    form.add(table);
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
  <legend>Demo HtmlTable - wrapped in Div that scrolls, with sticky header</legend>
  Test HtmlTable
  
<div style="overflow: scroll; width:500px; height:175px; margin: 20px; border: 1px solid black;">
    <table id="tableAppUser" border="2">
    </table>
</div>


<div style="overflow: scroll; width:820px; height:145px; margin: 20px; border: 1px solid black; position: relative;">
    <table id="tableAppUserLogin" border="2">
    </table>
</div>

  <label id="lbl5">Add New UserLogin<input type=button id="cmdAddNewUserLogin" value="New"></label> <br>



  <p>
    <br> <br>
    <%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>



