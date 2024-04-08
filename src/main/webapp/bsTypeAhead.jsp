<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%
formId = "bsTypeAhead";
form = oasession.getRequestForm(formId);

if (form == null) {
    form = oasession.createRequestForm(formId);

    BsTypeAhead comp = new BsTypeAhead("txtta") {
        int cnt;
        public void onSubmitCompleted(OAFormSubmitEvent formSubmitEvent) {
            processOnSubmitCompleted(formSubmitEvent, this, ++cnt);
        }
        protected ArrayList<TypeAheadValue> getTypeAheadValues(String search) {
            return onGetTypeAheadValues(search);
        }
    };
    form.add(comp);
    comp.setMultiValue(false);
    comp.setLabelId("lbl");
    // comp.setValue("a,b,c");

    OATypeAhead ta = new OATypeAhead(Arrays.asList(new OAObject()));
    ta.setMinimumInputLength(3);
    ta.setMaxResults(35);
    ta.setShowHint(true);
    comp.setTypeAhead(ta);
    // comp.setDebug(true);

    
    HtmlElementPropertyEditor hpe = new HtmlElementPropertyEditor(form, comp);

    
    
    comp = new BsTypeAhead("txtta2") {
        protected ArrayList<TypeAheadValue> getTypeAheadValues(String search) {
            return onGetTypeAheadValues(search);
        }
    };
    form.add(comp);
    comp.setMultiValue(true);
    comp.setLabelId("lbl2");

    ta = new OATypeAhead(Arrays.asList(new OAObject()));
    ta.setMinimumInputLength(3);
    ta.setMaxResults(35);
    ta.setShowHint(true);
    comp.setTypeAhead(ta);
}
%>

<%!public void processOnSubmitCompleted(OAFormSubmitEvent formSubmitEvent, BsTypeAhead comp, int cnt) {
    System.out.println((++cnt) + ") onSubmitCompleted, value=" + comp.getValue());
    OAForm form = formSubmitEvent.getForm();
}

// overwrite matching values
protected ArrayList<BsTypeAhead.TypeAheadValue> onGetTypeAheadValues(String term) {
    ArrayList<BsTypeAhead.TypeAheadValue> al =  new ArrayList<>(); 

    int x = (int) (Math.random() * 10) + 1;
    for (int i = 0; i < x; i++) {
        String id = "id." + i + term;
        String displayValue = term + " display value here "+i;
        String dd = term + " dropdown <br><b>display</> here "+i;

        BsTypeAhead.TypeAheadValue tav = new BsTypeAhead.TypeAheadValue(""+(id+i), displayValue, dd);
        al.add(tav);
    }

    return al;
}%>



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
  <legend>Demo BsTypeAhead</legend>
  
  <label id="lbl">Test BsTypeAhead (MultiValue=false)<input id="txtta" type="Text"></label> <br> <br> <label id="lbl2">Test BsTypeAhead
    (MultiValue=true)<input id="txtta2" type="Text">
  </label> <br>

  <p>
    <br>
    <br>
    <%@ include file="htmlElementPropertyEditor.jsp"%>
</fieldset>

<%@ include file="include/htmlFooter.jspf"%>


