package module.fileManagement.presentationTier.component;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.MultiUpload;
import org.vaadin.easyuploads.MultiUpload.FileDetail;
import org.vaadin.easyuploads.MultiUploadHandler;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.terminal.StreamVariable.StreamingEndEvent;
import com.vaadin.terminal.StreamVariable.StreamingErrorEvent;
import com.vaadin.terminal.StreamVariable.StreamingProgressEvent;
import com.vaadin.terminal.StreamVariable.StreamingStartEvent;
import com.vaadin.terminal.gwt.server.AbstractWebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;

/**
 * MultiFileUpload makes it easier to upload multiple files. MultiFileUpload
 * releases upload button for new uploads immediately when a file is selected
 * (aka parallel uploads). It also displays progress indicators for pending
 * uploads.
 * <p>
 * MultiFileUpload always streams straight to files to keep memory consumption low. To temporary files by default, but this can be
 * overridden with {@link #setFileFactory(FileFactory)} (eg. straight to target directory on the server).
 * <p>
 * Developer handles uploaded files by implementing the abstract {@link #handleFile(File, String, String, long)} method.
 * <p>
 * TODO Field version (type == Collection<File> or File where isDirectory() == true).
 * <p>
 * TODO a super progress indicator (total transferred per total, including queued files)
 * <p>
 * TODO Time remaining estimates and current transfer rate
 * 
 */
@SuppressWarnings("serial")
public abstract class MultiFileUpload extends CssLayout implements DropHandler {

    private CssLayout progressBars = new CssLayout();
    private CssLayout uploads = new CssLayout();

    public MultiFileUpload() {
        addComponent(progressBars);
        uploads.setStyleName("v-multifileupload-uploads");
        addComponent(uploads);
        prepareUpload();
    }

    private void prepareUpload() {
        final FileBuffer reciever = createReceiver();

        final MultiUpload upload = new MultiUpload();
        MultiUploadHandler handler = new MultiUploadHandler() {
            private LinkedList<ProgressIndicator> indicators;

            @Override
            public void streamingStarted(StreamingStartEvent event) {
//		System.err.println("Started" + event.getFileName() + " size " + event.getContentLength());
            }

            @Override
            public void streamingFinished(StreamingEndEvent event) {
                if (!indicators.isEmpty()) {
                    progressBars.removeComponent(indicators.remove(0));
                }
                File file = reciever.getFile();
                handleFile(file, event.getFileName(), event.getMimeType(), event.getBytesReceived());
                reciever.setValue(null);
            }

            @Override
            public void streamingFailed(StreamingErrorEvent event) {
                event.getException().printStackTrace();

                for (ProgressIndicator progressIndicator : indicators) {
                    progressBars.removeComponent(progressIndicator);
                }

            }

            @Override
            public void onProgress(StreamingProgressEvent event) {
                long readBytes = event.getBytesReceived();
                long contentLength = event.getContentLength();
                float f = (float) readBytes / (float) contentLength;
                indicators.get(0).setValue(f);
            }

            @Override
            public OutputStream getOutputStream() {
                FileDetail next = upload.getPendingFileNames().iterator().next();
                return reciever.receiveUpload(next.getFileName(), next.getMimeType());
            }

            @Override
            public void filesQueued(Collection<FileDetail> pendingFileNames) {
                if (indicators == null) {
                    indicators = new LinkedList<ProgressIndicator>();
                }
                for (FileDetail f : pendingFileNames) {
//		    System.err.println("Info to handler about file: " + f.getFileName());
                    ProgressIndicator pi = createProgressIndicator();
                    progressBars.addComponent(pi);
                    pi.setCaption(f.getFileName());
                    pi.setVisible(true);
                    indicators.add(pi);
                }
            }
        };
        upload.setHandler(handler);
        upload.setButtonCaption(getUploadButtonCaption());
        uploads.addComponent(upload);

    }

