package module.fileManagement.domain;

public class Metadata extends Metadata_Base {
    
    public Metadata() {
        super();
    }
    
    public Metadata(final String key, final String value) {
	setValueKey(key);
	setValue(value);
    }

    public void delete() {
	removeDocument();
	deleteDomainObject();
    }
    
}
