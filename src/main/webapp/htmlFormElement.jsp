<!-- Copied from OATemplate project by OABuilder 03/03/24 06:30 AM -->
<%@ include file="include/jspHeader.jspf"%>

<%

  formId = "baseComponent";
  form = oasession.getRequestForm(formId);
   
  oasession.setDebug(true); //qqqqqqq

  if (form == null) {
      form = oasession.createRequestForm(formId);
      HtmlFormElement comp = new HtmlFormElement("txt");
      comp.setLabelId("txtlbl");
      comp.setForwardUrl("test");
      comp.setValue("this is the text field value");
      // comp.setHeight("10em");
      comp.setDataList(Arrays.asList("oneA", "twoA", "threeA"));
      
      // comp.addClass("oaTextAreaMessage");
      // comp.addStyle("padding", "122px 85px");
      // comp.addStyle("font-style", "italic");
      
      
      // comp.setFocus(true);
      // comp.setEnabled(false);
      // comp.setVisible(false);
      // comp.setFloatLabel("Float label for txt");
      // comp.setRequired(true);
      // comp.setAjaxSubmit(true);
      comp.setToolTip("tooltip text for txt component is here");
      // comp.setAjaxSubmit(true);
      form.add(comp);

      comp = new HtmlFormElement("div");
      comp.setOverflow(HtmlElement.OverflowType.Auto);
      comp.setHeight("150px");
      comp.setWidth("150px");
      form.add(comp);

      
      comp = new HtmlFormElement("chk");
      //comp.setEnabled(false);
      //comp.setRequired(true);
      form.add(comp);

      HtmlFormElement compx = new HtmlFormElement("span");
      comp.setEventType(HtmlFormElement.OnClick);
      comp.setSubmitsValue(false);
      comp.setForwardUrl("baseComponent.jsp?hey=test");
      comp.setConfirmMessage("Are you sure, this is a te4st");
      comp.setCursor(HtmlFormElement.NotAllowed);
      form.add(comp);
      
      
// button is 'click'      
      /*
      comp = new BaseComponent("cmdTest") {
          protected String getEventNameForSubmitting() {
      return "click";
          }
          int cnt = 0;
          public void onSubmit2(OAFormSubmitEvent formSubmitEvent) {
      if (formSubmitEvent.getSubmitComponent() == this) {
          getForm().getComponent("txt").setValue("after ajax #"+(++cnt));
      }
          }
      };
      comp.setAjaxSubmit(true);
      comp.setValue("Button for Ajax");
      form.add(comp);
      */
      
      
      
      /* 
      final OAForm fx = form;
      OAProcess p = new OAProcess() {
         public void run() {
     for (int i=1; i<4; i++) {
       try {
           Thread.sleep(1000);
           setCurrentStep(i);
       }
       catch (Exception e) {
       }
     }
     done();
     fx.removeProcess(this);
         }
      };
      p.setName("test process");
      p.setSteps("Running Step#1", "Running Step2", "Running Step3");
      p.setCanCancel(true);
      form.addProcess(p);
      Thread t = new Thread(p);
      t.start();
      */
      
      // form.addPopupMessage("popup msg here");
  }

//qqqqqqqqqqqqq autocomplete, and other attributes
%>



<%@ include file="include/htmlHeader.jspf"%>

<style>

body {
    margin-left: 25px;
} 
label {
    margin-right: 15px;
}
#div {
    border: solid thin;
}
</style>

<a href="oareset.jsp">OA Reset</a>

<fieldset>
    <legend>Sample Form Components</legend>

<p>
<label id="txtlbl" for="txt">Text</label><input type="text" id="txt" accesskey="T"><br>
<label id="txtlbl2" for="txt2">Text2</label><input type="text" id="txt2" name="txt" list=animals><br>
<label id="txtlbl3" for="txt3">Text3</label><input type="text" id="txt3" name="txt3" inputmode="lowerCase"><br>

 <datalist id=animals>
  <option value="Cat">
  <option value="Dog">
  <option value="Mouse">
 </datalist>

<div id="div">
<%= OAString.getDummyText(750, 500, 900) %>
</div>



<label for="chk"><input type="checkbox" id="chk">Checkbox</label><br>

<label for="pw">Password<input type="password" id="pw" name="pw"></label><br>

<label for="rad">Radio</label>
<input type="radio" id="rad"><br>


<label for="img">Image</label>
<input type="image" id="img"><br>

