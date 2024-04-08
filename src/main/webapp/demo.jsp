<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@page trimDirectiveWhitespaces="true"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page language="java" errorPage="oaerror.jsp"%>
<%@page import="java.io.*,java.util.*, java.awt.*"%>
<%@page import="java.util.logging.*"%>

<%@page import="com.viaoa.object.*,com.viaoa.hub.*,com.viaoa.util.*,com.viaoa.jsp.*,com.viaoa.datasource.*,com.viaoa.datasource.jdbc.*,com.viaoa.jfc.image.*, com.viaoa.process.*"%>
<%@page import="com.viaoa.jsp.jqueryui.*, com.viaoa.jsp.bootstrap.*"%>

<%@page import="com.viaoa.appstore.model.oa.*,com.viaoa.appstore.model.oa.propertypath.*, com.viaoa.appstore.model.oa.search.*"%>
<%@page import="com.viaoa.appstore.model.*,com.viaoa.appstore.model.search.*"%>
<%@page import="com.viaoa.appstore.jsp.oa.*, com.viaoa.appstore.jsp.*"%>
<%@page import="com.viaoa.appstore.delegate.*"%>

<%-- OAJSP hierarchy: System/Application/Session/Form --%>
<jsp:useBean id="oasystem" scope="application" class="com.viaoa.jsp.OASystem" />

<%!static Logger LOG = Logger.getLogger("oajsp");%>

<%
    response.setHeader("Pragma:", "no-cache");
			response.setHeader("Expires:", "Thu, 16 Dec 1999 16:00:00 GMT");

			String applicationId = "oajsp";
			OAApplication oaapplication = oasystem.getApplication(applicationId, application);
			OASession oasession = oaapplication.getSession(session);
			OAForm form = null;
%>

<%
            String formId = "demoJsp";
			boolean bNew = (oasession.getForm(formId) == null);

			form = oasession.getForm(formId);
