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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.VaadinFrameworkLogger;

import com.vaadin.Application;
import com.vaadin.ui.Window;

import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.MetadataTemplateRule;
import module.fileManagement.domain.metadata.StringMetadata;
import module.fileManagement.presentationTier.action.OrganizationModelPluginAction.FileRepositoryView;
import module.fileManagement.tools.FilenameTemplate;
import module.organization.presentationTier.actions.OrganizationModelAction;

import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class FileManagementSystem extends FileManagementSystem_Base implements ModuleInitializer {

    private static FileManagementSystem system;
    public static final String BUNDLE = "resources.FileManagementResources";

    private static final Logger logger = Logger.getLogger(FileManagementSystem.class);

    public static final FilenameTemplate FILENAME_TEMPLATE = new FilenameTemplate();

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
	return getMetadataKey(keyValue, Boolean.FALSE, StringMetadata.class);
    }

    public MetadataKey getMetadataKey(String keyValue, final Class<? extends Metadata> classType) {
	return getMetadataKey(keyValue, Boolean.FALSE, classType);
    }

    public MetadataKey getMetadataKey(final String keyValue, final Boolean reserved, final Class<? extends Metadata> classType) {
	for (MetadataKey key : getMetadataKeys()) {
	    if (key.getKeyValue().equals(keyValue) && key.getMetadataValueType().equals(classType)
		    && reserved.equals(key.getReserved())) {
		return key;
	    }
	}
	return null;
    }

    public MetadataTemplate getMetadataTemplate(final String name) {
	for (MetadataTemplate template : getMetadataTemplatesSet()) {
	    if (template.getName().equals(name)) {
		return template;
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

    public static String getNewDisplayName(String displayName, final String fileName) {
	final String[] fileNameParts = StringUtils.split(fileName, '.');
	final String[] displayNameParts = StringUtils.split(displayName, '.');
	if (fileNameParts.length == 2) {
	    displayName = String.format("%s.%s", displayNameParts.length == 2 ? displayNameParts[0] : displayName,
		    fileNameParts[1]);
	}
	return displayName;
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

    public static String getBundleName() {
	return BUNDLE;
    }

    public static Logger getLogger() {
	return logger;
    }

    public static void showException(Application app, DomainException e) {
	Window.Notification notif = new Window.Notification(FileManagementSystem.getMessage("label.operation.not.allowed"),
		e.getMessage(), Window.Notification.TYPE_TRAY_NOTIFICATION);
	notif.setDelayMsec(-1);
	app.getMainWindow().showNotification(notif);
    }

    public static void showWarning(Application app, final String message) {
	show(app, getMessage("label.operation.not.allowed"), message, Window.Notification.TYPE_WARNING_MESSAGE);
    }

    public static void show(Application app, final String caption, final String message, int type) {
	Window.Notification notif = new Window.Notification(caption, type);
	notif.setDescription(message);
	notif.setDelayMsec(5000);
	notif.setPosition(Window.Notification.POSITION_TOP_RIGHT);
	app.getMainWindow().showNotification(notif);
    }

    public void setMetadataRulesReadOnlyFalse(FileManagementSystem root) {
	for (MetadataTemplate template : root.getMetadataTemplates()) {
	    for (MetadataTemplateRule rule : template.getRule()) {
		if (rule.getReadOnly() == null) {
		    rule.setReadOnly(Boolean.FALSE);
		}
	    }
	}
    }

    @Override
    public void init(MyOrg root) {
	setMetadataRulesReadOnlyFalse(root.getFileManagementSystem());
    }

    static {
	initDocumentOrganizationModelView();
    }

    private static void initDocumentOrganizationModelView() {
	OrganizationModelAction.partyViewHookManager.register(new FileRepositoryView());
    }

}
