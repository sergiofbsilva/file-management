package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.AbstractFileNode;
import pt.ist.vaadinframework.data.reflect.DomainItem;

public class SharedDirDetails extends DirDetails {

    public SharedDirDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem, operationsVisible,infoVisible);
    }
    
    public SharedDirDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem,true,true);
    }
    
    

}
