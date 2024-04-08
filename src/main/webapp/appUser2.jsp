<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
  formId = "appUserJsp";
  form = oasession.getForm(formId);
  if (form == null) {
      form = oasession.createForm(formId);
  
      final AppUserJsp jspAppUser = new AppUserJsp(ModelDelegate.getAppUsers(), form);
  
      form.add(jspAppUser.getTable());
      form.add(jspAppUser.getIdLabel());
  }
%>

<%=form.getScript()%>

