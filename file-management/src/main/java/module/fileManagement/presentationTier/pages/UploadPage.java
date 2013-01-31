package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import static module.fileManagement.domain.VersionedFile.FILE_SIZE_UNIT.prettyPrint;

import java.util.Collection;
import java.util.Map;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.component.MetadataPanel;
import module.fileManagement.presentationTier.component.UploadFilePanel;
import module.vaadin.ui.BennuTheme;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "UploadPage" }, args = { "contextPath" })
public class UploadPage extends CustomComponent implements EmbeddedComponentContainer {

	private MetadataPanel metadataPanel;
	private UploadFilePanel uploadFilePanel;
	private final VerticalLayout leftPanel;
	private final Label uploadDirLabel;
	private Label lblQuotaText;
	private final VerticalLayout vlMetadata;

	public Panel createQuotaPanel() {
		Panel messagePanel = new Panel();
		messagePanel.setStyleName(BennuTheme.PANEL_LIGHT);
		messagePanel.setCaption(getMessage("upload.title"));
		messagePanel.setScrollable(false);
		final Layout hlQuotaContent = new VerticalLayout();
		lblQuotaText = new Label();
		lblQuotaText.setContentMode(Label.CONTENT_TEXT);
		hlQuotaContent.addComponent(lblQuotaText);
		messagePanel.setContent(hlQuotaContent);
		return messagePanel;
	}

	private void updateQuotaLabel() {
		DirNode uploadDir = uploadFilePanel.getUploadDir();
		final String usedSpacePerc = uploadDir.getPercentOfTotalUsedSpace();
		final String usedSpace = uploadDir.getPresentationTotalUsedSpace();
		final String quota = uploadDir.getPresentationQuota();
		final String availableSpace = prettyPrint(uploadDir.getAvailableSpace());
		lblQuotaText.setValue(getMessage("upload.quota", usedSpace, usedSpacePerc, quota, availableSpace));
	}

	@SuppressWarnings("serial")
	public UploadPage() {
		uploadDirLabel = new Label("Upload para : ");

		GridLayout mainLayout = new GridLayout(2, 1);
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		leftPanel = new VerticalLayout();
		leftPanel.setSizeFull();
		leftPanel.setSpacing(true);
		leftPanel.addComponent(createQuotaPanel());
		leftPanel.addComponent(uploadDirLabel);
		mainLayout.addComponent(leftPanel, 0, 0);
		vlMetadata = new VerticalLayout();
		setCompositionRoot(mainLayout);
	}

	private void initSelectedDocuments() {
		final DirNode uploadDir = uploadFilePanel.getUploadDir();
		if (uploadDir.hasSequenceNumber()) {
			final FileNode fileNode = uploadDir.createFile();
			uploadFilePanel.selectDocument(fileNode.getDocument());
		}
	}

	public void setUploadArea(ContextPath contextPath) {
		if (uploadFilePanel != null) {
			leftPanel.removeComponent(uploadFilePanel);
		}
		uploadFilePanel = new UploadFilePanel(contextPath, this);
		uploadFilePanel.getDocumentContainer().addListener(new ItemSetChangeListener() {

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				updateQuotaLabel();
			}
		});

		updateQuotaLabel();

		leftPanel.addComponent(uploadFilePanel);

		this.metadataPanel =
				new MetadataPanel(uploadFilePanel.getDocumentContainer(), uploadFilePanel.getSelectedDocumentsProperty());
		vlMetadata.addComponent(new Label("Seleccione um ou mais ficheiros para editar"));
		vlMetadata.addComponent(metadataPanel);
		// vlMetadata.setVisible(false);
		((GridLayout) getCompositionRoot()).addComponent(vlMetadata, 1, 0);
		uploadDirLabel.setValue(uploadDirLabel.getValue() + uploadFilePanel.getUploadDir().getDisplayName());

		uploadFilePanel.addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				metadataPanel.selectDocuments((Collection<Document>) event.getProperty().getValue());
			}
		});
		initSelectedDocuments();
	}

	@Override
	public boolean isAllowedToOpen(Map<String, String> arguments) {
		return true;
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		final ContextPath contextPath = new ContextPath(arguments.get("contextPath"));
		setUploadArea(contextPath);
	}

	public void metadataPanelVisible(final boolean visible) {
		vlMetadata.setVisible(visible);
	}

	public MetadataPanel getMetadataPanel() {
		return metadataPanel;
	}
}
