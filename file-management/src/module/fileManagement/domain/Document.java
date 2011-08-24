package module.fileManagement.domain;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;

public class Document extends Document_Base {
    
    
    public Document() {
        super();
        setModifiedDateNow();
    }
    

    public Document(final File file, final String fileName) {
	this();
	setLastVersionedFile(new VersionedFile(file, fileName));
	addMetadata(MetadataKey.FILENAME_KEY_VALUE, fileName);
    }

    public String getDisplayName() {
	final VersionedFile file = getLastVersionedFile();
	return file.getDisplayName();
    }

    public void setDisplayName(final String displayName) {
	final VersionedFile file = getLastVersionedFile();
	file.setDisplayName(displayName);
    }
    
    @Service
    public void delete() {
	removeReadGroup();
	removeWriteGroup();
	for (final Metadata metadata : getMetadataSet()) {
	    metadata.delete();
	}
	for (final FileNode fileNode : getFileNodeSet()) {
	    fileNode.deleteFromDocument();
	}
	while (hasLastVersionedFile()) {
	    getLastVersionedFile().delete();
	}
	deleteDomainObject();
    }
    
    public void trash() {
//	setLastVersionedFile(VersionedFile.getEmptyVersionFile());
//	for (final FileNode fileNode : getFileNodeSet()) {
//	    fileNode.deleteFromDocument();
//	}
    }

    public String getPresentationFilesize() {
	final VersionedFile file = getLastVersionedFile();
	return file.getPresentationFilesize();
    }
    
    public int getFilesize() {
	final VersionedFile file = getLastVersionedFile();
	return file.getFilesize();
    }
    
    private int getVersionNumber(VersionedFile file) {
	return file.hasPreviousVersion() ? 1 + getVersionNumber(file.getPreviousVersion()) : 1;
    }
    
    public int getVersionNumber() {
	return getVersionNumber(getLastVersionedFile());
    }

    public void addVersion(final File file, final String filename) {
	final VersionedFile versionedFile = getLastVersionedFile();
	final VersionedFile newVersion = new VersionedFile(file, filename);
	newVersion.setPreviousVersion(versionedFile);
	setLastVersionedFile(newVersion);
	setModifiedDateNow();
    }
    
    public void setModifiedDateNow() {
	setLastModifiedDate(new DateTime());
    }
    
    @Service
    public void addMetadata(Collection<Metadata> metadata) {
	for(Metadata md : metadata) {
	  addMetadata(md.hasDocument() ? md.getCopy() : md);
	}
    }
    
    @Service
    public void addMetadata(String key, String value) {
	addMetadata(new Metadata(key,value));
    }
    
    @Service
    public void addMetadata(MetadataKey key, String value) {
	addMetadata(new Metadata(key,value));
    }
    
    public TreeSet<Metadata> getMetadataOrderedByTimestamp() {
	return new TreeSet<Metadata>(getMetadata()); 
    }
    
    public TreeSet<Metadata> getMetadataOrderedByTimestamp(MetadataKey key) {
	TreeSet<Metadata> treeSet = new TreeSet<Metadata>();
	for(Metadata metadata : getMetadata()) {
	    if (metadata.getMetadataKey().equals(key)) {
		treeSet.add(metadata);
	    }
	}
	return treeSet;
    }
    
    public Metadata getMetadataRecentlyChanged(MetadataKey key) {
	final TreeSet<Metadata> metadataByTimestamp = getMetadataOrderedByTimestamp(key);
	for(Metadata metadata : metadataByTimestamp.descendingSet()) {
	    if (metadata.getMetadataKey().equals(key)) {
		return metadata;
	    }
	}
	return null;
    }
    
    /**
     * replace metadata value. If metadata value is not present the metadata value is added.
     * 
     * @param key 
     * @param value if value is empty or null removeMetadata object
     */
    @Service
    public void replaceMetadata(MetadataKey key, String value) {
	boolean changed = false;
	for(Metadata metadata : getMetadataSet()) {
	    final MetadataKey metadataKey = metadata.getMetadataKey();
	    if (metadataKey.equals(key)) {
		if (value == null || value.isEmpty()) {
		    removeMetadata(metadata);
		} else {
		    metadata.setValue(value);
		}
		changed = true;
		break;
	    }
	}
	if (!changed) {
	    addMetadata(key, value);
	}
    }
    
    @Service 
    public void clearAllMetadata() {
	for (Metadata md : getMetadata()) {
	    md.delete();
	}
    }
    
    public Metadata getMetadata(MetadataKey metadataKey) {
	if (metadataKey == null) {
	    return null;
	}
	for(Metadata metadata : getMetadata()) {
	    if (metadataKey.equals(metadata.getMetadataKey())) {
		return metadata;
	    }
	}
	return null;
    }
    
    public String getTypology() {
	final Metadata metadataTemplate = getMetadataRecentlyChanged(MetadataKey.getTemplateKey());
	return metadataTemplate != null ? metadataTemplate.getValue() : "-"; 
    }
}
