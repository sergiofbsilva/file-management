package module.fileManagement.domain;

import org.joda.time.LocalDate;

public class Metadata extends Metadata_Base {
    
    public Metadata() {
        super();
    }
    
    public Metadata(final String keyValue, final String value) {
	setMetadataKey(MetadataKey.getOrCreateInstance(keyValue));
	setValue(value);
	setTimestamp(new LocalDate());
    }

    public void delete() {
	removeDocument();
	deleteDomainObject();
    }
    
    public String getKeyValue() {
	return getMetadataKey().getKeyValue();
    }
    
}
