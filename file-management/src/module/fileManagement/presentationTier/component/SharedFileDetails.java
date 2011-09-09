package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.presentationTier.component.FileDetails;
import pt.ist.vaadinframework.data.reflect.DomainItem;

public class SharedFileDetails extends FileDetails {

    public SharedFileDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem, operationsVisible,infoVisible);
    }
    
    public SharedFileDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem,true,true);
    }
}
