package module.fileManagement.domain;

import java.io.File;

public class Document extends Document_Base {
    
    public Document() {
        super();
    }

    public Document(final File file, final String fileName) {
	setLastVersionedFile(new VersionedFile(file, fileName));
    }

    public String getDisplayName() {
	final VersionedFile file = getLastVersionedFile();
	return file.getDisplayName();
    }

    public void setDisplayName(final String displayName) {
	final VersionedFile file = getLastVersionedFile();
	file.setDisplayName(displayName);
    }

    public void delete() {
	removeReadGroup();
	removeWriteGroup();
	for (final Metadata metadata : getMetadataSet()) {
	    metadata.delete();
	}
	for (final FileNode fileNode : getFileNodeSet()) {
	    fileNode.delete();
	}
	while (hasLastVersionedFile()) {
	    getLastVersionedFile().delete();
	}
	deleteDomainObject();
    }

    public String getPresentationFilesize() {
	final VersionedFile file = getLastVersionedFile();
	return file.getPresentationFilesize();
    }
    
}
