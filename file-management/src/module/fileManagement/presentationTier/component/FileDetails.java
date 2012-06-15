/*
 * @(#)FileDetails.java
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
package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Link;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.themes.BaseTheme;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.DownloadUtil;
import module.fileManagement.presentationTier.pages.DocumentExtendedInfo;
import module.vaadin.ui.BennuTheme;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class FileDetails extends NodeDetails {

    public FileDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
	super(nodeItem, operationsVisible, infoVisible);
	addVisibleProperty("document.versionNumber");
	addVisibleProperty("document.lastModifiedDate");
	addVisibleProperty("document.typology");
    }

    public FileDetails(DomainItem<AbstractFileNode> nodeItem) {
	this(nodeItem, true, true);
    }

    @Override
    public FileNode getNode() {
	return (FileNode) super.getNode();
    }

    public Button createExtendedInfoLink() {
	Button btExtendedInfoLink = new Button(getMessage("label.extended.info"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		EmbeddedApplication.open(getApplication(), DocumentExtendedInfo.class, getNode().getExternalId());
	    }
	});
	btExtendedInfoLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btExtendedInfoLink;
    }

    public Link createDownloadLink() {
	final String url = DownloadUtil.getDownloadUrl(getApplication(), getNode().getDocument());
	final ExternalResource externalResource = new ExternalResource(url);
	return new Link(getMessage("label.download"), externalResource);
    }

    public Button createDeleteFileLink() {
	Button btDeleteFileLink = new Button(getMessage("label.delete"), new Button.ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		showDeleteDialog();
	    }
	});
	btDeleteFileLink.setStyleName(BaseTheme.BUTTON_LINK);
	return btDeleteFileLink;
    }

    @Override
    public void updateOperations() {
	if (isInfoVisible()) {
	    addOperation(createExtendedInfoLink());
	}
	addOperation(createDownloadLink());
	if (getNode().isWriteGroupMember()) {
	    addOperation(createShareLink());
	    addOperation(createDeleteFileLink());
	    addOperation(createUploadLink());
	} else {
	    if (getNode().isShared()) {
		addOperation(createDeleteFileLink());
	    }
	}
    }

    private class SingleFileUpload extends Upload implements Upload.SucceededListener, Upload.FailedListener, Receiver {
	private File file;
	private String sameNameFailed;

	public SingleFileUpload() {
	    super();
	    setImmediate(true);
	    setReceiver(this);
	    addListener((SucceededListener) this);
	    addListener((FailedListener) this);
	    setButtonCaption("Upload Nova Versão");
	    addStyleName(BennuTheme.BUTTON_LINK);
	    sameNameFailed = StringUtils.EMPTY;
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
	    try {
		file = UploadFileArea.DEFAULT_FACTORY.createFile(filename, mimeType);
		final String displayName = FileManagementSystem.getNewDisplayName(getDocument().getDisplayName(), filename);
		DirNode parent = getNode().getParent();
		if (parent.searchNode(displayName) != null) {
		    interruptUpload();
		    sameNameFailed = displayName;
		    return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}
		    };
		}
		return new FileOutputStream(file);
	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    }
	    return null;
	}

	public Document getDocument() {
	    return getNode().getDocument();
	}

	@Override
	public void uploadFailed(FailedEvent event) {
	    if (!sameNameFailed.equals(StringUtils.EMPTY)) {
		String warnMsg = String.format("Já existe um ficheiro com esse nome %s", sameNameFailed);
		FileManagementSystem.showWarning(getApplication(), warnMsg);
		sameNameFailed = StringUtils.EMPTY;
	    }
	    FileManagementSystem.showWarning(getApplication(), "Upload falhou. Tente novamente!");
	}

	@Override
	@Service
	public void uploadSucceeded(SucceededEvent event) {
	    final Document document = getDocument();
	    final String displayName = FileManagementSystem.getNewDisplayName(document.getDisplayName(), event.getFilename());
	    getNode().addNewVersion(file, event.getFilename(), displayName, event.getLength());
	    getNodeItem().setValue(getNode());
	    getApplication().getMainWindow().showNotification(
		    String.format("Upload da nova versão %s com sucesso", document.getVersionNumber()));
	    sameNameFailed = StringUtils.EMPTY;
	}

    }

    public Component createUploadLink() {
	return new SingleFileUpload();
    }
}