//qqqqqqqqqqq      
form = null;    
			if (form == null) {
				form = oasession.createForm(formId, "demo.jsp");

				DemoJsp jspDemo = new DemoJsp(ModelDelegate.getAppUsers(), form);

				// create some sample data/employees
                String[] fns = new String[] {"John","Mary","Jim","Tracy","Victor","Howard","Pete","Morgan","Eva","Eli","Jethro"};
                String[] lns = new String[] {"Harris","Johnson","Smith","Doe","Tomlinson","Masters","Philips","Harrison"};
                String[] emails = new String[] {"gmail","yahoo","garage","bing"};
                
				Hub<AppUser> hubAppUser = jspDemo.getModel().getHub();
				hubAppUser.removeAll();
				int cid = 0;
				for (int i = 0; i < 40; i++) {
				    String fn = fns[i%fns.length];
                    String ln = lns[i%lns.length];
					AppUser user = new AppUser();
					user.setFirstName(fn);
					user.setLastName(ln);
					user.setNote( (fn.charAt(0)+ln+"@"+emails[i%emails.length]+".com").toLowerCase());
          
					hubAppUser.add(user);

					int max = (int) (Math.random() * 25) + 8;

					for (int ix = 0; ix < max; ix++) {
						AppUserLogin login = new AppUserLogin();
						login.setConnectionId(cid++);
						login.setComputerName("computer." + i + "." + ix);
						user.getAppUserLogins().add(login);
					}
					user.save();
				}
				hubAppUser.setPos(0);
//				hubAppUser.sort(Employee.P_LastName);

				AppUserModel modelAppUser = jspDemo.getModel();
				OAButton cmd;
				OATextField txt;


				OATabbedPane tabPane = new OATabbedPane("tabTest");
			    tabPane.addTab("Tab1", "#tabTest1");
                tabPane.addTab("Tab2", "test.jsp");
                tabPane.addTab("Tab3", "test.jsp");
				form.add(tabPane);
        
        
				/*qqqqqqq add to demoJsp.java        
				        OATypeAheadParams<AppUser, AppUser> tap;
				        OATypeAhead<AppUser, AppUser> ta;
				      
				        // TagsInput free form, multiple values
				        txt = new OATextField("txtTagsInput", jspDemo.getModel().getHub(), AppUser.P_Note);
				        txt.setMultiValue(true);
				        form.add(txt);
				
				        // TypeAhead only, one, no tagsinput, stored in prop
				        txt = new OATextField("txtTypeAhead", jspDemo.getModel().getHub(), AppUser.P_Note);
				        tap = new OATypeAheadParams<>(); 
				        tap.matchPropertyPath = AppUser.P_FullName;
				        ta = new OATypeAhead<AppUser, AppUser>(hubAppUser, tap);
				        txt.setTypeAhead(ta);
				        form.add(txt);
				        
				        // TypeAhead multiple in one prop - store values comma separated
				        txt = new OATextField("txtTypeAheadAndTagInput", jspDemo.getModel().getHub(), AppUser.P_Note);
				        tap = new OATypeAheadParams<>(); 
				        tap.matchPropertyPath = AppUser.P_FullName;
				        ta = new OATypeAhead<AppUser, AppUser>(hubAppUser, tap);
				        txt.setTypeAhead(ta);
				        txt.setMultiValue(true);
				        form.add(txt);
				        
				
				        // TypeAhead where prop is hub  
				        txt = new OATextField("txtTypeAheadAndTagInput", jspDemo.getModel().getHub(), AppUserPP.appUserLogins().pp);
				        tap = new OATypeAheadParams<>(); 
				        // tap.finderPropertyPath = AppUserPP.appUserLogins().pp;
				        tap.matchPropertyPath = AppUserLoginPP.created();
				        
				        Hub h = new Hub(AppUserLogin.class);
				        new HubMerger(jspDemo.getModel().getHub(), h, AppUserPP.appUserLogins().pp, false, true);
				        ta = new OATypeAhead<AppUser, AppUser>(h, tap);
				        
				        txt.setTypeAhead(ta);
				        //txt.setMultiValue(true);
				        form.add(txt);
				
				
				        // TypeAhead where prop is oneLink
				        txt = new OATextField("txtTypeAheadAndTagInput", jspDemo.getModel().getAppUserLogins(), AppUserLoginPP.appUser().pp);
				        tap = new OATypeAheadParams<>(); 
				        tap.matchPropertyPath = AppUserPP.fullName();
				        
				        ta = new OATypeAhead<AppUser, AppUser>(jspDemo.getModel().getHub(), tap);
				        txt.setTypeAhead(ta);
				        form.add(txt);
				        
				*/

				/*qqqqqqqqq create multiple typeAhead and txt.multiple=true        
				        // use textfield + typeahead for list of appUsers.fullName
				        txt = new OATextField("txtTo");
				        tap = new OATypeAheadParams<>(); 
				        tap.matchPropertyPath = AppUser.P_LastName;
				        tap.displayPropertyPath = AppUser.P_FullName;
				        tap.dropDownDisplayPropertyPath = AppUser.P_FirstName;
				        
				        ta = new OATypeAhead<AppUser, AppUser>(hubAppUser, tap) {
				            @Override
				            public String getDropDownDisplayValue(AppUser au) {
				return au.getFullName()+"  (<i>xx"+au.getId()+"xx</i>)";
				            }
				        };
				        txt.setTypeAhead(ta);
				        txt.setMultiValue(true);
				        form.add(txt);
				*/

				OAHtmlElement lbl;
				OACheckBox chk;
				OACombo cbo;
				/*qqq        
				OASelectMenu sm = new OASelectMenu("sm", modelAppUser.getHub(), AppUser.P_FullName, 25);
				sm.setMaxHeight("14em");
				sm.setNullDescription("Select a User");
				sm.setAjaxSubmit(true);
				form.add(sm);
				
				sm = new OASelectMenu("sm2", modelAppUser.getAppUserLoginsModel().getHub(), AppUserLogin.P_Created, 14);
				sm.setMaxHeight("14em");
				sm.setNullDescription("Select a user login");
				sm.setAjaxSubmit(true);
				form.add(sm);
				*/
				/*qqqqq        
				OAComboBox bscbo = new OAComboBox("bscbo", modelAppUser.getHub(), AppUser.P_FullName, 25);
				bscbo.setMaxHeight("14em");
				bscbo.setNullDescription("Select a User");
				bscbo.setAjaxSubmit(true);
				form.add(bscbo);
				
				bscbo = new OAComboBox("bscbo2", modelAppUser.getAppUserLoginsModel().getHub(), AppUserLogin.P_Created, 14);
				bscbo.setMaxHeight("14em");
				bscbo.setNullDescription("Select a user login");
				bscbo.setAjaxSubmit(true);
				form.add(bscbo);
				*/

				// form.addScript("alert('Welcome to OA|JSP Demo');");
			}
