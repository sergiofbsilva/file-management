package module.fileManagement.presentationTier;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.VersionedFile;
import pt.ist.fenixframework.DomainObject;

import com.vaadin.Application;

public class DownloadUtil {

    public static String getDownloadUrl(final HttpServletRequest request, final VersionedFile versionedFile) {
	return getDownloadUrlForDomainObject(request, versionedFile);
    }

    public static String getDownloadUrl(final HttpServletRequest request, final Document document) {
	return getDownloadUrlForDomainObject(request, document);
    }

    protected static String getDownloadUrlForDomainObject(final HttpServletRequest request, final DomainObject domainObject) {
	return getDownloadUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), domainObject);
    }

    public static String getDownloadUrl(final Application application, final DomainObject domainObject) {
	final URL url = application.getURL();
	final String path = url.getPath();
	return getDownloadUrl(url.getProtocol(), url.getHost(), url.getPort(), path.substring(0, path.length() - 7), domainObject);
    }

    public static String getDownloadUrl(final String scheme, final String servername, final int serverPort,
	    final String contextPath, final DomainObject domainObject) {
	final StringBuilder url = new StringBuilder();
	url.append(scheme);
	url.append("://");
	url.append(servername);
	if (serverPort > 0 && serverPort != 80 && serverPort != 443) {
	    url.append(":");
	    url.append(serverPort);
	}
	url.append(contextPath);
	url.append("download/");
	url.append(domainObject.getExternalId());
	return url.toString();
    }
}
