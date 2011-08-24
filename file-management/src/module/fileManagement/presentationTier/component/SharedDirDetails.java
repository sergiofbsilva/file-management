package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.SharedDirNode;

public class SharedDirDetails extends DirDetails {

    public SharedDirDetails(SharedDirNode sharedDirNode, boolean operationsVisible) {
	super(sharedDirNode, operationsVisible);
	addVisibleProperty("ownerName");
    }
    
    public SharedDirDetails(SharedDirNode sharedDirNode) {
	this(sharedDirNode,true);
    }
    
    

}
