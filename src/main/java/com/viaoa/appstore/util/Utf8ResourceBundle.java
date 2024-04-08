// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.util;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Utf8ResourceBundle {

	public static final ResourceBundle getBundle(String baseName) {
		ResourceBundle bundle = ResourceBundle.getBundle(baseName);
		return createUtf8PropertyResourceBundle(bundle);
	}

	public static final ResourceBundle getBundle(String baseName, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
		return createUtf8PropertyResourceBundle(bundle);
	}

	public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
		ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, loader);
        ResourceBundle rb = createUtf8PropertyResourceBundle(bundle);
        return rb;
	}
	
	
	private static ResourceBundle createUtf8PropertyResourceBundle(ResourceBundle bundle) {
		if (!(bundle instanceof PropertyResourceBundle)) return bundle;
		return new Utf8PropertyResourceBundle((PropertyResourceBundle) bundle);
	}

	private static class Utf8PropertyResourceBundle extends ResourceBundle {
		PropertyResourceBundle bundle;

		private Utf8PropertyResourceBundle(PropertyResourceBundle bundle) {
			this.bundle = bundle;
		}

		public Enumeration getKeys() {
			return bundle.getKeys();
		}

		// this gets internally called by getObject(key), which will check in parents also (if not found), by calling handleGetObject(key)
		protected Object handleGetObject(String key) {
			String value = (String) bundle.getObject(key);  // dont call bundle.handleGetObject(key)
			if (value == null) return null;
			try {
				return new String(value.getBytes("ISO-8859-1"), "UTF-8");
			} 
			catch (UnsupportedEncodingException e) {
				return null;
			}
		}

	}
}
