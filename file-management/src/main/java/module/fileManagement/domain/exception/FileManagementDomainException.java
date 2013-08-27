package module.fileManagement.domain.exception;

import module.fileManagement.domain.FileManagementSystem;

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.domain.exceptions.DomainException;

@SuppressWarnings("serial")
public class FileManagementDomainException extends DomainException {

    public FileManagementDomainException() {
        this(StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public FileManagementDomainException(String key, String... args) {
        super(FileManagementSystem.getBundleName(), key, args);
    }
}
