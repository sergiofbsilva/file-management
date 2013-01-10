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

    public FolderResource(DirNode node, FolderResource parent) {
	super(node, parent);
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
	final AbstractFileNode node = getNode().searchNode(childName);
	if (node != null && node.isAccessible()) {
	    return makeResource(node);
	}
	return null;
    }

    public Resource makeResource(final AbstractFileNode searchNode) {
	if (searchNode instanceof FileNode) {
	    return new FileResource((FileNode) searchNode, this);
	}
	if (searchNode instanceof DirNode) {
	    return new FolderResource((DirNode) searchNode, this);
	}
	return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
	log.trace("getChildren");
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
	    return new FolderResource(getNode().createDir(newName), this);
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
	    return new FileResource(fileNode, this);
	} catch (DomainException ndne) {
	    throw new BadRequestException(ndne.getLocalizedMessage(), ndne);
	}
    }

}
