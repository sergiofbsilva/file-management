package module.fileManagement.domain;

public class Metadata extends Metadata_Base {
    
    public  Metadata() {
        super();
    }
    
    public Metadata(String key, String value) {
	setValueKey(key);
	setValue(value);
    }
    
}
