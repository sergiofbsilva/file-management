package module.fileManagement.domain;

import pt.ist.fenixWebFramework.services.Service;

public class AbstractFileNode extends AbstractFileNode_Base {
    
    public AbstractFileNode() {
        super();
    }

    public boolean isFile() {
	return false;
    }

    public boolean isDir() {
	return false;
    }

    public void delete() {
	removeParent();
	deleteDomainObject();
    }

    @Service
    public void deleteService() {
	delete();
    }

}
