package module.fileManagement.domain;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class VersionedFile extends VersionedFile_Base {

    private static enum FILE_SIZE_UNIT {

	B, KB, MB, GB, TB, PB;

	private final double units;
	private final double unitsForConv;

	{
	    units = Math.pow(1024, ordinal() + 1);
	    unitsForConv = Math.pow(1024, ordinal());
	}

	private double convert(final double size) {
	    return size / unitsForConv;
	}

	private String toPresentationString(final double size) {
	    final double convertedSize = convert(size);
	    final long roundedValue = Math.round(convertedSize);
	    return DocumentSystem.getMessage("label.file.size.value",
		    Long.toString(roundedValue), toString());
	}

	private static String prettyPring(final double bytes) {
	    for (final FILE_SIZE_UNIT unit : FILE_SIZE_UNIT.values()) {
		if (unit.units > bytes) {
		    return unit.toPresentationString(bytes);
		}
	    }
	    return values()[values().length - 1].toPresentationString(bytes);
	}
    }

    public VersionedFile() {
        super();
    }

    public VersionedFile(final File file, final String fileName) {
	try {
	    init(fileName, fileName, FileUtils.readFileToByteArray(file));
	} catch (final IOException e) {
	    throw new Error(e);
	}
    }

    public int getFilesize() {
	final byte[] content = getContent();
	return content.length;
    }

    public String getPresentationFilesize() {
	final int filesize = getFilesize();
	return FILE_SIZE_UNIT.prettyPring(filesize);
    }

    @Override
    public void delete() {
	final Document document = getDocument();
	document.setLastVersionedFile(getPreviousVersion());
	removePreviousVersion();
        super.delete();
    }

}
