// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.report.oa.sample;

import java.awt.print.PageFormat;
import java.util.logging.Logger;

import com.viaoa.appstore.report.ApplicationReport;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADate;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAString;

/**
 * Customized sales order estimate report.
 *
 * @author vvia
 */
public class SalesOrderEstimateReport extends ApplicationReport<SalesOrder> {
	private static Logger LOG = Logger.getLogger(SalesOrderEstimateReport.class.getName());

	public SalesOrderEstimateReport(Hub<SalesOrder> hubSalesOrder) {
		super(hubSalesOrder, "Order Estimate", true);
		// getJfcController().getChangeListener().addPropertyChange(hubSalesOrder, SalesOrder.P_Draft);
	}

	@Override
	protected Object getProperty(Object defaultValue, OAObject oaObj, String propertyName) {
		boolean b = (propertyName != null && propertyName.startsWith("split$"));
		if ("count$".equals(propertyName) && oaObj instanceof SalesOrderItem) {
			SalesOrderItem soi = (SalesOrderItem) oaObj;
			SalesOrder so = soi.getSalesOrder();
			if (so == null) {
				return "?";
			}
			int pos = soi.getSalesOrder().getSalesOrderItems().getPos(soi);
			if (pos < 0) {
				return "?";
			}
			return (pos + 1) + "";
		}
		if (b) {
			propertyName = propertyName.substring(6);
			return oaObj.getProperty(propertyName);
		}
		return defaultValue;
	}

	@Override
	protected String getValue(String defaultValue, OAObject obj, String propertyName, int width, String fmt, OAProperties props) {
		if (obj == null) {
			return "";
		}
		boolean b = (propertyName != null && propertyName.startsWith("split$"));
		String s = defaultValue;

		if (b) {
			SalesOrder so = ((SalesOrderItem) obj).getSalesOrder();
			if (so != null) {
				Hub<SalesOrderItem> h = so.getSalesOrderItems();
				int max = 35;
				s = OAString.lineBreak(s, max, "<br>", 0);
			}
		} else if (OAString.isNotEmpty(defaultValue) && OAString.isNotEmpty(fmt) && fmt.indexOf("###-####)") >= 0) {
			s = (String) obj.getProperty(propertyName);
			s = OAString.removeNonDigits(s);
			s = OAString.format(s, fmt);
		}

		return s;
	}

	@Override
	public void refreshDetail() {
		if (getHub() == null) {
			return;
		}
		if (isOnHold()) {
			return;
		}
		SalesOrder salesOrder = getHub().getAO();
		if (salesOrder == null) {
			String msg = "<html><body><center><h2>Sales Order Estimate Report</h2></center><p>";
			msg += "<ul><i>Please select a Sales Order.</i><p>";
			getDetailTextPane().setText(msg);
			return;
		}

		String[] errors = new String[0]; // SalesOrderDelegate.getInvalidReasons(salesOrder, false);
		if (errors != null && salesOrder.getDateSubmitted() == null) {
			String msg = "<html><body><center><h2>Sales Order Estimate Report</h2></center><p>";
			msg += "<ul><i>The following will need to be corrected before the estimate can be created</i><p>";

			for (String s : errors) {
				msg += "<li>" + s + "</li>";
			}
			msg += "</ul></body></html>";
			getDetailTextPane().setText(msg);
			return;
		}
		super.refreshDetail();
	}

	@Override
	protected void loadDefaultHtmlFiles() {
		String titleHeader = null;
		String header = null;
		String footer = null;
		String detail = null;
		try {
			titleHeader = OAFile.readTextFile(this.getClass(), "/com/cdi/report/html/custom/salesOrderEstimateTitle.html", 1024);
			header = OAFile.readTextFile(this.getClass(), "/com/cdi/report/html/custom/salesOrderEstimateHeader.html", 1024);
			footer = OAFile.readTextFile(this.getClass(), "/com/cdi/report/html/custom/salesOrderEstimateFooter.html", 1024);
			detail = OAFile.readTextFile(this.getClass(), "/com/cdi/report/html/custom/salesOrderEstimate.html", 1024 * 3);
		} catch (Exception e) {
			System.out.println("Cant read salesOrderEstimate head/detail/foot.html from report directory");
		}
		setTitleHeaderHTML(titleHeader);
		setHeaderHTML(header);
		setFooterHTML(footer);
		setDetailHTML(detail);

		setTitle("Sales Order Estimate Report");
	}

	@Override
	protected void loadPageFormat(PageFormat pageFormat) {
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		super.loadPageFormat(pageFormat);
	}

}

// Dummy classes to make the compiler happy
class SalesOrder extends OAObject {
	public Hub<SalesOrderItem> getSalesOrderItems() {
		return null;
	}

	public OADate getDateSubmitted() {
		return null;
	}
}

class SalesOrderItem extends OAObject {
	public SalesOrder getSalesOrder() {
		return null;
	}
}
