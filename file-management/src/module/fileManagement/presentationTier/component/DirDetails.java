package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.DirNode;

import org.vaadin.dialogs.ConfirmDialog;

import pt.ist.fenixWebFramework.services.Service;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class DirDetails extends NodeDetails {
    
    
    public DirDetails(DirNode dirNode, boolean operationsVisible) {
	super(dirNode,operationsVisible);
	addVisibleProperty("countFiles");
    }
    
    public DirDetails(DirNode dirNode) {
	this(dirNode,true);
    }
    
    @Override
    public DirNode getNode() {
        return (DirNode) super.getNode();
    }
    
    @Service
    private void renameDir(final String newName) {	
	DirNode dirNode = (DirNode) getNode();
	dirNode.setDisplayName(newName);
    }
    
    private Button createRenameDirLink() {
	Button btDeleteDirLink = new Button(getMessage("label.rename"), new Button.ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		final Window window = new Window(getMessage("label.rename"));
//		TransactionalForm form = new TransactionalForm(FileManagementSystem.getBundle());
//		final DomainItem item = new DomainItem((DirNode)getFileNode());
//		form.setItemDataSource(item);
//		form.setVisibleItemProperties(new Object[] { "displayName" });
//		form.addSubmitButton();
//		window.addComponent(form);
		VerticalLayout windowLayout = new VerticalLayout();
		
		final TextField txtDirName = new TextField();
		windowLayout.addComponent(txtDirName);
		final Button btSave = new Button(getMessage("label.save"));
		final Button btClose = new Button(getMessage("label.close"));
		btSave.addListener(new ClickListener() {
		    
		    @Override
		    public void buttonClick(ClickEvent event) {
				renameDir((String) txtDirName.getValue());
				getDocumentBrowse().refresh();
				getWindow().removeWindow(window);
		    }
		});
		btClose.addListener(new ClickListener() {
		    
		    @Override
		    public void buttonClick(ClickEvent event) {
			getWindow().removeWindow(window);
		    }
		});
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		
		buttonsLayout.addComponent(btSave);
		buttonsLayout.addComponent(btClose);
		windowLayout.addComponent(buttonsLayout);
		
		window.addComponent(windowLayout);
		getWindow().addWindow(window);
	    }
	});
	
	btDeleteDirLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteDirLink;  
    }
    
    
    private Button createDeleteDirLink() {
	Button btDeleteDir = new Button(getMessage("label.delete"), new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		final DirNode dirNode = getNode();
		final String windowTitle = String.format("Apagar Directoria - %s", dirNode.getDisplayName());
		final String message = String.format("Deseja apagar a directoria %s ? ",dirNode.getDisplayName());
		ConfirmDialog.show(getWindow(), windowTitle, message , "Sim", "Não", new ConfirmDialog.Listener() {
		    
		    @Override
		    public void onClose(ConfirmDialog dialog) {
			if (dialog.isConfirmed()) {
			    dirNode.trash();
			    getDocumentBrowse().refresh();
			}
		    }
		});
	    }
	});
	
	btDeleteDir.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteDir;
    }

    @Override
    public void updateOperations() {
	addOperation(createRenameDirLink());
	if (!getNode().isSharedFilesDirectory()) {
	    addOperation(createDeleteDirLink());
	}
	addOperation(createShareLink());
    }
}
