package module.fileManagement.presentationTier.webdav;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.bennu.core.domain.User;

public abstract class Authenticator implements DigestResource, Resource {

	private static final Logger log = LoggerFactory.getLogger(Authenticator.class);

	@Override
	public Object authenticate(String user, String password) {
		throw new UnsupportedOperationException("Basic authentication is not supported.");
	}

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		log.trace("Authenticate : {} {} {}",
				new Object[] { getClass().getName(), digestRequest.getUser(), digestRequest.getResponseDigest() });
		final String username = digestRequest.getUser();
		if (StringUtils.isEmpty(username)) {
			log.warn("authenticate: User not set!");
			return null;
		}
		User user = User.findByUsername(username);
		if (user != null) {
			log.info("authenticate : login user " + username);
		} else {
			log.warn("authenticate : user not found : " + username);
		}
		return user;
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		log.trace("authorize: {}", auth == null ? "null" : auth.toString());
		return auth != null;
	}

	@Override
	public boolean isDigestAllowed() {
		return true;
	}
}
