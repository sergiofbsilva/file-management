/*
 * @(#)LogViewerFactory.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
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
import myorg.applicationTier.Authenticate.UserView;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @author Sérgio Silva
 * 
 */
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
	final FileNode fileNode = getLog().getFileNode();
	return UserView.getCurrentUser().equals(fileNode.getOwner()) && fileNode.isInTrash();
    }

    @Override
    public String createTargetNodeLink() {
	return getLog().getFileNode().getDisplayName();
    }

    private void updateParent() {

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
			    getParentContainer().reloadContent();
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
