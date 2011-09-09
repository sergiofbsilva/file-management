package module.fileManagement.presentationTier.pages;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.Metadata;
import module.fileManagement.presentationTier.DownloadUtil;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.fileManagement.presentationTier.component.TabularViewer;
import module.fileManagement.presentationTier.component.viewers.FMSViewerFactory;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.HintedProperty;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "DocumentExtendedInfo-(.*)" })
public class DocumentExtendedInfo extends CustomComponent implements EmbeddedComponentContainer {
    private final GridLayout mainGrid;
    private FileNode fileNode;

    @Override
    public void setArguments(String... args) {
	String externalId = args[1];
	fileNode = Document.fromExternalId(externalId);
    }

    public void update() {
	mainGrid.addComponent(createExtendedInfo(), 0, 0);
	mainGrid.addComponent(createDocumentOperations(), 1, 0);
    }

    public DocumentExtendedInfo() {
	final VerticalLayout vl = new VerticalLayout();
	vl.setSizeFull();
	vl.setSpacing(true);
	vl.setMargin(false, false, false, true);
	mainGrid = new GridLayout(2, 1);
	mainGrid.setSizeFull();
	mainGrid.setSpacing(true);
	vl.addComponent(new Label("<h3>Documento</h3>", Label.CONTENT_XHTML));
	vl.addComponent(mainGrid);
	setCompositionRoot(vl);
    }

    @Override
    public void attach() {
	super.attach();
	update();
    }

    private Component createDocumentOperations() {
	return NodeDetails.makeDetails(new DomainItem<AbstractFileNode>(fileNode), true, false);
    }

    private Component createExtendedInfo() {
	final VerticalLayout vlayout = new VerticalLayout();
	vlayout.setSpacing(true);

	final DomainItem<FileNode> item = new DomainItem<FileNode>(fileNode);
	vlayout.addComponent(createInfoPanel(item));
	vlayout.addComponent(createMetadataInfoPanel(item));
	return vlayout;
    }

    private List<Component> getVersionsEntries(DomainItem<FileNode> item) {
	final List<Component> versions = new ArrayList<Component>();
	final HintedProperty recentMetadata = (HintedProperty) item.getItemProperty("document.versions");
	final Collection<Metadata> itemIds = (Collection<Metadata>) recentMetadata.getValue();
	int index = itemIds.size();
	for (Metadata metadata : itemIds) {
	    final Label lblValue = new Label();
	    lblValue.setCaption(String.format("Versão (%d) info", index--));
	    lblValue.setValue(String.format("%s em %s", metadata.getValue(),
		    metadata.getTimestamp().toString("dd/MM/yyyy HH:mm:ss")));
	    versions.add(lblValue);
	}
	return versions;
    }

    private Component createMetadataInfoPanel(DomainItem<FileNode> item) {
	final HintedProperty recentMetadata = (HintedProperty) item.getItemProperty("document.recentMetadata");
	final DomainContainer<Metadata> metadataContainer = new DomainContainer<Metadata>(
		(Collection<Metadata>) recentMetadata.getValue(), Metadata.class);
	if (metadataContainer.size() > 0) {
	    final Table table = new Table();
	    metadataContainer.setContainerProperties("keyValue", "value");
	    table.setContainerDataSource(metadataContainer);
	    table.setVisibleColumns(new Object[] { "keyValue", "value" });
	    table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	    table.setCaption("Metadata");
	    return table;
	} else {
	    return new Label("Documento sem metadata");
	}
    }

    private Component createInfoPanel(DomainItem<FileNode> item) {
	TabularViewer viewer = new TabularViewer(FMSViewerFactory.getInstance());
	List<String> propertyIds = new ArrayList<String>();
	item.addItemProperty("url", new ObjectProperty<URL>(
		DownloadUtil.getDownloadUrl(getApplication(), fileNode.getDocument()), URL.class));
	propertyIds.addAll(Arrays.asList(new String[] { "displayName", "url", "document.versionNumber", "presentationFilesize",
		"document.lastModifiedDate", "parent.displayName" }));
	viewer.setItemDataSource(item, propertyIds);
	for (Component versionEntry : getVersionsEntries(item)) {
	    viewer.addLine(versionEntry);
	}
	return viewer;
    }
    
}
