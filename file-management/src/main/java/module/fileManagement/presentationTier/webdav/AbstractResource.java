package module.fileManagement.presentationTier.webdav;

import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import pt.ist.fenixWebFramework.services.Service;

public abstract class AbstractResource<T extends AbstractFileNode> extends Authenticator implements PropFindableResource,
	DeletableResource, MoveableResource {

    private final T node;

    public AbstractResource(T node) {
	this.node = node;
    }

    public T getNode() {
	return node;
    }

    @Override
    public String getUniqueId() {
	return getNode().getExternalId();
    }

    @Override
    public String getName() {
	return getNode().getDisplayName();
    }

    @Override
    public String getRealm() {
	return FMSMiltonConfigurator.REALM;
    }

    @Override
    public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
	return null;
    }

    @Override
    @Service
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
	getNode().trash();
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException,
	    BadRequestException {
	if (rDest instanceof FolderResource) {
	    final DirNode destNode = ((FolderResource) rDest).getNode();
	    try {
		getNode().moveTo(destNode);
	    } catch (NodeDuplicateNameException ndne) {
		throw new BadRequestException(ndne.getLocalizedMessage(), ndne);
	    }
	}
    }
}
