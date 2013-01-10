package module.fileManagement.presentationTier.webdav;

import io.milton.http.fs.SimpleSecurityManager;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;

import java.util.Map;

import module.fileManagement.domain.webdav.WebdavAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.bennu.core.domain.User;

public class FMSSecurityManager extends SimpleSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(FMSSecurityManager.class);

    public FMSSecurityManager(DigestGenerator digestGenerator) {
	super(digestGenerator);
    }

    public FMSSecurityManager(String realm, Map<String, String> nameAndPasswords) {
	super(realm, nameAndPasswords);
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
	log.trace("Authenticate : {} {} {}",
		new Object[] { getClass().getName(), digestRequest.getUser(), digestRequest.getResponseDigest() });
	if (getDigestGenerator() == null) {
	    throw new RuntimeException("No digest generator is configured");
	}
	final String username = digestRequest.getUser();
	final User user = User.findByUsername(username);
	if (user.hasWebdavAuthentication()) {
	    final WebdavAuthentication webDavAuth = user.getWebdavAuthentication();
	    String actualPassword = webDavAuth.getToken();
	    String serverResponse = getDigestGenerator().generateDigest(digestRequest, actualPassword);
	    String clientResponse = digestRequest.getResponseDigest();
	    if (serverResponse.equals(clientResponse)) {
		log.info("authenticate : user {} authenticated", username);
		return user;
	    } else {
		log.warn("authenticate : authentication for user {} failed", username);
		return null;
	    }
	} else {
	    log.warn("authenticate : user {} doesn't have WebDav credentials", username);
	    return null;
	}
    }
}
