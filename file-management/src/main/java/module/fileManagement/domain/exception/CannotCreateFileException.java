package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;
import pt.ist.bennu.core.domain.exceptions.DomainException;

public class CannotCreateFileException extends DomainException {
    private String fileName;

    public CannotCreateFileException(String fileName) {
        super();
        this.fileName = fileName;
    }

    @Override
    public String getMessage() {
        return FileManagementSystem.getMessage(getClass().getName(), fileName);
    }
}
