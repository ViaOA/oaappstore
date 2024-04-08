// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.delegate;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.viaoa.appstore.control.client.ClientFrameController;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.print.PrintController;

public class JfcDelegate {
	private static ClientFrameController controlFrame;
	private static Window window;
	private static PrintController controlPrint;

	public static void setMainWindow(Window win) {
		window = win;
		OAJfcUtil.setMainWindow(win);
	}

	public static void setMainWindow(Component... comps) {
		for (int i = 0; window == null && comps != null && i < comps.length; i++) {
			window = OAJfcUtil.getWindow(comps[i]);
		}
		OAJfcUtil.setMainWindow(window);
	}

	public static Window getMainWindow() {
		if (controlFrame == null) {
			if (window != null) {
				return window;
			}
			return OAJfcUtil.getMainWindow();
		}
		return controlFrame.getFrame();
	}

	public static void setPrintController(PrintController control) {
		controlPrint = control;
	}

	public static void setFrameController(ClientFrameController cfc) {
		controlFrame = cfc;
	}

	public static Window getWindow(Component comp) {
		if (comp instanceof Window) {
			return (Window) comp;
		}
		Window win = null;
		for (; comp != null;) {
			win = SwingUtilities.getWindowAncestor(comp);
			if (win != null) {
				break;
			}
			comp = comp.getParent();
			if (comp instanceof JPopupMenu) {
				comp = ((JPopupMenu) comp).getInvoker();
			}
		}

		if (win == null) {
			win = JfcDelegate.getMainWindow();
		}
		return win;
	}

	public static PrintController getPrintController() {
		if (controlFrame == null) {
			return controlPrint;
		}
		return controlFrame.getPrintController();
	}

	public static void setProcessing(boolean b, String msg) {
		if (controlFrame == null) {
			return;
		}
		controlFrame.setProcessing(b, msg);
	}

	public static void setProcessing(final boolean b) {
		if (controlFrame == null) {
			return;
		}
		controlFrame.setProcessing(b);
	}

	public static void setStatus(String s) {
		if (controlFrame == null) {
			return;
		}
		controlFrame.setStatus(s);
	}

	public static void showError(String title, String msg) {
		OAJfcUtil.showErrorMessage(title, msg);
	}

	public static boolean showConfirm(String title, String msg) {
		return OAJfcUtil.showConfirmMessage(title, msg);
	}

	public static void showMessage(String title, String errorMsg) {
		OAJfcUtil.showMessage(title, errorMsg);
	}
}
