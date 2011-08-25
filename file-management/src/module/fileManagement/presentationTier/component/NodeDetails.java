package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.SharedDirNode;
import module.fileManagement.domain.SharedFileNode;
import module.fileManagement.presentationTier.pages.DocumentBrowse;

import org.joda.time.DateTime;

import pt.ist.bennu.ui.viewers.ViewerFactory;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.DefaultViewerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.Viewer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public abstract class NodeDetails extends Panel {

    protected AbstractFileNode absFileNode;
    protected DocumentBrowse documentBrowse;
    private Set<String> visibleProperties;
    private VerticalLayout vlOperations;

    public NodeDetails(AbstractFileNode absFileNode, boolean operationsVisible) {
	super(getMessage("label.file.details"));
	this.absFileNode = absFileNode;
	VerticalLayout content = (VerticalLayout) getContent();
	content.setSpacing(true);
	visibleProperties = new HashSet<String>();
	vlOperations = new VerticalLayout();
	vlOperations.setVisible(operationsVisible);
    }

    public NodeDetails(AbstractFileNode absFileNode) {
	this(absFileNode, true);
    }

    public Button createShareLink() {
	Button btShareLink = new Button(getMessage("document.share.title"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		getWindow().open(new ExternalResource("#DocumentShare-" + getNode().getExternalId()));
	    }
	});
	btShareLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btShareLink;
    }

    public Component updateDetails() {
	final ResourceBundle bundle = FileManagementSystem.getBundle();
	final ViewerFactory defaultViewerFactory = new DefaultViewerFactory(bundle) {
	    @Override
	    protected Viewer makeViewer(Item item, Object propertyId, Component uiContext) {
		final Property itemProperty = item.getItemProperty(propertyId);
		if (DateTime.class.isAssignableFrom(itemProperty.getType())) {
		    final Label lbl = new Label() {
			@Override
			public String toString() {
			    final DateTime datetime = (DateTime) getPropertyDataSource().getValue();
			    return datetime.toString("yyyy/MM/dd HH:mm");
			}
		    };
		    return lbl;
		}
		return super.makeViewer(item, propertyId, uiContext);
	    }
	};
	TabularViewer viewer = new TabularViewer(defaultViewerFactory);

	List<String> propertyIds = new ArrayList<String>();
	propertyIds.addAll(Arrays.asList(new String[] { "displayName", "presentationFilesize" }));
	propertyIds.addAll(visibleProperties);
	viewer.setItemDataSource(new DomainItem(getNode()), propertyIds);
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
    public static NodeDetails makeDetails(AbstractFileNode absFileNode, boolean operationsVisible) {
	if (absFileNode.isShared()) {
	    if (absFileNode.isDir()) {
		return new SharedDirDetails((SharedDirNode) absFileNode, operationsVisible);
	    }
	    if (absFileNode.isFile()) {
		return new SharedFileDetails((SharedFileNode) absFileNode, operationsVisible);
	    }
	}
	if (absFileNode.isFile()) {
	    return new FileDetails((FileNode) absFileNode, operationsVisible);
	}
	if (absFileNode.isDir()) {
	    return new DirDetails((DirNode) absFileNode, operationsVisible);
	}

	return null;
    }

    public static NodeDetails makeDetails(AbstractFileNode absFileNode) {
	return makeDetails(absFileNode, true);
    }

    public AbstractFileNode getNode() {
	return absFileNode;
    }

    public void setFileNode(final AbstractFileNode absFileNode) {
	this.absFileNode = absFileNode;
	updateFilePanel();
    }

    @Override
    public void attach() {
	super.attach();
	updateFilePanel();
    }

    public void updateFilePanel() {
	if (getNode() == null) {
	    return;
	}
	removeAllComponents();
	addComponent(updateDetails());
	addComponent(vlOperations);
	updateOperations();
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

}
