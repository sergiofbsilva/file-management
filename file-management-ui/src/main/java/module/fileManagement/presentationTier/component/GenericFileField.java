package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.vaadin.easyuploads.DirectoryFileFactory;

import pt.ist.bennu.io.domain.GenericFile;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CustomField;

public class GenericFileField extends CustomField {
    private UploadArea uploadArea;

    private class UploadArea extends MultiFileUpload {
        Set<GenericFile> uploadedFiles = new HashSet<GenericFile>();

        public UploadArea() {
            final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
            setFileFactory(directoryFileFactory);
            setWidth(75, UNITS_PERCENTAGE);
            setMargin(true);
        }

        @Override
        protected String getAreaText() {
            return getMessage("label.file.repository.drop.files.here");
        };

        @Override
        protected String getUploadButtonCaption() {
            return getMessage("label.file.repository.add.file");
        }

        @Override
        protected void handleFile(File file, String fileName, String mimeType, long length) {
            try {
                final GenericFile genericFileInstance = (GenericFile) getPropertyDataSource().getType().newInstance();
                genericFileInstance.setContent(FileUtils.readFileToByteArray(file));
                genericFileInstance.setContentType(mimeType);
                genericFileInstance.setFilename(fileName);
                uploadedFiles.add(genericFileInstance);
                setValue(uploadedFiles);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    public GenericFileField() {
        uploadArea = new UploadArea();
        setCompositionRoot(uploadArea);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        super.commit();
        getPropertyDataSource().setValue(uploadArea.uploadedFiles);
    }

    @Override
    protected void setInternalValue(Object newValue) {
        super.setInternalValue(newValue);

    }

    @Override
    public Class<?> getType() {
        return GenericFile.class;
    }

}
