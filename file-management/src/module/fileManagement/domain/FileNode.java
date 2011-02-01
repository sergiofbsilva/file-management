package module.fileManagement.domain;

import java.io.File;

import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;

public class FileNode extends FileNode_Base {

    public FileNode() {
        super();
    }

    public FileNode(final DirNode dirNode, final File file, final String fileName) {
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

}
