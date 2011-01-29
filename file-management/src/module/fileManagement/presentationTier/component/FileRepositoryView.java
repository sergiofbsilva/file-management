package module.fileManagement.presentationTier.component;

import java.io.File;

import module.contents.presentationTier.component.BaseComponent;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileRepository;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.MultiFileUpload;
import org.vaadin.easyuploads.MultiUpload;

import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import vaadin.annotation.EmbeddedComponent;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "FileRepositoryView-(.*)" })
public class FileRepositoryView extends BaseComponent implements EmbeddedComponentContainer {

    @Override
    protected String getBundle() {
	return "resources.FileManagementResources";
    }

    @Override
    public void setArguments(final String... arguments) {
    }

    @Override
    public void attach() {
	final AbstractLayout layout = createVerticalLayout();
	setCompositionRoot(layout);

	final User user = UserView.getCurrentUser();
	final DirNode dirNode = FileRepository.getOrCreateFileRepository(user);

	final Label title = new Label(getMessage("label.file.repository.of") + " " + user.getPresentationName());
	layout.addComponent(title);

	if (dirNode.hasAnyChild()) {
	} else {
	    final Label noFilesFound = new Label(getMessage("label.file.repository.no.files.found"));
	    layout.addComponent(noFilesFound);	    
	}

        final MultiFileUpload multiFileUpload = new MultiFileUpload() {

            {
        	setWidth("400px");
            }

            @Override
            public void addComponent(final Component c) {
                super.addComponent(c);
                if (c instanceof MultiUpload) {
                    final MultiUpload mu = (MultiUpload) c;
                    mu.setButtonCaption(getUploadButtonCaption());
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
            
	    @Override
	    protected void handleFile(File file, String fileName, String mimeType, long length) {
		System.out.println("Filename: " + fileName + " " + length);

		final Label label = new Label("Uploaded file: " + fileName);
		layout.addComponent(label);
	    }

	};
	final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	multiFileUpload.setFileFactory(directoryFileFactory);
	layout.addComponent(multiFileUpload);

/**
	final Label label = new Label("Hello World");
	layout.addComponent(label);

	
	MultiUpload multiUpload = new MultiUpload();
	multiUpload.setHandler(new MultiUploadHandler() {
	    
	    @Override
	    public void streamingStarted(StreamingStartEvent event) {
		System.out.println("1");
	    }
	    
	    @Override
	    public void streamingFinished(StreamingEndEvent event) {
		System.out.println("2");
	    }
	    
	    @Override
	    public void streamingFailed(StreamingErrorEvent event) {
		System.out.println("3");
	    }
	    
	    @Override
	    public void onProgress(StreamingProgressEvent event) {
		System.out.println("4");
	    }
	    
	    @Override
	    public OutputStream getOutputStream() {
		System.out.println("5");
		return null;
	    }
	    
	    @Override
	    public void filesQueued(Collection<FileDetail> pendingFileNames) {
		System.out.println("6");
	    }
	});
	layout.addComponent(multiUpload);
	
        final MultiFileUpload multiFileUpload = new MultiFileUpload() {

	    @Override
	    protected void handleFile(File file, String fileName, String mimeType, long length) {
		System.out.println("Filename: " + fileName + " " + length);

		final Label label = new Label("Uploaded file: " + fileName);
		layout.addComponent(label);
	    }

	};
	final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	multiFileUpload.setFileFactory(directoryFileFactory);
	layout.addComponent(multiFileUpload);
	

/*	final MultiUpload multiUpload = new MultiUpload();
	layout.addComponent(multiUpload);

	final Label label1 = new Label("Sep 1");
	layout.addComponent(label1);

	final MultiFileUpload multiFileUpload = new MultiFileUpload() {
	    
	    @Override
	    protected void handleFile(final File file, final String fileName, final String mimeType, final long length) {
		// TODO Auto-generated method stub
		
	    }
	};

	layout.addComponent(multiFileUpload);

	final Label label2 = new Label("Sep 2");
	layout.addComponent(label2);
*/
	
	setImmediate(true);
	super.attach();
    }

}
