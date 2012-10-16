package pt.ist.file.rest.utils.client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import module.webserviceutils.domain.ClientHost;
import module.webserviceutils.domain.HostSystem;
import pt.ist.bennu.core.domain.User;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

public class FMSRestClient {

    private final WebResource webResource;
    private final ClientHost clientHost;
    private final User userToLogin;

    public FMSRestClient(final String clientHostName, final User userToLogin) {
	this(HostSystem.getClientHost(clientHostName), userToLogin);
    }

    public FMSRestClient(final ClientHost clientHost, final User userToLogin) {
	this.clientHost = clientHost;
	this.userToLogin = userToLogin;
	final Client client = new Client();
	client.addFilter(new AuthFilter());
	webResource = client.resource(clientHost.getServerUrl());
    }

    class AuthFilter extends ClientFilter {

	@Override
	public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
	    if (!clientHost.isEnabled()) {
		throw new WebApplicationException(Status.FORBIDDEN);
	    }
	    cr.getHeaders().putSingle("__username__", clientHost.getUsername());
	    cr.getHeaders().putSingle("__password__", clientHost.getPassword());
	    cr.getHeaders().putSingle("__userToLogin__", userToLogin.getUsername());
	    return getNext().handle(cr);
	}

    }

    public Folder getRootDirectory() {
	return getDirectory(null);
    }

    public ClientHost getClientHost() {
	return clientHost;
    }

    public User getUserToLogin() {
	return userToLogin;
    }

    public Folder getDirectory(final String remoteOid) {
	return clientHost == null ? null : new Folder(this, remoteOid);
    }

    public WebResource getWebResource() {
	return webResource;
    }
}
