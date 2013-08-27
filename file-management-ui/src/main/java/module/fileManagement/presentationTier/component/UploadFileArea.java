package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;

import org.vaadin.easyuploads.FileFactory;

import pt.ist.bennu.core.security.Authenticate;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class UploadFileArea extends CustomComponent {

    private UploadArea uploadArea;
    private VerticalLayout vlError;
    private ContextPath contextPath;

    private static Method UPLOAD_FILE_METHOD;
    public static FileFactory DEFAULT_FACTORY;

    static {
        try {
            UPLOAD_FILE_METHOD =
                    FileUploadListener.class.getDeclaredMethod("uploadedFile", new Class[] { OnFileUploadEvent.class });
            DEFAULT_FACTORY = new TempFileFactory();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static class TempFileFactory implements FileFactory {

        public TempFileFactory() {

        }

        @Override
        public File createFile(String fileName, String mimeType) {
            final String tempFileName = "upload_tmpfile_" + Authenticate.getUser().getUsername();
            try {
                return File.createTempFile(tempFileName, "_" + System.currentTimeMillis());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class UploadArea extends MultiFileUpload {

        public UploadArea() {
            setFileFactory(DEFAULT_FACTORY);
            setMargin(true);
            setSizeFull();
        }

        @Override
        protected void handleFile(final File file, final String fileName, final String mimeType, final long length) {
            fireFileUploaded(file, fileName, mimeType, length);
        }

        @Override
        protected String getAreaText() {
            return getMessage("label.file.repository.drop.files.here");
        };

        @Override
        protected String getUploadButtonCaption() {
            return getMessage("label.file.repository.add.file");
        }
    }

    public UploadFileArea() {
        super();
        uploadArea = new UploadArea();
        vlError = new VerticalLayout();
        vlError.setSizeFull();
        vlError.setSpacing(true);
    }

    private Component newError(String message) {
        Label label = new Label();
        label.setStyleName("v-upload-error-message");
        label.setValue(message);
        return label;
    }

    public void fireFileUploaded(final File file, final String fileName, final String mimeType, final long length) {
        fireEvent(new OnFileUploadEvent(this, file, fileName, mimeType, length));
    }

    public DirNode getUploadDir() {
        return contextPath.getLastDirNode();
    }

    public class OnFileUploadEvent extends Component.Event {

        private final File file;
        private final String fileName;
        private final String mimeType;
        private final long length;

        public OnFileUploadEvent(Component source, final File file, final String fileName, final String mimeType,
                final long length) {
            super(source);
            this.file = file;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.length = length;
        }

        public File getFile() {
            return file;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public long getLength() {
            return length;
        }

    }

    public interface FileUploadListener extends Serializable {
        public void uploadedFile(OnFileUploadEvent event);
    }

    public void addListener(FileUploadListener listener) {
        addListener(OnFileUploadEvent.class, listener, UPLOAD_FILE_METHOD);
    }

    @Override
    public void attach() {
        setCompositionRoot(uploadArea);
    }

    public Layout getErrorLayout() {
        return vlError;
    }

    public void setContextPath(ContextPath contextPath) {
        this.contextPath = contextPath;
    }

    public void setError(String message) {
        vlError.removeAllComponents();
        vlError.addComponent(newError(message));
    }
}
