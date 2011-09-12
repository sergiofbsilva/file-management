package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import static module.fileManagement.domain.VersionedFile.FILE_SIZE_UNIT.prettyPrint;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.Metadata;
import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;
import module.fileManagement.presentationTier.component.UploadFilePanel;
import module.fileManagement.presentationTier.data.FMSFieldFactory;
import module.fileManagement.presentationTier.data.TemplateItem;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@EmbeddedComponent(path = { "UploadPage-(.*),(.*)" })
public class UploadPage extends CustomComponent implements EmbeddedComponentContainer {
    
    private UploadFilePanel uploadArea;
    private Panel metadataPanel;
    private Select selectTemplate;
    private VerticalLayout leftPanel;
//    private Select selectDir;
    private Label uploadDirLabel;
    protected TemplateItem metadataTemplateItem;
    private TransactionalForm metadataForm;
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
	DirNode uploadDir = uploadArea.getUploadDir();
	final String usedSpacePerc = uploadDir.getPercentOfTotalUsedSpace();
	final String usedSpace = uploadDir.getPresentationTotalUsedSpace();
	final String quota = uploadDir.getPresentationQuota();
	final String availableSpace = prettyPrint(uploadDir.getAvailableSpace());
	lblQuotaText.setValue(getMessage("upload.quota", usedSpace ,usedSpacePerc,quota, availableSpace));
    }

    
//    public Form createSelectUploadDir() {
//	Form hl = new Form();
//
//	final DirNode dir = FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
//
//	selectDir = new Select(getMessage("label.select.upload.dir"));
//	selectDir.setImmediate(true);
//	selectDir.setNullSelectionAllowed(false);
//	final HashSet<DirNode> dirsSet = new HashSet<DirNode>();
//	dirsSet.addAll(dir.getAllDirs());
//	DomainContainer dirs = new DomainContainer(dirsSet, DirNode.class);
//	dirs.setContainerProperties("absolutePath");
//	selectDir.setContainerDataSource(dirs);
//	selectDir.setItemCaptionPropertyId("absolutePath");
//	selectDir.addListener(new ValueChangeListener() {
//
//	    @Override
//	    public void valueChange(ValueChangeEvent event) {
//		uploadArea.setUploadDir((DirNode) event.getProperty().getValue());
//	    }
//	});
//
//	hl.addField("select", selectDir);
//	return hl;
//    }

    @Override
    public void attach() {
	super.attach();
    }

    private void setMetadataItem(MetadataTemplate template) {
	if (template == null) {
	    metadataForm.setEnabled(false);
	    metadataForm.setItemDataSource(null);
	    metadataTemplateItem = null;
	    return;
	}
	if (metadataTemplateItem == null) {
	    selectTemplate.setEnabled(true);
	    metadataTemplateItem = new TemplateItem(template, uploadArea.getDocumentContainer(),
		    uploadArea.getSelectedDocumentsProperty());
	    metadataForm.setItemDataSource(metadataTemplateItem);
	    metadataForm.setVisibleItemProperties(metadataTemplateItem.getVisibleItemProperties());
	    metadataForm.setImmediate(true);
	    metadataForm.setWriteThrough(true);
	    uploadArea.getSelectedDocumentsProperty().addListener(new ValueChangeListener() {

		@Override
		public void valueChange(ValueChangeEvent event) {
		    metadataForm.discard();
		}
	    });

	} else {
	    metadataTemplateItem.setValue(template);
	}

    }

    @SuppressWarnings("serial")
    private Panel createMetadataPanel() {
	final Panel panel = new Panel("Metadata");

	selectTemplate = new Select("Template");
	selectTemplate.setImmediate(true);
	selectTemplate.setEnabled(false);
	selectTemplate.setContainerDataSource((Container) new DomainItem(FileManagementSystem.getInstance())
		.getItemProperty("metadataTemplates"));
	selectTemplate.setItemCaptionPropertyId("name");
	selectTemplate.addListener(new ValueChangeListener() {
	    @Override
	    public void valueChange(ValueChangeEvent event) {
		final MetadataTemplate template = (MetadataTemplate) event.getProperty().getValue();
		setMetadataItem(template);
	    }
	});
	panel.addComponent(selectTemplate);
	metadataForm = new TransactionalForm(FileManagementSystem.getBundle());
	metadataForm.setFormFieldFactory(new FMSFieldFactory(FileManagementSystem.getBundle()));
	panel.addComponent(metadataForm);
	return panel;
    }

    @SuppressWarnings("serial")
    public UploadPage() {
	uploadDirLabel = new Label();
	uploadDirLabel.setCaption("Upload para :");
	GridLayout mainLayout = new GridLayout(2, 1);
	mainLayout.setSizeFull();
	mainLayout.setSpacing(true);

	leftPanel = new VerticalLayout();
	leftPanel.setSizeFull();
	leftPanel.setSpacing(true);
	leftPanel.addComponent(createQuotaPanel());
//	leftPanel.addComponent(createSelectUploadDir());
	leftPanel.addComponent(uploadDirLabel);
	mainLayout.addComponent(leftPanel, 0, 0);
	setCompositionRoot(mainLayout);
    }

    public void setUploadArea(DirNode node) {
	if (uploadArea != null) {
	    leftPanel.removeComponent(uploadArea);
	}
	uploadArea = new UploadFilePanel(node);
	uploadArea.getDocumentContainer().addListener(new ItemSetChangeListener() {
	    
	    @Override
	    public void containerItemSetChange(ItemSetChangeEvent event) {
		updateQuotaLabel();
	    }
	});
	
	updateQuotaLabel();
	
	uploadArea.addListener(new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		selectTemplate.setEnabled(true);
		if (selectTemplate.getValue() == null) {
		    Collection<Document> selectedDocuments = (Collection<Document>) event.getProperty().getValue();
		    Collection<String> templateNames = new HashSet<String>();
		    for (Document doc : selectedDocuments) {
			Metadata templateMetadata = doc.getMetadata(MetadataKey.getTemplateKey());
			if (templateMetadata != null) {
			    templateNames.add(templateMetadata.getValue());
			}
		    }
		    final String templateName = templateNames.size() < 1 ? null : templateNames.iterator().next();
		    selectTemplate.select(templateName == null ? null : MetadataTemplate.getMetadataTemplate(templateName));
		}
	    }
	});
	leftPanel.addComponent(uploadArea);
	this.metadataPanel = createMetadataPanel();
	((GridLayout) getCompositionRoot()).addComponent(metadataPanel, 1, 0);
	uploadDirLabel.setValue(uploadArea.getUploadDir().getDisplayName());
//	selectDir.select(uploadArea.getUploadDir());
    }

    @Override
    public void setArguments(String... arguments) {
	DirNode dir = DirNode.fromExternalId(arguments[1]);
	setUploadArea(dir);
	uploadArea.setContextPath(arguments[2]);
    }
}
