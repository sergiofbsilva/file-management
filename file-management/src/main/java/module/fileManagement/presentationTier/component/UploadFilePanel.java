package module.fileManagement.presentationTier.component;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.component.UploadFileArea.FileUploadListener;
import module.fileManagement.presentationTier.component.UploadFileArea.OnFileUploadEvent;
import module.fileManagement.presentationTier.data.DocumentContainer;
import module.fileManagement.presentationTier.pages.DocumentBrowse;
import module.fileManagement.presentationTier.pages.UploadPage;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.FFDomainException;
import pt.ist.vaadinframework.EmbeddedApplication;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class UploadFilePanel extends CustomComponent implements ValueChangeNotifier {

	private final UploadFileArea uploadArea;
	private final Table documents;
	private DocumentContainer documentContainer;
	private ObjectHintedProperty<Collection> selectedDocumentsProperty;
	private ContextPath contextPath;
	private final UploadPage uploadPage;
	private final Label submittedDocumentsLabel;
	private final Label lblOr = new Label("ou");

	class UploadListener implements FileUploadListener {

		@Override
		public void uploadedFile(OnFileUploadEvent event) {
			final DirNode uploadDir = getUploadDir();
			if (uploadDir.hasSequenceNumber()) {
				seqNumberUpload(event);
			} else {
				normalUpload(event);
			}
		}

		private void normalUpload(OnFileUploadEvent event) {
			final DirNode uploadDir = getUploadDir();
			if (uploadDir != null) {
				try {
					final FileNode fileNode =
							uploadDir.createFile(event.getFile(), event.getFileName(), event.getLength(), contextPath);
					addDocument(fileNode.getDocument());
				} catch (FFDomainException ffde) {
					uploadArea.setError(ffde.getMessage());
				}
			}
		}

		@Service
		private void seqNumberUpload(OnFileUploadEvent event) {
			final Collection<Document> selectedDocument = selectedDocumentsProperty.getValue();
			assert documentContainer.size() == 1 && selectedDocument.size() == 1;
			Document document = selectedDocument.iterator().next();
			final String fileName = event.getFileName();
			final String displayName = FileManagementSystem.getNewDisplayName(document.getDisplayName(), fileName);
			document.getFileNode().get(0).addNewVersion(event.getFile(), fileName, displayName, event.getLength());
			documentContainer.getItem(document).getItemProperty("displayName").setValue(displayName);
		}
	}

	public UploadFilePanel(final UploadPage uploadPage) {
		super();
		documentContainer = new DocumentContainer();
		selectedDocumentsProperty = new ObjectHintedProperty<Collection>(new HashSet<Document>(), Collection.class);
		this.documents = createDocumentTable();

		uploadArea = new UploadFileArea();
		uploadArea.addListener(new UploadListener());

		this.uploadPage = uploadPage;
		submittedDocumentsLabel = new Label("<h3> Documentos Submetidos </h3>", Label.CONTENT_XHTML);
		submittedDocumentsLabel.setVisible(false);
	}

	public UploadFilePanel(ContextPath contextPath, final UploadPage uploadPage) {
		this(uploadPage);
		this.contextPath = contextPath;
		uploadArea.setContextPath(contextPath);
	}

	@Override
	public void attach() {
		super.attach();
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.setSpacing(true);
		vl.setMargin(true, false, false, false);
		VerticalLayout vlUpload = new VerticalLayout();

		vlUpload.setSizeFull();
		vlUpload.setSpacing(true);
		vlUpload.addComponent(uploadArea);

		HorizontalLayout hlUploadEditMetadata = new HorizontalLayout();
		final Button btFinishUpload = new Button("Finalizar Upload", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// if (!uploadPage.getMetadataPanel().isValid()) {
				// return;
				// }
				uploadPage.getMetadataPanel().commit();
				documentContainer.commit();
				EmbeddedApplication.open(getApplication(), DocumentBrowse.class, contextPath.toString());
			}
		});
		hlUploadEditMetadata.addComponent(btFinishUpload);

		Button btEditMetadata = new Button("Editar Metadata", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				uploadPage.metadataPanelVisible(true);
				lblOr.setVisible(false);
				event.getButton().setVisible(false);
			}
		});

		btEditMetadata.setStyleName(BaseTheme.BUTTON_LINK);
		hlUploadEditMetadata.setSpacing(true);
		hlUploadEditMetadata.addComponent(btFinishUpload);
		hlUploadEditMetadata.addComponent(lblOr);
		hlUploadEditMetadata.addComponent(btEditMetadata);

		vl.addComponent(uploadArea.getErrorLayout());
		vl.addComponent(vlUpload);
		vl.addComponent(submittedDocumentsLabel);
		vl.addComponent(documents);
		vl.addComponent(hlUploadEditMetadata);
		documents.addListener(new ItemSetChangeListener() {

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				submittedDocumentsLabel.setVisible(event.getContainer().size() > 0);
			}
		});
		setCompositionRoot(vl);
	}

	private Table createDocumentTable() {
		final Table table = new Table();
		table.setSizeFull();
		// table.setVisible(false);
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setImmediate(true);
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.setSortDisabled(true);
		table.setContainerDataSource(documentContainer);
		table.setPropertyDataSource(selectedDocumentsProperty);
		table.setVisibleColumns(new Object[] { "displayName" });
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

	public DocumentContainer getDocumentContainer() {
		return documentContainer;
	}

	public ObjectHintedProperty<Collection> getSelectedDocumentsProperty() {
		return selectedDocumentsProperty;
	}

	public void setContextPath(ContextPath ContextPath) {
		uploadArea.setContextPath(contextPath);
	}

	public void selectDocument(Document document) {
		if (!documents.containsId(document)) {
			documents.addItem(document);
		}
		documents.select(document);
	}

}
