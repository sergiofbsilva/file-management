package module.fileManagement.domain;

import pt.ist.fenixWebFramework.services.Service;

public class MetadataKey extends MetadataKey_Base {
    
    public  MetadataKey() {
        super();
    }
    
    public MetadataKey(String keyValue) {
	setKeyValue(keyValue);
    }

    @Service
    public static MetadataKey getOrCreateInstance(String keyValue) {
	MetadataKey metadataKey = FileManagementSystem.getInstance().getMetadataKey(keyValue);
	if (metadataKey == null) {
	    metadataKey = new MetadataKey(keyValue);
	    FileManagementSystem.getInstance().addMetadataKeys(metadataKey);
	}
	return metadataKey;
    }
}
