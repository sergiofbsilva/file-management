package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;

import org.vaadin.easyuploads.DirectoryFileFactory;

import pt.ist.fenixframework.FFDomainException;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class UploadFileArea extends CustomComponent {

    private UploadArea uploadArea;
    private DirNode uploadDir;
    private VerticalLayout vlError;

    private static Method UPLOAD_FILE_METHOD;

    static {
	try {
	    UPLOAD_FILE_METHOD = FileUploadListener.class.getDeclaredMethod("uploadedFile",
		    new Class[] { OnFileUploadEvent.class });
	} catch (SecurityException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private class UploadArea extends MultiFileUpload {

	public UploadArea() {
	    final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	    setFileFactory(directoryFileFactory);
	    setMargin(true);
	    setSizeFull();
	}

	private Component newError(String message) {
	    Label label = new Label();
	    label.setStyleName("v-upload-error-message");
	    label.setValue(message);
	    return label;
	}

	@Override
	protected void handleFile(final File file, final String fileName, final String mimeType, final long length) {
//	    final DirNode destination = uploadDir == null || !uploadDir.isWriteGroupMember() ? UserView.getCurrentUser()
//		    .getFileRepository() : uploadDir;
	    if (uploadDir != null) {
		try {
		    final FileNode fileNode = uploadDir.createFile(file, fileName, length);
		    fireFileUploaded(fileNode);
		} catch (FFDomainException ffde) {
		    vlError.addComponent(newError(ffde.getMessage()));
		}
	    }
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

    public UploadFileArea(DirNode dirNode) {
	this();
	setUploadDir(dirNode);
    }

    public void fireFileUploaded(FileNode fileNode) {
	fireEvent(new OnFileUploadEvent(this, fileNode));
    }

    public DirNode getUploadDir() {
	return uploadDir;
    }

    public class OnFileUploadEvent extends Component.Event {
	private FileNode node;

	public OnFileUploadEvent(Component source) {
	    super(source);
	}

	public OnFileUploadEvent(Component source, FileNode node) {
	    this(source);
	    this.node = node;
	}

	public FileNode getUploadedFileNode() {
	    return node;
	}
    }

    public interface FileUploadListener extends Serializable {
	public void uploadedFile(OnFileUploadEvent event);
    }

    public void addListener(FileUploadListener listener) {
	addListener(OnFileUploadEvent.class, listener, UPLOAD_FILE_METHOD);
    }

    public void setUploadDir(DirNode uploadDir) {
	this.uploadDir = uploadDir;
    }

    @Override
    public void attach() {
	setCompositionRoot(uploadArea);
    }

    public Layout getErrorLayout() {
	return vlError;
    }

}
