package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import myorg.applicationTier.Authenticate.UserView;

import org.vaadin.easyuploads.DirectoryFileFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Window.Notification;


public class UploadFileArea extends CustomComponent {

    private UploadArea uploadArea;
    private DirNode uploadDir;
    
    private static Method UPLOAD_FILE_METHOD;
    
    static {
	try {
	    UPLOAD_FILE_METHOD = FileUploadListener.class.getDeclaredMethod(
	            "uploadedFile", new Class[] { OnFileUploadEvent.class });
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
	    setWidth(75, UNITS_PERCENTAGE);
	    setMargin(true);
	}

	@Override
	protected void handleFile(final File file, final String fileName, final String mimeType, final long length) {
	    final DirNode destination = uploadDir == null || !uploadDir.isWriteGroupMember() ? UserView.getCurrentUser()
		    .getFileRepository() : uploadDir;
	    if (destination != null) {
		if (destination.hasAvailableQuota(length)) {
		    final FileNode fileNode = destination.createFile(file, fileName);
		    // if (destination == uploadDir) {
		    //
		    // }
		    fireFileUploaded(fileNode);
		} else {
		    getWindow().showNotification(FileManagementSystem.getMessage("message.file.upload.failled"),
			    FileManagementSystem.getMessage("message.file.upload.failled.exceeded.quota"), Notification.TYPE_ERROR_MESSAGE);
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
        super.attach();
        setCompositionRoot(uploadArea);
    }
    

}
