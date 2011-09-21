package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

public class DirDetails extends NodeDetails {
    
    
    public DirDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem,operationsVisible, infoVisible);
	addVisibleProperty("countFiles");
    }
    
    public DirDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem,true,true);
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

    @Override
    public void updateOperations() {
	if (getNode().hasParent() && getNode().isWriteGroupMember()) {
	    addOperation(createRenameDirLink());
	    addOperation(createDeleteDirLink());
	    addOperation(createShareLink());
	} else {
	    if (getNode().isShared()) {
		addOperation(createDeleteDirLink());
	    }
	}
    }
}
