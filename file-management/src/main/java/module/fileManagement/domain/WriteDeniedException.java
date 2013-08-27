package module.fileManagement.domain;

import module.fileManagement.domain.exception.FileManagementDomainException;

@SuppressWarnings("serial")
public class WriteDeniedException extends FileManagementDomainException {

    public WriteDeniedException() {
        super();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return FileManagementSystem.getMessage(getClass().getName());
    }

}
