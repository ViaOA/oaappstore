<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
  formId = "appUserJsp";
  form = oasession.getForm(formId);
  if (form == null || oasession.isReset()) {
      form = oasession.createForm(formId);
  
      final AppUserModel model = new AppUserModel(ModelDelegate.getAppUsers());
      final AppUserJsp jspAppUser = new AppUserJsp(ModelDelegate.getAppUsers(), form);
  
      form.add(jspAppUser.getTable());
      form.add(jspAppUser.getIdLabel());
      form.add(jspAppUser.getLoginIdTextField());
      form.add(jspAppUser.getPasswordPassword());
      form.add(jspAppUser.getAdminCheckBox());
    
      form.add(jspAppUser.getFirstNameTextField());
      form.add(jspAppUser.getLastNameTextField());
      form.add(jspAppUser.getInactiveDateTextField());
      form.add(jspAppUser.getEditProcessedCheckBox());
      form.add(jspAppUser.getNoteTextField());
  }
%>

<%@ include file="include/htmlHeader.jspf"%>


<div id="table">table will be placed here</div>
<p>
ID: <span id="lblId"></span>
<p>
Login ID: <input id="txtLoginId" type="text" placeholder="Login Id">
<p>
Password: <input id="ptxtPassword" type="password">
<p>
Admin: <input id='chkAdmin' type='checkbox'>
<p>
First Name: <input id="txtFirstName" type="text">
<p>
Last Name: <input id="txtLastName" type="text">
<p>
Inactive Date: <input id="txtInactiveDate" type="text">
<p>
Edit Processed: <input id='chkEditProcessed' type='checkbox'>
<p>
Notes: <input id="txtNote" type="text">




<%@ include file="include/htmlFooter.jspf"%>