<label for="num">Number</label>
<input type="number" id="num" name="txtNum"><br>

<label for="file">File</label>
<input type="file" id="file"><br>

<label>Button
<input type="button" id="cmdTest" value="Test Button w Ajax to changed txt"></label><br>

<label for="reset">Reset</label>
Reset: <input type="reset" id="reset"><br>

<label for="submit">Submit</label>
<input type="submit" id="submit" value="Click to Submit"><br>

<span id="span">SPAN here</span><br>

<legend>Hidden Component legend</legend>

<input type="hidden"><br>

<legend>Misc Components</legend>
<label for="color">Color</label>
<input type="color" id="color"><br>

<label for="date">Date</label>
<input type="date" id="date"><br>

<label for="datetimelocla">DateTime Local</label>
<input type="datetime-local"><br>

<label for="email">Email</label>
<input type="email" id="email"><br>

<label for="month">Month</label>
<input type="month" id="month"><br>

<label for="range">Range</label>
<input type="range" id="range"><br>

<label for="search">Search</label>
<input type="search" id="search"><br>

<label for="tel">Telephone</label>
<input type="tel" id="tel"><br>

<label for="time">Time</label>
<input type="time" id="time"><br>

<label for="url">URL</label>
<input type="url" id="url"><br>

<label for="week">Week</label>
<input type="week" id="week"><br>

<label for="select">Select</label>
<select name="select" id="select" >
  <option value="volvo">Volvo</option>
  <option value="saab">Saab</option>
  <option value="mercedes">Mercedes<hr></option>
  <option value="audi">Audi</option>
  <option value="" selected>pick auto</option>
</select>
<br>
<p>

<label for="selectM">Select Multiple
<select id="selectM" name="selectM" multiple>
  <option value="volvo">Volvo</option>
  <option value="saab">Saab<hr></option>
  <option value="mercedes">Mercedes</option>
  <option value="audi">Audi</option>
  <option value="" selected>NONE of the above</option>
</select>
</label>
<br>


<label for="rangex">Range</label>
<input type="range" id="rangex" name="rangex" min="0" max="10">
<br>

<label for="txta">TextArea</label>
<textarea id="txta" name="txta" rows="4" cols="80" wrap="soft" placeholder="text area here ...">
</textarea><br>

<label for="">Progress</label>
<progress id="progress" value="32" max="100"> 32% </progress><br>


<label for="meter">Meter</label>
<meter id="meter" value="2" min="0" max="10">2 out of 10</meter><br>


<label for="select2">Select2</label>
<select name="select2" id="select2">
  <optgroup label="Swedish Cars">
    <option value="volvo">Volvo</option>
    <option value="saab" selected>Saab</option>
  </optgroup>
  <optgroup label="German Cars">
    <option label="MERCEDES label disabled" disabled value="mercedes">Mercedes</option>
    <option value="audi">Audi</option>
  </optgroup>
</select>

<p>
<figure>
  <img src="icon.gif" alt="AltNameForImage">
  <figcaption>This is the figcaption</figcaption>
</figure>


</fieldset>

<p>

<label for="ice-cream-choice">Choose a flavor
<input list="ice-cream-flavors" id="ice-cream-choice" name="ice-cream-choice">
<datalist id="ice-cream-flavors">
    <option value="Chocolate">
    <option value="Coconut">
    <option value="Mint">
    <option value="Strawberry">
    <option value="Vanilla">
</datalist>
</label>

<p>

<label for="tick">Tip amount
<input type="range" list="tickmarks" min="0" max="100" id="tick" name="tick" />
<datalist id="tickmarks">
  <option value="0"></option>
  <option value="10"></option>
  <option value="20"></option>
  <option value="30"></option>
</datalist>
</label>

<p>

<label for="colors">Pick a color (preferably a red tone)
<input type="color" list="redColors" id="colors" />
<datalist id="redColors">
  <option value="#800000"></option>
  <option value="#8B0000"></option>
  <option value="#A52A2A"></option>
  <option value="#DC143C"></option>
</datalist>
</label>
<p>

<br>
<label>
 Animal
 <input name=animal list=animals>
</label>
<datalist id=animals>
 <label>
  or select from the list:
  <select name=animal>
   <option value="">
   <option>Cat
   <option>Dog
  </select>
 </label>
</datalist>


<%@ include file="include/htmlFooter.jspf"%>


