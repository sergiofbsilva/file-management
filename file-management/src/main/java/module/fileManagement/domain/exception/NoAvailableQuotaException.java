package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;

public class NoAvailableQuotaException extends FileManagementDomainException {
    public NoAvailableQuotaException() {
        super(FileManagementSystem.getMessage("message.file.upload.failled.exceeded.quota"));
    }
}
