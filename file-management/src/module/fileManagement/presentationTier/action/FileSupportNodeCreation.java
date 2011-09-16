package module.fileManagement.presentationTier.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myorg.domain.RoleType;
import myorg.domain.VirtualHost;
import myorg.domain.contents.Node;
import myorg.domain.groups.Role;
import myorg.domain.groups.UserGroup;
import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.vaadin.domain.contents.VaadinNode;
import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/fileManagementNodeCreation")
public class FileSupportNodeCreation extends ContextBaseAction {

    @CreateNodeAction(bundle = "FILE_MANAGEMENT_RESOURCES", key = "add.node.file.management.interface", groupKey = "label.module.fileSupport")
    public final ActionForward createFileManagerInterface(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
	final Node node = getDomainObject(request, "parentOfNodesToManageId");
	final String FILEMANAGEMENT_RESOURCES = "resources.FileManagementResources";

	final VaadinNode documentHome = VaadinNode.createVaadinNode(virtualHost, node,
		FILEMANAGEMENT_RESOURCES, "label.link.documents", "DocumentHome",
		 UserGroup.getInstance()
		/*Role.getRole(RoleType.MANAGER)*/);

	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.home",
		"DocumentHome", UserGroup.getInstance());
	
	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.browse",
		"DocumentBrowse-", UserGroup.getInstance());
	
	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.logs",
		"LogPage", UserGroup.getInstance());
	
//	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.search",
//		"DocumentSearch", UserGroup.getInstance());
	
//	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.old.documents",
//		"DocumentFrontPage", UserGroup.getInstance());
	
	VaadinNode.createVaadinNode(virtualHost, documentHome, FILEMANAGEMENT_RESOURCES, "label.link.metadata.templates",
		"ManageMetadataTemplates", Role.getRole(RoleType.MANAGER));

	return forwardToMuneConfiguration(request, virtualHost, node);
    }

}
