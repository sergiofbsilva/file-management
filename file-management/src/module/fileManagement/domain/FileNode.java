package module.fileManagement.domain;

public class FileNode extends FileNode_Base {

    public FileNode() {
        super();
    }

    @Override
    public boolean isFile() {
        return true;
    }

}
