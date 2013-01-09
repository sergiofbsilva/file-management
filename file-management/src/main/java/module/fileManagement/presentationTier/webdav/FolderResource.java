package module.fileManagement.presentationTier.webdav;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.exception.NodeDuplicateNameException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.bennu.core.domain.exceptions.DomainException;

public class FolderResource extends AbstractResource<DirNode> implements CollectionResource, MakeCollectionableResource,
	PutableResource {

    private static final Logger log = LoggerFactory.getLogger(FolderResource.class);

    public FolderResource(DirNode node) {
	super(node);
    }

    private Resource internalChild(String childName) {
	final AbstractFileNode node = getNode().searchNode(childName);
	if (node != null && node.isAccessible()) {
	    return makeResource(node);
	}
	return null;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
	return internalChild(childName);
    }

    public Resource makeResource(final AbstractFileNode searchNode) {
	if (searchNode instanceof FileNode) {
	    return new FileResource((FileNode) searchNode);
	}
	if (searchNode instanceof DirNode) {
	    return new FolderResource((DirNode) searchNode);
	}
	return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
	log.warn("getChildren");
	return getInternalChildren();
    }

    private List<? extends Resource> getInternalChildren() {
	final List<Resource> resources = new ArrayList<Resource>();
	for (AbstractFileNode node : getNode().getChild()) {
	    if (node.isAccessible()) {
		resources.add(makeResource(node));
	    }
	}
	return resources;
    }

    @Override
    public Date getModifiedDate() {
	return null;
    }

    @Override
    public Date getCreateDate() {
	return null;
    }

    @Override
    public String getName() {
	return getNode().getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException,
	    BadRequestException {
	try {
	    return new FolderResource(getNode().createDir(newName));
	} catch (NodeDuplicateNameException ndne) {
	    throw new BadRequestException(ndne.getLocalizedMessage(), ndne);
	}
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException,
	    ConflictException, NotAuthorizedException, BadRequestException {
	try {
	    final FileNode fileNode = getNode().createFile(IOUtils.toByteArray(inputStream), newName, newName,
		    length.longValue(), getNode().getContextPath());
	    return new FileResource(fileNode);
	} catch (DomainException ndne) {
	    throw new BadRequestException(ndne.getLocalizedMessage(), ndne);
	}
    }
}
