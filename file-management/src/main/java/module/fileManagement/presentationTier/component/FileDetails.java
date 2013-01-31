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

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.presentationTier.pages.DocumentExtendedInfo;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

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
			addOperation(createRenameLink());
			addOperation(createShareLink());
			if (!getNode().isShared()) {
				addOperation(createMoveLink());
			}
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
			addStyleName(BaseTheme.BUTTON_LINK);
			sameNameFailed = StringUtils.EMPTY;
		}

		private boolean dirHasFile(String filename) {
			AbstractFileNode searchNode = getNode().getParent().searchNode(filename);
			return searchNode != null && !searchNode.equals(getNode());
		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			try {
				file = UploadFileArea.DEFAULT_FACTORY.createFile(filename, mimeType);
				final String displayName = FileManagementSystem.getNewDisplayName(getDocument().getDisplayName(), filename);
				if (dirHasFile(displayName)) {
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
			} else {
				FileManagementSystem.showWarning(getApplication(), "Upload falhou. Tente novamente!");
			}
		}

		@Override
		@Service
		public void uploadSucceeded(SucceededEvent event) {
			final Document document = getDocument();
			final String displayName = FileManagementSystem.getNewDisplayName(document.getDisplayName(), event.getFilename());
			getNode().addNewVersion(file, event.getFilename(), displayName, event.getLength());
			getNodeItem().setValue(getNode());
			String msg = String.format("Nova versão %s", document.getVersionNumber());
			FileManagementSystem.show(getApplication(), "Upload", msg, Window.Notification.TYPE_HUMANIZED_MESSAGE);
			sameNameFailed = StringUtils.EMPTY;
		}

	}

	public Component createUploadLink() {
		return new SingleFileUpload();
	}

	@Override
	public <T extends AbstractFileNode> T getNodeToDownload() {
		return (T) getNode();
	}
}
