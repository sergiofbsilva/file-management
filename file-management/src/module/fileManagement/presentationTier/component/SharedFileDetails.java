package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.SharedFileNode;

public class SharedFileDetails extends FileDetails {

    public SharedFileDetails(SharedFileNode sharedFileNode, boolean operationsVisible) {
	super(sharedFileNode, operationsVisible);
	addVisibleProperty("ownerName");
    }
    
    public SharedFileDetails(SharedFileNode sharedFileNode) {
	this(sharedFileNode,true);
    }
    
    @Override
    public void updateOperations() {
	addOperation(createDownloadLink());
	addOperation(createDeleteFileLink());
    }
}
