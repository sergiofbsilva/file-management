package module.fileManagement.presentationTier.webdav;

import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.DigestResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepositoriesResource extends Authenticator implements PropFindableResource, CollectionResource, DigestResource {

	private static final Logger log = LoggerFactory.getLogger(UserRepositoriesResource.class);

	private Set<DirNode> userRepositories;

	public UserRepositoriesResource() {
	}

	private Set<DirNode> getUserRepositories() throws NotAuthorizedException {
		if (userRepositories == null) {
			userRepositories = FileRepository.getAvailableRepositories();
		}
		return userRepositories;
	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public Date getModifiedDate() {
		return null;
	}

	@Override
	public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
		log.trace("child: {}", childName);
		for (DirNode repository : getUserRepositories()) {
			if (repository.getName().equals(childName)) {
				return new FolderResource(repository, null);
			}
		}
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
		final List<FolderResource> repositories = new ArrayList<FolderResource>();
		for (DirNode repository : getUserRepositories()) {
			repositories.add(new FolderResource(repository, null));
		}
		return repositories;
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public String getName() {
		return "Repositórios Disponíveis";
	}

	@Override
	public String getRealm() {
		return FMSMiltonConfigurator.REALM;
	}

	@Override
	public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
		return null;
	}

}
