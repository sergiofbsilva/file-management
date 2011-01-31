package module.fileManagement.domain;

public class AbstractFileNode extends AbstractFileNode_Base {
    
    public AbstractFileNode() {
        super();
    }

    public boolean isFile() {
	return false;
    }
    
}
