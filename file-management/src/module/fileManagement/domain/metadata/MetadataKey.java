package module.fileManagement.domain.metadata;

import module.fileManagement.domain.FileManagementSystem;
import pt.ist.fenixWebFramework.services.Service;

public class MetadataKey extends MetadataKey_Base implements Comparable<MetadataKey> {

    public static final String TEMPLATE_KEY_VALUE = "Tipologia";
    public static final String FILENAME_KEY_VALUE = "Nome do Ficheiro";
    public static final String SAVE_ACCESS_LOG = "save.access.log";
    public static final String DOCUMENT_NEW_VERSION_KEY = "document.new.version.key.value";

    public MetadataKey() {
	super();
	FileManagementSystem.getInstance().addMetadataKeys(this);
    }

    public MetadataKey(String keyValue) {
	this(keyValue, Boolean.FALSE, StringMetadata.class);
    }

    public MetadataKey(String keyValue, Boolean reserved, Class<? extends Metadata> classType) {
	this();
	setKeyValue(keyValue);
	setReserved(reserved);
	setMetadataValueType(classType);
    }

    public static MetadataKey getTemplateKey() {
	return getOrCreateInstance(TEMPLATE_KEY_VALUE, Boolean.TRUE, StringMetadata.class);
    }

    public static MetadataKey getFilenameKey() {
	return getOrCreateInstance(FILENAME_KEY_VALUE, Boolean.TRUE, StringMetadata.class);
    }

    public static MetadataKey getSaveLogKey() {
	return getOrCreateInstance(SAVE_ACCESS_LOG, Boolean.TRUE, StringMetadata.class);
    }

    public static MetadataKey getNewDocumentVersionKey() {
	return getOrCreateInstance(DOCUMENT_NEW_VERSION_KEY, Boolean.TRUE, StringMetadata.class);
    }

    public static MetadataKey getOrCreateInstance(final String keyValue) {
	return getOrCreateInstance(keyValue, Boolean.FALSE, StringMetadata.class);
    }

    public static MetadataKey getOrCreateInstance(final String keyValue, final Class<? extends Metadata> classType) {
	return getOrCreateInstance(keyValue, Boolean.FALSE, classType);
    }

    public static MetadataKey getOrCreateInstance(final String keyValue, final Boolean reserved) {
	return getOrCreateInstance(keyValue, reserved, StringMetadata.class);
    }

    @Service
    public static MetadataKey getOrCreateInstance(final String keyValue, final Boolean reserved,
	    final Class<? extends Metadata> classType) {
	MetadataKey metadataKey = FileManagementSystem.getInstance().getMetadataKey(keyValue, reserved, classType);
	if (metadataKey == null) {
	    metadataKey = new MetadataKey(keyValue, reserved, classType);
	}
	return metadataKey;
    }

    @Service
    public void delete() {
	if (hasAnyMetadata() || hasAnyRule()) {
	    return;
	}
	getTemplates().clear();
	FileManagementSystem.getInstance().removeMetadataKeys(this);
	deleteDomainObject();
    }

    @Override
    public int compareTo(MetadataKey o) {
	return getKeyValue().compareTo(o.getKeyValue());
    }

    public Metadata createMetadata(Object value) {
	final Class metadataValueType = getMetadataValueType();
	try {
	    final Metadata newInstance = (Metadata) metadataValueType.newInstance();
	    newInstance.setMetadataKey(this);
	    newInstance.setRealValue(value);
	    return newInstance;
	} catch (InstantiationException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public Class getKeyPrimitiveType() {
	return Metadata.getMetadataType(getMetadataValueType());
    }

    public boolean isReserved() {
	return getReserved() != null && getReserved();
    }

    @Override
    public String toString() {
	return String.format("%s : %s (%s)", getKeyValue(), getMetadataValueType().getSimpleName(), isReserved());
    }

}
