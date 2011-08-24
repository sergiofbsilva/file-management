package module.fileManagement.presentationTier.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.presentationTier.DownloadUtil;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import pt.ist.fenixWebFramework.Config.CasConfig;
import pt.ist.fenixWebFramework.FenixWebFramework;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

public class DownloadDocumentServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
	process(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
	process(request, response);
    }

    private void process(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
	final String uri = request.getRequestURI();
	final int lastIndexOf = uri.lastIndexOf('/');
	if (lastIndexOf >= 0) {
	    final String oid = uri.substring(lastIndexOf + 1);
	    final DomainObject domainObject = AbstractDomainObject.fromExternalId(oid);
	    if (domainObject != null) {
		final Document document;
		final VersionedFile versionedFile;
		if (domainObject instanceof Document) {
		    document = (Document) domainObject;
		    versionedFile = document.getLastVersionedFile();
		} else if (domainObject instanceof VersionedFile) {
		    versionedFile = (VersionedFile) domainObject;
		    document = versionedFile.getConnectedDocument();
		} else {
		    document = null;
		    versionedFile = null;
		}
		if (document != null && versionedFile != null) {
		    process(request, response, document, versionedFile);
		}
	    }
	} else {
	    // TODO
	}
    }

    private void process(final HttpServletRequest request, final HttpServletResponse response,
	    final Document document, final VersionedFile versionedFile) throws IOException {
	for (final FileNode fileNode : document.getFileNodeSet()) {
	    if (fileNode.isAccessible() /*|| fileNode instanceof SharedFileNode*/) {
		process(request, response, versionedFile);
	    } else {
		final User user = UserView.getCurrentUser();
		if (user == null) {
		    final String serverName = request.getServerName();
		    final CasConfig casConfig = FenixWebFramework.getConfig().getCasConfig(serverName);
		    if (casConfig != null && casConfig.isCasEnabled()) {
			final String casLoginUrl = casConfig.getCasLoginUrl();
			final StringBuilder url = new StringBuilder();
			url.append(casLoginUrl);
			url.append(DownloadUtil.getDownloadUrl(request, versionedFile));
			response.sendRedirect(url.toString());
			return;
		    }
		}
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access to this resource is restrited");
	    }
	}
    }

    private void process(final HttpServletRequest request, final HttpServletResponse response,
	    final VersionedFile versionedFile) throws IOException {
	response.setContentType(versionedFile.getContentType());
	response.setHeader("Content-disposition", "attachment; filename=" + versionedFile.getFilename());

	final ServletOutputStream outputStream = response.getOutputStream();
	outputStream.write(versionedFile.getContent());
	outputStream.close();
    }

}

