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

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.StringMetadata;
import module.fileManagement.presentationTier.action.OrganizationModelPluginAction.FileRepositoryView;
import module.fileManagement.tools.FilenameTemplate;
import module.organization.domain.Party;
import module.organization.presentationTier.actions.OrganizationModelAction;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.User;
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
	Window.Notification notif = new Window.Notification(FileManagementSystem.getMessage("label.operation.not.allowed"),
		e.getMessage(), Window.Notification.TYPE_TRAY_NOTIFICATION);
	notif.setDelayMsec(-1);
	app.getMainWindow().showNotification(notif);
    }

    private void checkUserPartyRelation(MyOrg myOrg) {
	if (true) return ; 
	VaadinFrameworkLogger.getLogger().debug("Start processing party <-> dirNode relation");
	for (User user : myOrg.getUser()) {
	    Party person = user.getPerson();
	    if (person != null) {
		if (!person.hasFileRepository()) {
		    person.setFileRepository(user.getFileRepository());
		} else {
		    VaadinFrameworkLogger.getLogger().debug("Assume that if a person already has root dir everybody has");
		    break;
		}
	    }
	}
	VaadinFrameworkLogger.getLogger().debug("Stop processing party <-> dirNode relation");
    }

    private void checkMetadataKeyReservedValue(FileManagementSystem root) {
	final Set<String> reserved = new HashSet<String>();
	reserved.add(MetadataKey.FILENAME_KEY_VALUE);
	reserved.add(MetadataKey.TEMPLATE_KEY_VALUE);
	reserved.add(MetadataKey.DOCUMENT_NEW_VERSION_KEY);
	reserved.add(MetadataKey.SAVE_ACCESS_LOG);
	for (MetadataKey metadataKey : root.getMetadataKeys()) {
	    if (metadataKey.getReserved() == null) {
		final boolean contains = reserved.contains(metadataKey.getKeyValue());
		metadataKey.setReserved(new Boolean(contains));
		metadataKey.setMetadataValueType(StringMetadata.class);
		VaadinFrameworkLogger.getLogger().warn(
			String.format("set key %s, reserved : %s", metadataKey.getKeyValue(), contains));
	    }
	    if (metadataKey.getMetadataValueType() == null) {
		metadataKey.setMetadataValueType(StringMetadata.class);
		VaadinFrameworkLogger.getLogger().warn(String.format("set type to String for key %s", metadataKey.getKeyValue()));
	    }
	}
    }

    @Override
    public void init(MyOrg root) {
	checkUserPartyRelation(root);
	checkMetadataKeyReservedValue(root.getFileManagementSystem());
    }

    static {
	initDocumentOrganizationModelView();
    }

    private static void initDocumentOrganizationModelView() {
	OrganizationModelAction.partyViewHookManager.register(new FileRepositoryView());
    }

}
