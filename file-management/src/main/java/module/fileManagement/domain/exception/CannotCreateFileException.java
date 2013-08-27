package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("serial")
public class CannotCreateFileException extends FileManagementDomainException {
    private String fileName;

    public CannotCreateFileException(String fileName) {
        super(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        this.fileName = fileName;
    }

    @Override
    public String getMessage() {
        return FileManagementSystem.getMessage(getClass().getName(), fileName);
    }
}
