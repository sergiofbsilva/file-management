package module.fileManagement.presentationTier.component.viewers;

import java.util.Collections;
import java.util.List;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.domain.log.DeleteFileLog;
import module.fileManagement.domain.log.DirLog;
import module.fileManagement.domain.log.FileLog;
import module.fileManagement.presentationTier.DownloadUtil;
import module.fileManagement.presentationTier.component.dialog.SelectDestinationDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

class FileLogViewer extends AbstractLogLabel<FileLog> {

    public FileLogViewer(FileLog fileLog) {
	super(fileLog);
    }

    public String createTargetNodeLink() {
	final FileNode fileNode = getLog().getFileNode();
	if (fileNode.isAccessible()) {
	    final String url = DownloadUtil.getDownloadUrl(getApplication(), fileNode.getDocument());
	    return String.format("<a href=\"%s\">%s</a>", url, fileNode.getDisplayName());
	} else {
	    return fileNode.getDisplayName();
	}
    }
}

class DirLogViewer extends AbstractLogLabel<DirLog> {

    public DirLogViewer(DirLog log) {
	super(log);
    }

    @Override
    public String createTargetNodeLink() {
	final ContextPath targetNodePath = getLog().getContextPath().concat(getLog().getDirNode());
	return createDirNodeLink(targetNodePath);
    }
}

class DeleteFileLogViewer extends FileLogViewer {

    public DeleteFileLogViewer(FileLog log) {
	super(log);
    }

    @Override
    public boolean hasOperations() {
	return getLog().getFileNode().isInTrash();
    }

    @Override
    public String createTargetNodeLink() {
	return getLog().getFileNode().getDisplayName();
    }

    @Override
    public List<Component> getOperations() {
	final Component btSelectDir = new Button("Recuperar");
	btSelectDir.setStyleName(BaseTheme.BUTTON_LINK);
	((Button) btSelectDir).addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		final SelectDestinationDialog window = new SelectDestinationDialog();
		getWindow().addWindow(window);
		window.addListener(new CloseListener() {

		    @Override
		    public void windowClose(CloseEvent e) {
			final DirNode targetDir = window.getSelectedDirNode();
			if (targetDir != null) {
			    getLog().getFileNode().recoverTo(targetDir);
			}
		    }
		});
	    }
	});
	return Collections.singletonList(btSelectDir);
    }
}

public class LogViewerFactory {

    public static Component createViewer(AbstractLog log) {
	if (log instanceof FileLog) {
	    if (log instanceof DeleteFileLog) {
		return new DeleteFileLogViewer((FileLog) log);
	    }
	    return new FileLogViewer((FileLog) log);
	}
	if (log instanceof DirLog) {

	    return new DirLogViewer((DirLog) log);
	}
	return new Label();
    }
}
