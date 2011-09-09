package module.fileManagement.presentationTier.component;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.presentationTier.component.UploadFileArea.FileUploadListener;
import module.fileManagement.presentationTier.component.UploadFileArea.OnFileUploadEvent;
import module.fileManagement.presentationTier.data.DocumentContainer;
import module.vaadin.data.util.ObjectHintedProperty;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class UploadFilePanel extends CustomComponent implements ValueChangeNotifier {
    
    private final UploadFileArea uploadArea;
    private final Table documents;
    private DocumentContainer documentContainer;
//    private DocumentContainer selectedDocumentsContainer;
    private ObjectHintedProperty<Collection> selectedDocumentsProperty;
    
    
    public UploadFilePanel() {
	super();
	documentContainer = new DocumentContainer();
	selectedDocumentsProperty = new ObjectHintedProperty<Collection>(new HashSet<Document>(), Collection.class);
	this.documents = createDocumentTable();
	
	uploadArea = new UploadFileArea();
	uploadArea.addListener(new FileUploadListener() {
	    
	    @Override
	    public void uploadedFile(OnFileUploadEvent event) {
		addDocument(event.getUploadedFileNode().getDocument());
	    }
	});
    }
    
    public UploadFilePanel(DirNode dirNode) {
	this();
	uploadArea.setUploadDir(dirNode);
    }

    @Override
    public void attach() {
	super.attach();
	VerticalLayout vl = new VerticalLayout();
	vl.setSizeFull();
	vl.setSpacing(true);
	vl.setMargin(true, false, false, false);
	vl.addComponent(new Label("<h3> Documentos Submetidos </h3>", Label.CONTENT_XHTML));
	vl.addComponent(documents);
	HorizontalLayout hlUploadArea = new HorizontalLayout();
	hlUploadArea.setSizeFull();
	hlUploadArea.setSpacing(true);
	hlUploadArea.addComponent(uploadArea);
	hlUploadArea.addComponent(new Button("Finalizar Upload", new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		documentContainer.commit();
		getWindow().open(new ExternalResource("#DocumentBrowse-" + uploadArea.getUploadDir().getExternalId()));
	    }
	}));
	vl.addComponent(uploadArea.getErrorLayout());
	vl.addComponent(hlUploadArea);
	setCompositionRoot(vl);
    }

    private Table createDocumentTable() {
	final Table table = new Table();
	table.setSizeFull();
	table.setVisible(false);
	table.setSelectable(true);
	table.setMultiSelect(true);
	table.setImmediate(true);
	table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	table.setSortDisabled(true);
	table.setContainerDataSource(documentContainer);
//	table.setPropertyDataSource(selectedDocumentsContainer);
	table.setPropertyDataSource(selectedDocumentsProperty);
	table.setVisibleColumns(new Object[] {"displayName"});
//	table.addGeneratedColumn("ops", new ColumnGenerator() {
//
//	    @Override
//	    public Component generateCell(Table source, final Object itemId, Object columnId) {
//		HorizontalLayout hl = new HorizontalLayout();
//		Button button = new Button("Remover", new ClickListener() {
//		    @Override
//		    public void buttonClick(ClickEvent event) {
//			table.removeItem(itemId);
//			forceDocumentVisibility();
//		    }
//		});
//		button.setStyleName(BaseTheme.BUTTON_LINK);
//		hl.addComponent(button);
//		return hl;
//	    }
//	});
	return table;
    }

    private void forceDocumentVisibility() {
	documents.setVisible(documents.getItemIds().size() > 0);
    }

    private void addDocument(Document doc) {
	documentContainer.addItem(doc);
	forceDocumentVisibility();
    }

    public boolean hasUploadedAnyDocument() {
	return documents.getItemIds().size() > 0;
    }

    @Override
    public void addListener(ValueChangeListener listener) {
	selectedDocumentsProperty.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
	selectedDocumentsProperty.removeListener(listener);
    }
    
    public DirNode getUploadDir() {
	return uploadArea.getUploadDir();
    }
    
    public void setUploadDir(DirNode uploadDir) {
	uploadArea.setUploadDir(uploadDir);
    }
    
    public DocumentContainer getDocumentContainer() {
//	return selectedDocumentsContainer;
	return documentContainer;
    }
    
    public ObjectHintedProperty<Collection> getSelectedDocumentsProperty() {
	return selectedDocumentsProperty;
    }

}
