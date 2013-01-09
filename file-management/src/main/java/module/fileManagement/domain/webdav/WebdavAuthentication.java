package module.fileManagement.domain.webdav;

import java.math.BigInteger;
import java.security.SecureRandom;

import pt.ist.bennu.core.domain.User;
import pt.ist.fenixWebFramework.services.Service;

public class WebdavAuthentication extends WebdavAuthentication_Base {

    public WebdavAuthentication(User user) {
	super();
	setUser(user);
	generateNewToken();
    }

    private String generateToken() {
	return new BigInteger(130, new SecureRandom()).toString(32);
    }

    @Service
    public void generateNewToken() {
	setToken(generateToken());
    }

    @Service
    public static WebdavAuthentication getOrCreateWebdavAuthentication(User user) {
	if (user.hasWebdavAuthentication()) {
	    return user.getWebdavAuthentication();
	}
	return new WebdavAuthentication(user);
    }

}
