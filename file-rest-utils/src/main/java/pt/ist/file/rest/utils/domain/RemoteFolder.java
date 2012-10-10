package pt.ist.file.rest.utils.domain;

import java.io.File;

import module.webserviceutils.domain.ClientHost;
import pt.ist.bennu.core.domain.User;
import pt.ist.file.rest.utils.client.FMSRestClient;
import pt.ist.file.rest.utils.client.Folder;
import pt.ist.file.rest.utils.client.IDocument;
import pt.ist.file.rest.utils.client.IFolder;

public class RemoteFolder extends RemoteFolder_Base {

    private Folder folder;

    public RemoteFolder(final Folder folder) {
	this(folder.getFMSRestClient(), folder.getRemoteExternalId());
	this.folder = folder;
    }

    private RemoteFolder(final FMSRestClient fmsRestClient, final String remoteExternalId) {
	this(fmsRestClient.getClientHost(), fmsRestClient.getUserToLogin(), remoteExternalId);
    }

    private RemoteFolder(final ClientHost clientHost, final User userToLogin, final String remoteExternalId) {
	super();
	setClientHost(clientHost);
	setRemoteExternalId(remoteExternalId);
	setUser(userToLogin);
    }

    private Folder getFolder() {
	if (folder == null) {
	    folder = new Folder(new FMSRestClient(getClientHost(), getUser()), getExternalId());
	}
	return folder;
    }

    @Override
    public IDocument createDocument(final File file2upload) {
	return getFolder().createDocument(file2upload);
    }

    @Override
    public IFolder createDirectory(final String name) {
	return getFolder().createDirectory(name);
    }

    @Override
    public FMSRestClient getFMSRestClient() {
	throw new UnsupportedOperationException();
    }

}
