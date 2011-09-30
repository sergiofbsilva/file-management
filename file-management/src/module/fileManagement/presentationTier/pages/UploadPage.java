package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import static module.fileManagement.domain.VersionedFile.FILE_SIZE_UNIT.prettyPrint;

import java.util.Collection;
import java.util.Map;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.presentationTier.component.MetadataPanel;
import module.fileManagement.presentationTier.component.UploadFilePanel;
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
import com.vaadin.ui.themes.Reindeer;

@EmbeddedComponent(path = { "UploadPage" }, args = { "contextPath" })
public class UploadPage extends CustomComponent implements EmbeddedComponentContainer {

    private MetadataPanel metadataPanel;
    private UploadFilePanel uploadFilePanel;
    private VerticalLayout leftPanel;
    // private Select selectDir;
    private Label uploadDirLabel;
    private Label lblQuotaText;

    public Panel createQuotaPanel() {
	Panel messagePanel = new Panel();
	messagePanel.setStyleName(Reindeer.PANEL_LIGHT);
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

    // public Form createSelectUploadDir() {
    // Form hl = new Form();
    //
    // final DirNode dir =
    // FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
    //
    // selectDir = new Select(getMessage("label.select.upload.dir"));
    // selectDir.setImmediate(true);
    // selectDir.setNullSelectionAllowed(false);
    // final HashSet<DirNode> dirsSet = new HashSet<DirNode>();
    // dirsSet.addAll(dir.getAllDirs());
    // DomainContainer dirs = new DomainContainer(dirsSet, DirNode.class);
    // dirs.setContainerProperties("absolutePath");
    // selectDir.setContainerDataSource(dirs);
    // selectDir.setItemCaptionPropertyId("absolutePath");
    // selectDir.addListener(new ValueChangeListener() {
    //
    // @Override
    // public void valueChange(ValueChangeEvent event) {
    // uploadArea.setUploadDir((DirNode) event.getProperty().getValue());
    // }
    // });
    //
    // hl.addField("select", selectDir);
    // return hl;
    // }

    @Override
    public void attach() {
	super.attach();
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
	// leftPanel.addComponent(createSelectUploadDir());
	leftPanel.addComponent(uploadDirLabel);
	mainLayout.addComponent(leftPanel, 0, 0);
	setCompositionRoot(mainLayout);
    }

    public void setUploadArea(ContextPath contextPath) {
	if (uploadFilePanel != null) {
	    leftPanel.removeComponent(uploadFilePanel);
	}
	uploadFilePanel = new UploadFilePanel(contextPath);
	uploadFilePanel.getDocumentContainer().addListener(new ItemSetChangeListener() {

	    @Override
	    public void containerItemSetChange(ItemSetChangeEvent event) {
		updateQuotaLabel();
	    }
	});

	updateQuotaLabel();

	uploadFilePanel.addListener(new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		metadataPanel.selectDocuments((Collection<Document>) event.getProperty().getValue());
	    }
	});
	leftPanel.addComponent(uploadFilePanel);
	this.metadataPanel = new MetadataPanel(uploadFilePanel.getDocumentContainer(),
		uploadFilePanel.getSelectedDocumentsProperty());
	((GridLayout) getCompositionRoot()).addComponent(metadataPanel, 1, 0);
	uploadDirLabel.setValue(uploadDirLabel.getValue() + uploadFilePanel.getUploadDir().getDisplayName());
	// selectDir.select(uploadArea.getUploadDir());
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	final ContextPath contextPath = new ContextPath(arguments.get("contextPath"));
	setUploadArea(contextPath);
    }
}
