package module.fileManagement.presentationTier.data;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.Metadata;
import module.fileManagement.domain.MetadataKey;
import pt.ist.vaadinframework.data.BufferedProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

public class DocumentItem extends DomainItem<Document> {
    public class MetadataKeyProperty extends AbstractProperty {
	private final MetadataKey key;

	public MetadataKeyProperty(MetadataKey key) {
	    this.key = key;
	}

	@Override
	public Object getValue() {
	    Document document = DocumentItem.this.getValue();
	    if (document != null) {
		Metadata metadata = document.getMetadataRecentlyChanged(key);
		return metadata != null ? metadata.getValue() : null;
	    }
	    return null;
	}

	@Override
	public void setValue(Object newValue) {
	    Document document = DocumentItem.this.getValue();
	    document.addMetadata(key, (String) newValue);
	}

	@Override
	public Class<?> getType() {
	    return String.class;
	}
    }

    public DocumentItem(Document document) {
	super(document);
    }

    public DocumentItem(Class<? extends Document> type) {
	super(type);
    }

    {
	setWriteThrough(false);
    }

    @Override
    protected Property makeProperty(Object propertyId) {
	if (propertyId instanceof MetadataKey) {
	    BufferedProperty<String> property = new BufferedProperty<String>(new MetadataKeyProperty((MetadataKey) propertyId));
	    addItemProperty(propertyId, property);
	    return property;
	}
	return super.makeProperty(propertyId);
    }
}
