package module.fileManagement.domain;

import myorg.domain.exceptions.DomainException;
import pt.ist.fenixWebFramework.services.Service;

public class MetadataKey extends MetadataKey_Base implements Comparable<MetadataKey> {

    public static final String TEMPLATE_KEY_VALUE = "Tipologia";
    public static final String FILENAME_KEY_VALUE = "Nome do Ficheiro";
    public static final String SAVE_ACCESS_LOG = "save.access.log";

    public MetadataKey() {
	super();
	FileManagementSystem.getInstance().addMetadataKeys(this);
    }

    public MetadataKey(String keyValue) {
	this(keyValue, Boolean.FALSE);
    }

    public MetadataKey(String keyValue, Boolean reserved) {
	this();
	setKeyValue(keyValue);
	setReserved(reserved);
    }

    public static MetadataKey getTemplateKey() {
	return getOrCreateInstance(TEMPLATE_KEY_VALUE);
    }

    public static MetadataKey getOrCreateInstance(final String keyValue) {
	return getOrCreateInstance(keyValue, Boolean.FALSE);
    }

    @Service
    public static MetadataKey getOrCreateInstance(final String keyValue, final Boolean reserved) {
	MetadataKey metadataKey = FileManagementSystem.getInstance().getMetadataKey(keyValue);
	if (metadataKey == null || !reserved.equals(metadataKey.getReserved())) {
	    metadataKey = new MetadataKey(keyValue, reserved);
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
