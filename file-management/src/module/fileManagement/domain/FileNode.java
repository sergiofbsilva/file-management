package module.fileManagement.domain;

import java.io.File;

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

}