%>



<!DOCTYPE html>
<html lang="enUS">

<HEAD>
<TITLE>OA|JSP</TITLE>
<meta charset="utf-8">
<meta content="text/html; charset=UFT-8" http-equiv=Content-Type>
<meta http-equiv="Content-Language" content="en-us" />
<meta http-equiv="Expires" content="Tue, 20 Aug 1996 14:25:27 GMT">
<meta HTTP-EQUIV="Pragma" CONTENT="no-cache">
<link rel="shortcut icon" type="image" href="image/icon.gif" />


<!-- form css insert -->
<%=form.getCssInsert()%>
<!-- qqqqqq -->
<link href="vendor/bootstrap-select/css/bootstrap-select.css" rel="stylesheet">



<!-- form js insert -->
<%=form.getJsInsert()%>

<!-- qqqqqq -->
<script type="text/javascript" language="javascript" src="vendor/bootstrap-select/js/bootstrap-select.js"></script>


<style>

.oatableSelected {
    border: 2px red solid;
}

.tt-dropdown-menu {
	background-color: yellow;
	max-height: 400px;
	overflow-y: auto;
}

/* Move down content because we have a fixed navbar that is 50px tall */
body {
	padding-top: 50px;
}

/* Hide for mobile, show later */
.sidebar {
	display: none;
}

@media ( min-width : 768px) {
	.sidebar {
		position: fixed;
		top: 51px;
		bottom: 30px;
		left: 0;
		z-index: 1000;
		display: block;
		padding: 20px;
		overflow-x: hidden;
		overflow-y: auto;
		border-right: 1px solid #eee;
	}
}

/* Sidebar navigation */
.nav-sidebar {
	margin-bottom: 32px;
	margin-left: -20px;
	margin-right: -21px;
}

.nav-sidebar>li>a {
	padding-right: 20px;
	padding-left: 20px;
}

.nav-sidebar>.active>a, .nav-sidebar>.active>a:hover, .nav-sidebar>.active>a:focus {
	color: #fff;
	background-color: #428bca;
}

/*
 * Main content
 */
.main {
	padding: 20px;
}

@media ( min-width : 768px) {
	.main {
		padding-right: 40px;
		padding-left: 40px;
	}
}

.main .page-header {
	margin-top: 0;
}

.footer {
	position: fixed;
	bottom: 0;
	width: 100%;
	height: 32px;
	background-color: #f5f5f5;
}
</style>

</HEAD>

<%=form.getScript()%>

<body>

  <!-- map id to oaform -->
  <form id="<%=form.getId()%>">

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Project name</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
        </div>
        <!--/.nav-collapse -->
      </div>
    </nav>


    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li class="active"><a href="#">Overview</a></li>
            <li><a href="#">Reports</a></li>
            <li><a href="#">Analytics</a></li>
            <li><a href="#">Export</a></li>
          </ul>
          <ul class="nav nav-sidebar">
            <li><a href="">Nav item</a></li>
            <li><a href="">Nav item again</a></li>
            <li><a href="">One more nav</a></li>
            <li><a href="">Another nav item</a></li>
            <li><a href="">More navigation</a></li>
          </ul>
          <ul class="nav nav-sidebar">
            <li><a href="">Nav item again</a></li>
            <li><a href="">One more nav</a></li>
            <li><a href="">Another nav item</a></li>
          </ul>
        </div>
      </div>



      <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

<br><br>
<span id="spanAaa">
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
</span>
<br>
<br>XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX<br>


<br> 
<br>



<select id="selTest" class="" data-live-search="true">
<option>Mustard</option><option>Ketchup</option><option>Relish</option><option>Mustard1</option><option>Ketchup1</option><option>Relish1</option><option>Mustard2</option>
<option>Ketchup2</option><option>Relish2</option><option>Mustard3</option><option>Ketchup3</option><option>Relish3</option><option>Mustard</option><option>Ketchup</option><option>Relish</option><option>Mustard1</option><option>Ketchup1</option>
<option>Relish1</option><option>Mustard2</option><option>Ketchup2</option><option>Relish2</option><option>Mustard3</option><option>Ketchup3</option><option>Relish3</option><option>Mustard</option>
<option>Ketchup</option><option>Relish</option><option>Mustard1</option><option>Ketchup1</option><option>Relish1</option><option>Mustard2</option><option>Ketchup2</option><option>Relish2</option><option>Mustard3</option><option>Ketchup3</option><option>Relish3</option>
</select>

