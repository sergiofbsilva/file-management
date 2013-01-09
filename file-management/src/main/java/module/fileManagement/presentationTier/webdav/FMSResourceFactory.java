package module.fileManagement.presentationTier.webdav;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FMSResourceFactory implements ResourceFactory {
    private static final Logger log = LoggerFactory.getLogger(FMSResourceFactory.class);
    private static final String WEBDAV = "webdav";

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
	url = url.substring(url.indexOf(WEBDAV) + WEBDAV.length(), url.length());
	log.warn("getResource: {} {}", host, url);
	Path path = Path.path(url);
	Resource r = find(path);
	return r;
    }

    private Resource find(Path path) throws NotAuthorizedException, BadRequestException {
	log.warn("find: {}", path.toString());
	if (path.isRoot()) {
	    return new UserRepositoriesResource();
	}
	Resource rParent = find(path.getParent());
	if (rParent == null) {
	    return null;
	}
	if (rParent instanceof CollectionResource) {
	    CollectionResource folder = (CollectionResource) rParent;
	    return folder.child(path.getName());
	}
	return null;
    }
}
