package module.fileManagement.presentationTier.component;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.Metadata;
import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;
import module.fileManagement.presentationTier.data.DocumentContainer;
import module.fileManagement.presentationTier.data.FMSFieldFactory;
import module.fileManagement.presentationTier.data.TemplateItem;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;

public class MetadataPanel extends Panel {

    private final Select selectTemplate;
    protected TemplateItem metadataTemplateItem;
    private final TransactionalForm metadataForm;
    private final DocumentContainer documentContainer;
    private final ObjectHintedProperty<Collection> selectedDocuments;

    private void setMetadataItem(MetadataTemplate template) {
	if (template == null) {
	    metadataForm.setEnabled(false);
	    metadataForm.setItemDataSource(null);
	    metadataTemplateItem = null;
	    return;
	}
	if (metadataTemplateItem == null) {
	    selectTemplate.setEnabled(true);
	    metadataTemplateItem = new TemplateItem(template, documentContainer, selectedDocuments);
	    metadataForm.setItemDataSource(metadataTemplateItem);
	    metadataForm.setVisibleItemProperties(metadataTemplateItem.getVisibleItemProperties());
	    metadataForm.setImmediate(true);
	    metadataForm.setWriteThrough(true);
	    selectedDocuments.addListener(new ValueChangeListener() {

		@Override
		public void valueChange(ValueChangeEvent event) {
		    metadataTemplateItem.discard();
		}
	    });

	} else {
	    metadataTemplateItem.setValue(template);
	}

    }

    @SuppressWarnings("serial")
    public MetadataPanel(DocumentContainer documentContainer, ObjectHintedProperty<Collection> selectedDocuments) {
	super("Metadata");
	this.documentContainer = documentContainer;
	this.selectedDocuments = selectedDocuments;

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
	addComponent(selectTemplate);
	metadataForm = new TransactionalForm(FileManagementSystem.BUNDLE);
	metadataForm.setFormFieldFactory(new FMSFieldFactory(FileManagementSystem.BUNDLE));
	addComponent(metadataForm);
    }

    public void selectDocuments(Collection<Document> documents) {
	selectTemplate.setEnabled(true);
	if (selectTemplate.getValue() == null) {
	    Collection<Document> selectedDocuments = documents;
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

}
