package module.fileManagement.presentationTier.component.viewers;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.domain.log.DirLog;
import module.fileManagement.domain.log.FileLog;
import module.fileManagement.presentationTier.DownloadUtil;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;


class FileLogViewer extends AbstractLogViewer<FileLog> {
    
    public FileLogViewer(FileLog fileLog) {
	super(fileLog);
    }
    
    public String createTargetNodeLink() {
	final FileNode fileNode = getLog().getFileNode();
	if (fileNode.isAccessible()) {
	    final String url = DownloadUtil.getDownloadUrl(getApplication(), fileNode.getDocument());
	    return String.format("<a href=\"%s\">%s</a>",url, fileNode.getDisplayName()); 
	} else {
	    return fileNode.getDisplayName();
	}
    }
}

class DirLogViewer extends AbstractLogViewer<DirLog> {
    
    public DirLogViewer(DirLog log) {
	super(log);
    }

    @Override
    public String createTargetNodeLink() {
	final ContextPath targetNodePath = getLog().getContextPath().concat(getLog().getDirNode());
	return createDirNodeLink(targetNodePath);
    }
}


public class LogViewerFactory {

    public static Component createViewer(AbstractLog log) {
	if (log instanceof FileLog) {
	    return new FileLogViewer((FileLog) log);
	}
	if (log instanceof DirLog) {
	    return new DirLogViewer((DirLog) log);
	}
	return new Label();
    }
}
