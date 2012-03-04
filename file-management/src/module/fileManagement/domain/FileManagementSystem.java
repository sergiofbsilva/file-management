/*
 * @(#)FileManagementSystem.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.domain;

import java.util.ResourceBundle;

import myorg.domain.MyOrg;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.VaadinFrameworkLogger;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class FileManagementSystem extends FileManagementSystem_Base {

    private static FileManagementSystem system;
    public static final String BUNDLE = "resources.FileManagementResources";

    private static final Logger logger = Logger.getLogger(FileManagementSystem.class);

    static {
	VaadinFrameworkLogger.getLogger().setLevel(Level.ALL);
    }

    private FileManagementSystem() {
	super();
    }

    public static String getMessage(final String key, String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(BUNDLE, key, args);
    }

    public MetadataKey getMetadataKey(String keyValue) {
	for (MetadataKey key : getMetadataKeys()) {
	    if (key.getKeyValue().equals(keyValue)) {
		return key;
	    }
	}
	return null;
    }

    public static FileManagementSystem getInstance() {
	if (system == null) {
	    system = getOrCreateInstance();
	}
	return system;

    }

    @Service
    private static FileManagementSystem getOrCreateInstance() {
	final MyOrg myorg = MyOrg.getInstance();
	if (!myorg.hasFileManagementSystem()) {
	    final FileManagementSystem fileManagementSystem = new FileManagementSystem();
	    myorg.setFileManagementSystem(fileManagementSystem);

	}
	return myorg.getFileManagementSystem();
    }

    public static ResourceBundle getBundle() {
	return ResourceBundle.getBundle(BUNDLE);
    }

    public static Logger getLogger() {
	return logger;
    }

    // public static void goTo(Class clazz) {
    //
    // for(Annotation anot : clazz.getAnnotations()) {
    // if (anot.annotationType().equals(EmbeddedComponent.class)) {
    // EmbeddedComponent embedded = (EmbeddedComponent) anot;
    //
    // }
    // }
    // }

    public static void showException(Application app, DomainException e) {
	Window.Notification notif = new Window.Notification("Operação não permitida", e.getMessage(),
		Window.Notification.TYPE_TRAY_NOTIFICATION);
	notif.setDelayMsec(-1);
	app.getMainWindow().showNotification(notif);
    }

}
