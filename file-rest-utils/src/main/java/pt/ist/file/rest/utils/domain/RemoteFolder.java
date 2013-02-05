package pt.ist.file.rest.utils.domain;

import java.io.File;

import module.webserviceutils.domain.ClientHost;
import pt.ist.file.rest.utils.client.FMSRestClient;
import pt.ist.file.rest.utils.client.Folder;
import pt.ist.file.rest.utils.client.IDocument;

public class RemoteFolder extends RemoteFolder_Base {

	private Folder folder;

	public RemoteFolder(final Folder folder) {
		this(folder.getFMSRestClient(), folder.getRemoteExternalId());
		this.folder = folder;
	}

	private RemoteFolder(final FMSRestClient fmsRestClient, final String remoteExternalId) {
		this(fmsRestClient.getClientHost(), fmsRestClient.getUserToLogin(), remoteExternalId);
	}

	private RemoteFolder(final ClientHost clientHost, final String userToLogin, final String remoteExternalId) {
		super();
		setClientHost(clientHost);
		setRemoteExternalId(remoteExternalId);
		setUsername(userToLogin);
	}

	private Folder getFolder() {
		if (folder == null) {
			folder = new Folder(new FMSRestClient(getClientHost(), getUsername()), getRemoteExternalId());
		}
		return folder;
	}

	@Override
	public IDocument createDocument(final File file2upload) {
		return getFolder().createDocument(file2upload);
	}

	@Override
	public Folder createFolder(final String name) {
		return getFolder().createFolder(name);
	}

	@Override
	public FMSRestClient getFMSRestClient() {
		throw new UnsupportedOperationException();
	}

}