<script type="text/javascript">

$('#selTest').selectpicker({
    style: 'btn-default',
    header: 'search for state',
    noneSelectedText:'please select a value',
    size: 8
  });


</script>

        <div style="border: 1px yellow solid;">
          OAButtonList1 - no cols<br>
          <button id="cmdButtonList1" class="btn btn-default">cmdButtonList1 here</button>
          <br><br>OAButtonList2 - 20 cols<br>
          <button id="cmdButtonList2" class="btn btn-default">cmdButtonList2 here</button>
          <br><br>OAButtonList3 - no columns<br>
          <button id="cmdButtonList3" class="btn btn-default">cmdButtonList3 here</button>
          <br><br>OAButtonList4 - width=30%, popup width=100%<br>
          <button id="cmdButtonList4" class="btn btn-default">cmdButtonList4 here</button>
        </div>
<br><br><br>


<!-- OATabbedPane qqqqqqqqqqqqqqqqqqqqqqqqqqqqqq -->

        <div id="tabTest" class="container"></div>

        <div id="tabTest1" class="XXoaHide">This is tabTest1 data here</div>


        <br><br><br>
<!-- tabPane -->
        <div id="tabMain" class="container" style="border: red 3px solid;">
          <ul class="nav nav-tabs">
            <li><a href="#tabMain1" Xdata-toggle="tab">Overview</a></li>
            <li class="active"><a href="#tabMain2" Xdata-toggle="tab">Using nav-pills</a></li>
            <li><a href="#tabMain3" Xdata-toggle="tab">Applying clearfix</a></li>
            <li><a href="#tabMain4" Xdata-toggle="tab">Background color</a></li>
          </ul>

          <div class="tab-content clearfix">
            <div class="tab-pane active" id="tabMain1">
              <h3>Content's background color is the same for the tab</h3>
            </div>
            <div class="tab-pane" id="tabMain2">
              <h3>We use the class nav-pills instead of nav-tabs which automatically creates a background color for the tab</h3>
            </div>
            <div class="tab-pane" id="tabMain3">
              <h3>We applied clearfix to the tab-content to rid of the gap between the tab and the content</h3>
            </div>
            <div class="tab-pane" id="tabMain4">
              <h3>We use css to change the background color of the content to be equal to the tab</h3>
            </div>
          </div>
        </div>





        <script>
        
        $('#tabMain ul li a').click(function (e) {
            e.preventDefault();
            // ajaxSubmit();
            $(this).tab('show');
        })        
        
        </script>


        <br>
        <br>
        <br> OAStyledTextArea<br>
        <textarea cols="40" id="stxtaTest" rows="10"></textarea>

        <br>
        <br>
        <br>

        <!-- qqqqqq
Summernote<br>
<textarea cols="80" id="txtaSummernote" rows="10" ></textarea>
<script>
/*
$('#txtaSummernote').summernote({
    toolbar: [
      // [groupName, [list of button]]
      ['style', ['bold', 'italic', 'underline', 'clear']],
      ['font', ['strikethrough', 'superscript', 'subscript']],
      ['fontsize', ['fontsize']],
      ['color', ['color']],
      ['para', ['ul', 'ol', 'paragraph']],
      ['height', ['height']]
    ]
  });
*/
</script>

<br><br><br>

