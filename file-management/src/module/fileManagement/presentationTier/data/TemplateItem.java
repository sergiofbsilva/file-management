package module.fileManagement.presentationTier.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.vaadinframework.data.BufferedItem;
import pt.ist.vaadinframework.data.BufferedItem.BufferedProperty;
import pt.ist.vaadinframework.data.HintedProperty;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;

public class TemplateItem extends BufferedItem<MetadataKey, MetadataTemplate> implements ValueChangeListener {
	
	DocumentContainer container;
	ObjectHintedProperty<Collection> selectedDocuments;
	
	private TemplateItem(HintedProperty value) {
	    super(value);
	    setWriteThrough(true);
	}
	
	public TemplateItem(MetadataTemplate value, DocumentContainer container, ObjectHintedProperty<Collection> selectedDocumentsProperty) {
	    this(new ObjectHintedProperty<MetadataTemplate>(null, MetadataTemplate.class));
	    selectedDocuments = selectedDocumentsProperty;
	    setDocumentContainer(container);
	    addListener(this); // first null then listener then value 
	    setValue(value);
	}
	
	
	public void setDocumentContainer(DocumentContainer container) {
	    this.container = container;
	}
	
	@Override
	protected Object readPropertyValue(MetadataTemplate host, MetadataKey propertyId) {
	    final Set<String> allValues = new HashSet<String>();
	    for(Object item : selectedDocuments.getValue()) {
		final Document document = (Document) item;
		final DocumentItem documentItem = container.getItem(document);
		final Property itemProperty = documentItem.getItemProperty(propertyId);
		allValues.add((String) (itemProperty == null ? null : itemProperty.getValue()));
	    }
	    return allValues.size() == 1 ? allValues.iterator().next() : null;
	}

	@Override
	protected void writePropertyValue(MetadataTemplate host, MetadataKey propertyId, Object newValue) {
	    for(Object item : selectedDocuments.getValue()) {
		Document document = (Document) item;
		final DocumentItem documentItem = container.getItem(document);
		final Property itemProperty = documentItem.getItemProperty(propertyId);
		itemProperty.setValue(newValue);
	    }
	}

	@Override
	protected Property makeProperty(MetadataKey propertyId) {
	    Property property = new BufferedProperty(propertyId,String.class);
	    addItemProperty(propertyId,property);
	    return property;
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
	    final MetadataTemplate template = (MetadataTemplate) event.getProperty().getValue();
	    final MetadataKey templateKey = MetadataKey.getTemplateKey();
	    final Property itemProperty = getItemProperty(templateKey);
	    itemProperty.setValue(getValue().getName());
	    
	    for(MetadataKey key : getItemPropertyIds()) {
		if (!template.hasKeys(key) && !templateKey.equals(key)) {
		    removeItemProperty(key);
		}
	    }
	    for (MetadataKey key : template.getKeys()) {
		getItemProperty(key);
	    }
	}

	public Collection<?> getVisibleItemProperties() {
	    HashSet<MetadataKey> propIds = new HashSet<MetadataKey>(getItemPropertyIds());
	    propIds.remove(MetadataKey.getTemplateKey());
	    return Collections.unmodifiableCollection(propIds);
	}
}
