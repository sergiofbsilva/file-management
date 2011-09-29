package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.DownloadUtil;
import module.fileManagement.presentationTier.pages.DocumentExtendedInfo;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.BaseTheme;

public class FileDetails extends NodeDetails {

    public FileDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem, operationsVisible, infoVisible);
	addVisibleProperty("document.versionNumber");
	addVisibleProperty("document.lastModifiedDate");
	addVisibleProperty("document.typology");
    }

    public FileDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem, true, true);
    }

    @Override
    public FileNode getNode() {
	return (FileNode) super.getNode();
    }

    public Button createExtendedInfoLink() {
	Button btExtendedInfoLink = new Button(getMessage("label.extended.info"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		final String fragment = "DocumentExtendedInfo-" + getNode().getExternalId();
		((EmbeddedApplication) getApplication()).open(DocumentExtendedInfo.class, fragment);
	    }
	});
	btExtendedInfoLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btExtendedInfoLink;
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
		showDeleteDialog();
	    }
	});
	btDeleteFileLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteFileLink;
    }

    @Override
    public void updateOperations() {
	addOperation(createExtendedInfoLink());
	addOperation(createDownloadLink());
	if (getNode().isWriteGroupMember()) {
	    addOperation(createShareLink());
	    addOperation(createDeleteFileLink());
	} else {
	    if (getNode().isShared()) {
		addOperation(createDeleteFileLink());
	    }
	}
    }
}