CKEditor<br>
<textarea cols="80" id="editor1" name="editor1" rows="10" >&lt;p&gt;This is some &lt;strong&gt;sample text&lt;/strong&gt;. You are using &lt;a href="http://ckeditor.com/"&gt;CKEditor&lt;/a&gt;.&lt;/p&gt;
</textarea>
<script>
/*
  CKEDITOR.replace( 'editor1', {
      height: 260,
      width: 700,
  } );
*/  
</script>
qqqqqq  -->

        <br>
        <br>
        <br>


        <button id="cmdPopover">Hover over for a popover</button>

        <button id="cmdPopover2">Hover over for a popover2</button>


        <div id="slimscrollTest" style="border: 2px red solid;">
          lots of text lots of text lots of text lots of text lots of text<br> lots of text lots<br>lots of text lots of text lots of text lots of text lots of
          text<br>lots of text lots of text lots of text lots of text lots of text<br> lots of text lots of text lots of text lots of text lots of text<br>lots
          of text lots of text lots of text lots of text lots of text<br> lots of text lots of text lots of text lots of text lots of text<br>lots of text lots of
          text lots of text lots of text lots of text<br> lots of text lots<br>lots of text lots<br>lots of text lots<br>lots of text lots<br>lots
          of text lots<br>lots of text lots<br>lots of text lots<br> lots of text lots of text lots of text lots of text lots of text
        </div>


        <br>
        <br>
        <br>

        <button id="cmdTt" type="button" class="btn btn-primary" data-toggle="tooltip" data-placement="top" title="Tooltip test text here">Tooltip test</button>


        <script>
                                    /*
                                     $('#slimscrollTest').slimscroll({
                                     height: '100px',
                                     width: '400px',
                                     size: '9px'
                                     });
                                     */

                                    var objx = $('#cmdPopover2');
                                    $('#cmdPopover2').popover({
                                        content : 'This is a popover2 message',
                                        placement : 'top',
                                        title : 'Popover2 test',
                                        trigger : 'hover'
                                    });

                                    $(function() {
                                        $('#cmdTt').tooltip();
                                    });
                                </script>



        <button id="cmdSpin" type="button" class="btn btn-primary ladda-button" data-style="slide-right" data-toggle="tooltip" data-placement="top" title="Tooltip here">
          <span class="ladda-label">Slide Right</span> <span class="ladda-spinner"></span>
        </button>

        <script>
                                    $(function() {
                                        $('#cmdSpin').click(function(e) {
                                            e.preventDefault();
                                            var l = Ladda.create(this);
                                            l.start();
                                            setTimeout(function() {
                                                alert("==>" + $('#cmdSpin'));
                                                //Ladda.create($('#cmdSpin')).stop();
                                                l.stop();
                                            }, 1000);
                                            return false;
                                        });
                                    });
                                </script>

        <br>
        <br>
        <br> zzzzzzzzzzzzzzzzzzzzzzzzzzzz <br>
        <br>
        <br>

        <div class="oaMouseOverShadow" style="margin-left: 25px; padding: 25px; display: inline-block;">
          (click on Full Name to display) <br> #oaFormMessage: <span id="oaFormMessage">Messages will show here</span> <br> #oaFormErrorMessage: <span
            id="oaFormErrorMessage">Error Messages will show here</span> <br> #oaHiddenFormMessage: <span id="oaFormHiddenMessage">Hidden Messages will show
            here</span>
        </div>

        <br>


        <div>
          <button id="cmdCreateOAProcess">create form process</button>
          <button id="cmdHideOAProcessDialog">hide process dialog</button>
          <button id="cmdShowOAProcessDialog">show process dialog</button>
          <button id="cmdCancelOAProcesses">cancel process</button>
          <button id="cmdClearOAProcesses">clear form process</button>
        </div>

        <div>
          Active UserID: <span id="badgeId">99999</span>
        </div>



        UserId:
        <div id="lblId">ID should be here</div>

        First Name: <input id="txtFirstName" type="text" value="test" size="15" maxlength="3"> 
        
        <br> 
        First Name+ajax: <input id="txtFirstNameAjax" type="text" value="test" size="15" maxlength="30"> <br> 
        
        
        Last Name: <input id="txtLastName" type="text" value="test" size="12" maxlength="3"> <br>
        Password: <input id="ptxtPassword" type="password"></input> <br> Fullname+ajax (click to see messages): <span id="lblFullNameAjax"
          style="border: 1px solid green;">XXXX</span> <br> <br> Admin: <input id="chkAdmin" type="checkbox" name="abc" value="def" checked> <br> <input
          id="radAdminTrue" type="radio"> Admin Yes <input id="radAdminFalse" type="radio"> Admin No <br> Inactive date: <input id="txtInactiveDate"
          type="text"> 
          
<br>Select of all AppUsers: <select id="cbo">
          <option value="x">none</option>
        </select> 
        