    private ProgressIndicator createProgressIndicator() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPollingInterval(300);
        progressIndicator.setValue(0);
        return progressIndicator;
    }

    protected String getUploadButtonCaption() {
        return "...";
    }

    private FileFactory fileFactory;

    public FileFactory getFileFactory() {
        return fileFactory;
    }

    public void setFileFactory(FileFactory fileFactory) {
        this.fileFactory = fileFactory;
    }

    protected FileBuffer createReceiver() {
        return new FileBuffer() {
            @Override
            public FileFactory getFileFactory() {
                return MultiFileUpload.this.getFileFactory();
            }
        };
    }

    protected int getPollinInterval() {
        return 500;
    }

    @Override
    public void attach() {
        super.attach();
        if (supportsFileDrops()) {
            prepareDropZone();
        }
    }

    private DragAndDropWrapper dropZone;

    /**
     * Sets up DragAndDropWrapper to accept multi file drops.
     */
    private void prepareDropZone() {
        Component label = new Label(getAreaText(), Label.CONTENT_XHTML);
        label.setSizeUndefined();
        dropZone = new DragAndDropWrapper(label);
        dropZone.setStyleName("v-multifileupload-dropzone");
//	dropZone.setSizeUndefined();
//	dropZone.setSizeFull();
        dropZone.setWidth(50, UNITS_PERCENTAGE);
        addComponent(dropZone, 1);
        dropZone.setDropHandler(this);
        addStyleName("no-horizontal-drag-hints");
        addStyleName("no-vertical-drag-hints");
    }

    protected String getAreaText() {
        return "<small>DROP<br/>FILES</small>";
    }

    protected boolean supportsFileDrops() {
        AbstractWebApplicationContext context = (AbstractWebApplicationContext) getApplication().getContext();
        WebBrowser browser = context.getBrowser();
        if (browser.isChrome()) {
            return true;
        } else if (browser.isFirefox()) {
            return true;
        } else if (browser.isSafari()) {
            return true;
        }
        return false;
    }

    abstract protected void handleFile(File file, String fileName, String mimeType, long length);

    /**
     * A helper method to set DirectoryFileFactory with given pathname as
     * directory.
     * 
     * @param file
     */
    public void setRootDirectory(String directoryWhereToUpload) {
        setFileFactory(new DirectoryFileFactory(new File(directoryWhereToUpload)));
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        // TODO accept only files
        // return new And(new TargetDetailIs("verticalLocation","MIDDLE"), new
        // TargetDetailIs("horizontalLoction", "MIDDLE"));
        return AcceptAll.get();
    }

    @Override
    public void drop(DragAndDropEvent event) {
        DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
        Html5File[] files = transferable.getFiles();
        for (final Html5File html5File : files) {
            final ProgressIndicator pi = new ProgressIndicator();
            pi.setCaption(html5File.getFileName());
            progressBars.addComponent(pi);
            final FileBuffer receiver = createReceiver();
            html5File.setStreamVariable(new StreamVariable() {

                private String name;
                private String mime;

                @Override
                public OutputStream getOutputStream() {
                    return receiver.receiveUpload(name, mime);
                }

                @Override
                public boolean listenProgress() {
                    return true;
                }

                @Override
                public void onProgress(StreamingProgressEvent event) {
                    float p = (float) event.getBytesReceived() / (float) event.getContentLength();
                    pi.setValue(p);
                }

                @Override
                public void streamingStarted(StreamingStartEvent event) {
                    name = event.getFileName();
                    mime = event.getMimeType();

                }

                @Override
                public void streamingFinished(StreamingEndEvent event) {
                    progressBars.removeComponent(pi);
                    handleFile(receiver.getFile(), html5File.getFileName(), html5File.getType(), html5File.getFileSize());
                }

                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    progressBars.removeComponent(pi);
                }

                @Override
                public boolean isInterrupted() {
                    return false;
                }
            });
        }

    }

    public void hideProgressBars() {
        progressBars.setVisible(false);
    }
}
