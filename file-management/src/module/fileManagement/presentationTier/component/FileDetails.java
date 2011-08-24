package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.DownloadUtil;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.BaseTheme;

public class FileDetails extends NodeDetails {

    public FileDetails(FileNode fileNode, boolean operationsVisible) {
	super(fileNode, operationsVisible);
	addVisibleProperty("document.versionNumber");
	addVisibleProperty("document.lastModifiedDate");
	addVisibleProperty("document.typology");
    }
    
    public FileDetails(FileNode fileNode) {
	this(fileNode, true);
    }
    
    
    @Override
    public FileNode getNode() {
	return (FileNode) super.getNode();
    }

    
    public Link createDownloadLink() {
	final String url = DownloadUtil.getDownloadUrl(getApplication(), getNode().getDocument());
	final ExternalResource externalResource = new ExternalResource(url);
	return new Link(getMessage("label.download"), externalResource);
    }
    
    public Button createDeleteFileLink() {
	Button btDeleteFileLink = new Button(getMessage("label.delete"), new Button.ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		final String windowTitle = String.format("Apagar Ficheiro - %s", getNode().getDisplayName());
		final String message = String.format("Deseja apagar o ficheiro %s ? ",getNode().getDisplayName());
		ConfirmDialog.show(getWindow(), windowTitle, message , "Sim", "Não", new ConfirmDialog.Listener() {
		    
		    @Override
		    public void onClose(ConfirmDialog dialog) {
			if (dialog.isConfirmed()) {
			    absFileNode.trash();
			    documentBrowse.removeNode(absFileNode);
			}
		    }
		});
	    }
	});
	btDeleteFileLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteFileLink;  
    }

    @Override
    public void updateOperations() {
	addOperation(createDownloadLink());
	addOperation(createDeleteFileLink());
	addOperation(createShareLink());
    }
}
