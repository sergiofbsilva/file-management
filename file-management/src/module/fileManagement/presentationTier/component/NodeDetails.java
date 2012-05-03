package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.ContextPath;
import module.fileManagement.presentationTier.component.viewers.FMSViewerFactory;
import module.fileManagement.presentationTier.pages.DocumentBrowse;
import module.fileManagement.presentationTier.pages.DocumentShare;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

public abstract class NodeDetails extends Panel {

    protected DomainItem<AbstractFileNode> nodeItem;
    protected DocumentBrowse documentBrowse;
    private Set<String> visibleProperties;
    private VerticalLayout vlOperations;
    private final boolean operationsVisible;
    private final boolean infoVisible;

    public String getDeleteDialogMessage() {
	return getMessage("label.node.delete.message", getNode().getDisplayName());
    }

    public String getDeleteDialogTitle() {
	return getMessage("label.node.delete.title", getNode().getDisplayName());
    }

    public void showDeleteDialog() {
	final String windowTitle = getDeleteDialogTitle();
	final String message = getDeleteDialogMessage();
	final String yes = getMessage("label.yes");
	final String no = getMessage("label.no");
	ConfirmDialog confirm = new DefaultConfirmDialogFactory().create(windowTitle, message, yes, no);

	ConfirmDialog.Listener listener = new ConfirmDialog.Listener() {

	    @Override
	    public void onClose(ConfirmDialog dialog) {
		if (dialog.isConfirmed()) {
		    // getNode().trash((Boolean)chkDeleteAll.getValue());
		    getNode().trash(getContextPath());
		    documentBrowse.removeNode(getNodeItem());
		}
	    }

	};
	confirm.show(getWindow(), listener, true);
    }

    public NodeDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super();
	this.nodeItem = nodeItem;
	this.operationsVisible = operationsVisible;
	this.infoVisible = infoVisible;
	addStyleName(Reindeer.PANEL_LIGHT);

	updateCaption();
	VerticalLayout content = (VerticalLayout) getContent();
	content.setSpacing(true);
	visibleProperties = new HashSet<String>();
	if (operationsVisible) {
	    vlOperations = new VerticalLayout();
	}
    }

    private void updateCaption() {
	if (infoVisible) {
	    setCaption(getMessage("label.file.details"));
	    return;
	}
	if (operationsVisible) {
	    setCaption(getMessage("label.file.operations"));
	    return;
	}
    }

    public NodeDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem, true, true);
    }

    public Button createShareLink() {
	Button btShareLink = new Button(getMessage("document.share.title"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		EmbeddedApplication.open(getApplication(), DocumentShare.class, getNode().getExternalId(), getNode().getParent()
			.getContextPath().toString());
	    }
	});
	btShareLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btShareLink;
    }

    public Component updateDetails() {
	TabularViewer viewer = new TabularViewer(FMSViewerFactory.getDefaultViewerFactory());
	List<String> propertyIds = new ArrayList<String>();
	// propertyIds.addAll(Arrays.asList(new String[] { "type",
	// "displayName", "presentationFilesize" , "visibilityGroups" }));
	propertyIds.addAll(Arrays.asList(new String[] { "type", "displayName", "presentationFilesize" }));
	propertyIds.addAll(visibleProperties);
	viewer.setItemDataSource(nodeItem, propertyIds);
	return viewer;
    }

    public String[] getVisibleProperties() {
	return visibleProperties.toArray(new String[] {});
    }

    /**
     * Factory
     * 
     * @param absFileNode
     * @return
     */
    public static NodeDetails makeDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	final AbstractFileNode absFileNode = nodeItem.getValue();
	if (absFileNode == null) {
	    return null;
	}
	if (absFileNode.isShared()) {
	    if (absFileNode.isDir()) {
		return new SharedDirDetails(nodeItem, operationsVisible, infoVisible);
	    }
	    if (absFileNode.isFile()) {
		return new SharedFileDetails(nodeItem, operationsVisible, infoVisible);
	    }
	}
	if (absFileNode.isFile()) {
	    return new FileDetails(nodeItem, operationsVisible, infoVisible);
	}
	if (absFileNode.isDir()) {
	    return new DirDetails(nodeItem, operationsVisible, infoVisible);
	}

	return null;
    }

    public static NodeDetails makeDetails(DomainItem<AbstractFileNode> nodeItem) {
	return makeDetails(nodeItem, true, true);
    }

    public AbstractFileNode getNode() {
	return nodeItem != null ? nodeItem.getValue() : null;
    }

    public void setFileNode(final DomainItem<AbstractFileNode> nodeItem) {
	this.nodeItem = nodeItem;
	updateFilePanel();
    }

    @Override
    public void attach() {
	super.attach();
	((VerticalLayout) getContent()).setMargin(false);
	updateFilePanel();
    }

    @Override
    public void removeAllComponents() {
	super.removeAllComponents();
	if (vlOperations != null) {
	    vlOperations.removeAllComponents();
	}
    }

    public void updateFilePanel() {
	removeAllComponents();

	if (getNode() == null) {
	    return;
	}
	if (infoVisible) {
	    addComponent(updateDetails());
	}
	if (operationsVisible) {
	    addComponent(vlOperations);
	    updateOperations();
	}
    }

    public abstract void updateOperations();

    public DocumentBrowse getDocumentBrowse() {
	return documentBrowse;
    }

    public void setDocumentBrowse(DocumentBrowse documentBrowse) {
	this.documentBrowse = documentBrowse;
    }

    public void addVisibleProperty(String property) {
	visibleProperties.add(property);
    }

    public void addOperation(Component component) {
	vlOperations.addComponent(component);
    }

    public DomainItem<AbstractFileNode> getNodeItem() {
	return nodeItem;
    };

    private ContextPath getContextPath() {
	return documentBrowse != null ? documentBrowse.getContextPath() : getNode().getParent().getContextPath();
    }

    public boolean isOperationsVisible() {
	return operationsVisible;
    }

    public boolean isInfoVisible() {
	return infoVisible;
    }
}
