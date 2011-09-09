package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;
import myorg.domain.exceptions.DomainException;

public class NodeDuplicateNameException extends DomainException {

    public NodeDuplicateNameException() {
	super();
    }
    
    @Override
    public String getMessage() {
	return FileManagementSystem.getMessage(getClass().getName());
    }
    
    @Override
    public String getLocalizedMessage() {
     return getMessage();
    }

}
