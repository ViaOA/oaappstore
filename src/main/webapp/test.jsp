<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
// Custom for this page
formId = "test"; // required
form = oasession.getForm(formId);
        
if (form == null || oasession.isReset()) {
    form = new OAForm(formId);
    
    Hub hub = ModelDelegate.getAppUsers();
    hub.setPos(0);
    OATextField txt = new OATextField("txtFirstName", hub, AppUser.P_FirstName);
    txt.setAjaxSubmit(true);
    form.add(txt);
    
    OAInputCheckBox chk = new OAInputCheckBox("chkEditProcessed", hub, AppUser.P_EditProcessed);
    // chk.setAjaxSubmit(true);
    form.add(chk);

    chk = new OAInputCheckBox("chkAdmin", hub, AppUser.P_Admin);
    // chk.setAjaxSubmit(true);
    form.add(chk);
    
    oasession.addForm(form);
}
%>

<%@ include file="include/htmlHeader.jspf"%>



  HEY ... this is from test.jsp
  <%= (new OADateTime()).toString("MM/dd/yyyy HH:mm:ss") %>
  <%=formId %>,
  First Name: <input id="txtFirstName" type="text" value="test" size="12" maxlength="3">
<br>
  EditProcessed <input id='chkEditProcessed' type='checkbox' name='chkEditProcessed' value='' checked>
<br>
  Admin <input id='chkAdmin' type='checkbox' name='chkAdmin' value='' checked>


<%@ include file="include/htmlFooter.jspf"%>
