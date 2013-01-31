package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;
import pt.ist.bennu.core.domain.exceptions.DomainException;

public class NoAvailableQuotaException extends DomainException {
	public NoAvailableQuotaException() {
		super(FileManagementSystem.getMessage("message.file.upload.failled.exceeded.quota"));
	}
}
