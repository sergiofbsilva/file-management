package module.fileManagement.domain;

import pt.ist.bennu.core.domain.exceptions.DomainException;

public class WriteDeniedException extends DomainException {
    
    public WriteDeniedException() {
	super();
    }

    @Override
    public String getMessage() {
	return FileManagementSystem.getMessage(getClass().getName());
    }
    
}
