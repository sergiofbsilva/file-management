package module.fileManagement.domain;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class UploadedFile extends UploadedFile_Base {
    
    public UploadedFile() {
        super();
    }

    public UploadedFile(final File file, final String fileName) {
	try {
	    init(fileName, fileName, FileUtils.readFileToByteArray(file));
	} catch (final IOException e) {
	    throw new Error(e);
	}
    }
    
}
