package module.fileManagement.domain;

import java.io.File;

import myorg.domain.exceptions.DomainException;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;

public class FileNode extends FileNode_Base {

    public FileNode(final DirNode dirNode, final File file, final String fileName) {
	if (dirNode == null) {
	    throw new DomainException("must.specify.a.dir.node.when.creating.a.file.node");
	}
	setParent(dirNode);
	setFile(new UploadedFile(file, fileName));
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public void delete() {
	if (hasFile()) {
	    final GenericFile file = getFile();
	    file.delete();
	    removeFile();
	}
        super.delete();
    }

    @Service
    public void deleteService() {
	delete();
    }

    @Service
    public void edit(final String displayName, final String filename) {
	final GenericFile file = getFile();
	file.setDisplayName(displayName);
	file.setFilename(filename);
    }

}
