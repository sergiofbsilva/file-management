package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.presentationTier.pages.ManageDirProperties;
import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.BaseTheme;

public class DirDetails extends NodeDetails {

    public DirDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem, operationsVisible, infoVisible);
	addVisibleProperty("countFiles");
    }

    public DirDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem, true, true);
    }

    @Override
    public DirNode getNode() {
	return (DirNode) super.getNode();
    }

    private Button createRenameDirLink() {
	Button btRenameDir = new Button(getMessage("label.rename"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		final RenameDirWindow renameDirWindow = new RenameDirWindow(getNodeItem());
		getWindow().addWindow(renameDirWindow);
	    }
	});

	btRenameDir.setStyleName(BaseTheme.BUTTON_LINK);
	return btRenameDir;
    }

    @Override
    public String getDeleteDialogMessage() {
	if (getNode().hasAnyChild()) {
	    return getMessage("label.node.notempty.delete.message", getNode().getDisplayName());
	} else {
	    return super.getDeleteDialogMessage();
	}
    }

    private Button createDeleteDirLink() {
	Button btDeleteDir = new Button(getMessage("label.delete"), new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		showDeleteDialog();
	    }
	});

	btDeleteDir.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteDir;
    }

    private boolean userIsManager() {
	return UserView.getCurrentUser().hasRoleType(RoleType.MANAGER);
    }

    @Override
    public void updateOperations() {
	if (getNode().hasParent() && getNode().isWriteGroupMember()) {
	    addOperation(createRenameDirLink());
	    addOperation(createShareLink());
	    addOperation(createDeleteDirLink());
	} else {
	    if (getNode().isShared()) {
		addOperation(createDeleteDirLink());
	    }
	}
	if (userIsManager()) {
	    addOperation(manageDirProperties());
	}
    }

    private Component manageDirProperties() {
	final Button btManage = new Button("Propriedades (manager)");
	btManage.setStyleName(BaseTheme.BUTTON_LINK);
	btManage.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		EmbeddedApplication.open(getApplication(), ManageDirProperties.class, getNode().getExternalId());
	    }
	});
	return btManage;
    }
}
