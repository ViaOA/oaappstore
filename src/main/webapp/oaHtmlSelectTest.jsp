<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "oaHtmlSelectTest";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    Hub<AppUserLogin> hubx = ModelDelegate.getAppUserLogins();
    hubx.setPos(0);
    
    Hub<AppUser> hub = ModelDelegate.getAppUsers();
    hub.setLinkHub(hubx, AppUserLogin.P_AppUser);
    
    OAHtmlSelect comp = new OAHtmlSelect("sel", hub, AppUser.P_FullName) {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            System.out.println((++cnt)+") onSubmitCompleted"); 
        }
    };
    comp.setLabelId("lbl");
    comp.setDebug(true);
    comp.setAjaxSubmit(true);
    form.add(comp);
    
}
%>

<%@ include file="include/htmlHeader.jspf"%>

  <label id="lbl">Test OAHtmlSelect <select id="sel"></select></label> <br>

<%@ include file="include/htmlFooter.jspf"%>


