/*
 * @(#)FileSupportNodeCreation.java
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
package module.fileManagement.presentationTier.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.contents.Node;
import pt.ist.bennu.core.domain.groups.Role;
import pt.ist.bennu.core.domain.groups.UserGroup;
import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.bennu.vaadin.domain.contents.VaadinNode;
import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/fileManagementNodeCreation")
/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class FileSupportNodeCreation extends ContextBaseAction {

    @CreateNodeAction(bundle = "FILE_MANAGEMENT_RESOURCES", key = "add.node.file.management.interface", groupKey = "label.module.fileSupport")
    public final ActionForward createFileManagerInterface(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
	final Node node = getDomainObject(request, "parentOfNodesToManageId");
	final String FILEMANAGEMENT_RESOURCES = "resources.FileManagementResources";

	final VaadinNode documentHome = VaadinNode.createVaadinNode(virtualHost, node, FILEMANAGEMENT_RESOURCES,
		"label.link.documents", "DocumentHome", UserGroup.getInstance()
	/* Role.getRole(RoleType.MANAGER) */);

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.home", "DocumentHome",
		UserGroup.getInstance());

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.browse", "DocumentBrowse",
		UserGroup.getInstance());

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.webdav", "WebdavHome",
		Role.getRole(RoleType.MANAGER));

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.logs", "LogPage",
		UserGroup.getInstance());

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.search", "DocumentSearch",
		UserGroup.getInstance());

	// VaadinNode.createVaadinNode(virtualHost, documentHome,
	// FILEMANAGEMENT_RESOURCES, "label.link.old.documents",
	// "DocumentFrontPage", UserGroup.getInstance());

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.metadata.templates",
		"ManageMetadataTemplates", Role.getRole(RoleType.MANAGER));

	return forwardToMuneConfiguration(request, virtualHost, node);
    }

}
