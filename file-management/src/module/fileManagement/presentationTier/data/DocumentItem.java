package module.fileManagement.presentationTier.data;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.Metadata;
import module.fileManagement.domain.MetadataKey;
import pt.ist.vaadinframework.data.BufferedItem.BufferedProperty;
import pt.ist.vaadinframework.data.HintedProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;

public class DocumentItem extends DomainItem<Document> {

    public DocumentItem(HintedProperty value) {
	super(value);
//	setWriteThrough(true);
    }	
    
    @Override
    public void setWriteThrough(boolean arg0) throws SourceException, InvalidValueException {
        super.setWriteThrough(false);
    }
    
    @Override
    protected Property makeProperty(Object propertyId) {
	Property property;
	if (propertyId instanceof MetadataKey) {
	    property = new BufferedProperty((Object) propertyId, String.class);
	    addItemProperty(propertyId, property);
	    return property;
	} else {
	    return super.makeProperty((String) propertyId);
	}
    }

    @Override
    protected Object readPropertyValue(Document host, Object propertyId) {
	if (propertyId instanceof MetadataKey) {
	    Metadata metadata = host.getMetadataRecentlyChanged((MetadataKey) propertyId);
	    return metadata != null ? metadata.getValue() : null; 
	}
	return super.readPropertyValue(host, (String) propertyId);
    }

    @Override
    protected void writePropertyValue(Document host, Object propertyId, Object newValue) {
	if (propertyId instanceof MetadataKey) {
	    host.addMetadata((MetadataKey) propertyId, (String) newValue);
	} else {
	    super.writePropertyValue(host, (String) propertyId, newValue);
	}
    }

}
