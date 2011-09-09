package module.fileManagement.domain;

import myorg.domain.exceptions.DomainException;
import pt.ist.fenixWebFramework.services.Service;

public class MetadataKey extends MetadataKey_Base implements Comparable<MetadataKey> {
    
    public static final String TEMPLATE_KEY_VALUE = "Tipologia";
    public static final String FILENAME_KEY_VALUE = "Nome do Ficheiro";
    
    public  MetadataKey() {
        super();
        FileManagementSystem.getInstance().addMetadataKeys(this);
    }
    
    public MetadataKey(String keyValue) {
	this();
	setKeyValue(keyValue);
    }

    public static MetadataKey getTemplateKey() {
	return getOrCreateInstance(TEMPLATE_KEY_VALUE);
    }
    
    @Service
    public static MetadataKey getOrCreateInstance(String keyValue) {
	MetadataKey metadataKey = FileManagementSystem.getInstance().getMetadataKey(keyValue);
	if (metadataKey == null) {
	    metadataKey = new MetadataKey(keyValue);
	}
	return metadataKey;
    }

    @Service
    public void delete() {
	if (hasAnyMetadata()) {
	    throw new DomainException();
	}
	if (hasAnyTemplates()) {
	    throw new DomainException();
	}
	FileManagementSystem.getInstance().removeMetadataKeys(this);
	deleteDomainObject();
    }

    @Override
    public int compareTo(MetadataKey o) {
	return getKeyValue().compareTo(o.getKeyValue());
    }
    
}