<br>Select of user Logins: <select id="AppUserLoginscbo">
          <option value="x">none</option>
        </select> <br> <br>Created datetime: <input id="AppUserLoginstxtCreated" type="text"> <br>jquiSelectMenu of all AppUsers: <select id="sm"></select> <br>jquiSelectMenu
        of user Logins: <select id="sm2"></select> <br>

        <br>bsComboBox of all AppUsers: <select id="bscbo"></select> <br>bsComboBox of user Logins: <select id="bscbo2"></select> <br> <br>HtmlSelect+ajax:<br>
        <select id="select">
        </select> <br> <br> <input id="cmdSubmit" type="submit" value="Submit"> <br>
        <Button id="cmdSubmitAjax">Ajax Submit Button w/Spinner</Button>
        <br> Fullname in a div:


        <div id="lblTest"></div>
        <br>

        <textarea id="txta"></textarea>
        <br>

        <div>
          OAListing<br>
          <ol id="XXXXqqqqqqqqqqqXXXlisting"></ol>
        </div>

        <div>
          OAList appUser<br>
          <div>
            <ol id="list"></ol>
          </div>
        </div>

        <div>
          OAList appUserLogin<br>
          <div>
            <ol id="listAppUserLogins">
              <li>appUserLogin OAList here
            </ol>
          </div>
        </div>

        <div id="table">table will be placed here</div>

        <div id="tableAppUserLogins">tableAppUserLogins will be placed here</div>
  </form>

  <div id='dlgTest'>
    This is the text <br> for the Dialog box, it can have anything in it.
    <button>Like custom buttons</button>
    Text Field: <input type="text">
  </div>

  <br>
  <div id="grid" style="width: 70%;">grid will be placed here</div>

  <button onclick="oaShowMessage('title goes here', 'your message goes here'); return false;">test popup modal message</button>

  <br>
  <button onclick="$('#oaDialogdlgTest').modal({keyboard: true}); return false;">test dialog</button>

  <br>
  <button id="cmdSnackbar">show snackbar message</button>

  <br>

  <div style="border: 2px black solid; padding: 15px;">
    <button id="popupCommandA">popup top</button>
    <button id="popupCommandB">popup bottom</button>
    <button id="popupCommand1">popup top-right</button>
    <button id="popupCommand2">popup bottom-right</button>
    <button id="popupCommand3">popup bottom-left</button>
    <button id="popupCommand4">popup top-left</button>
    <button id="popupCommand7">popup middle-right</button>
    <button id="popupCommand5">popup center</button>
    <button id="popupCommand6">popup other</button>
  </div>

  <br>
  <div id="popupDivA" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div top</div>
  <div id="popupDivB" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div bottom</div>
  <div id="popupDiv1" class="oaHide">
    popup div top-right<br>
    <h1>this is a long text to display the popup text that needs to be be hidden</h1>
    here's the bottom text.
  </div>
  <div id="popupDiv2" class="oaHide">
    popup div bottom-right<br>
    <h1>this is a long text to display the popup text that needs to be be hidden</h1>
    here's the bottom text.
  </div>
  <div id="popupDiv3" class="oaHide">popup div bottom-left</div>
  <div id="popupDiv4" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div top-left</div>
  <div id="popupDiv5" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div center</div>
  <div id="popupDiv6" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div other</div>
  <div id="popupDiv7" class="oaHide" style="min-width: 20vw; min-height: 20vh;">popup div right-middle</div>
  <br>


  <button id="expanderCommand">click to Expand</button>
  <br>

  <div id="expanderDiv" class="oaHide" style="border: 2px blue solid; text-align: left;">
    this is expanded or collapsed<br> using another <br> button or html element;
  </div>
  some text after expand/collapse.

  <div style="height: 50px;">&nbsp;</div>


  TREE HERE:
  <div id="tree" style="width: 400px; height: 400px; border: 2px green solid; overflow: scroll;">tree here</div>

  <script>
            function xclick() {
                $('#tree').remove();
            }
        </script>
  <button onclick="xclick(); return false;">Update Tree</button>

  </div>


    <ul id="list"></ul>


  <br>
  <br>
  <br>
  <br>
  <br> Bottom of the page ... Bottom of the page ... Bottom of the page ...
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>

  </div>


  <footer class="footer">
    <div class="container-fluid">Place sticky footer content here.</div>
  </footer>



</body>
</html>





