// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.view;

import java.net.URL;

import javax.swing.*;

import com.viaoa.appstore.resource.Resource;

/**
 *  This is used as a base Frame for dialogs that
 *  are used before the main Frame is displayed.
 */
public class DummyFrame extends JFrame {
	public DummyFrame() {
        this.setIconImage(Resource.getJarIcon(Resource.getValue(Resource.IMG_AppClientIcon)).getImage());
	}
}

