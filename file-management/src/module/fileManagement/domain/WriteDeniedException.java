package module.fileManagement.domain;

import myorg.domain.exceptions.DomainException;

public class WriteDeniedException extends DomainException {
    
    public WriteDeniedException() {
	super();
    }

    @Override
    public String getMessage() {
	return FileManagementSystem.getMessage(getClass().getName());
    }
    
}
