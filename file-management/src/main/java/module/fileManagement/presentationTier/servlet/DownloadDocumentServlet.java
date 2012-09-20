package module.fileManagement.presentationTier.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.domain.log.AccessFileLog;
import module.fileManagement.presentationTier.DownloadUtil;
import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.fenixWebFramework.Config.CasConfig;
import pt.ist.fenixWebFramework.FenixWebFramework;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

public class DownloadDocumentServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
	    IOException {
	process(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
	    IOException {
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
		    process(request, response, document);
		}
	    }
	} else {
	    // TODO
	}
    }

    @Service
    private static void saveAccessLog(FileNode fileNode) {
	new AccessFileLog(UserView.getCurrentUser(), fileNode.getParent().getContextPath(), fileNode);
    }

    /**
     * 
     * @param request
     * @param response
     * @param fileNode
     * @throws IOException
     *             if there was a problem reading the file
     * @throws DomainException
     *             if the current user has no access to the given
     *             {@link FileNode}
     */
    public static void downloadDocument(final HttpServletRequest request, final HttpServletResponse response,
	    final FileNode fileNode) throws IOException, DomainException {
	if (!fileNode.isAccessible())
	    throw new DomainException("no.read.access.to.file", FileManagementSystem.BUNDLE);
	Document document = fileNode.getDocument();
	if (document.mustSaveAccessLog()) {
	    saveAccessLog(fileNode);
	}
	VersionedFile versionedFile = document.getLastVersionedFile();
	process(request, response, versionedFile);
    }

    private void process(final HttpServletRequest request, final HttpServletResponse response, final Document document)
	    throws IOException {
	for (final FileNode fileNode : document.getFileNodeSet()) {
	    if (fileNode.isAccessible() /* || fileNode instanceof SharedFileNode */) {
		downloadDocument(request, response, fileNode);
	    } else {
		final User user = UserView.getCurrentUser();
		if (user == null) {
		    final String serverName = request.getServerName();
		    final CasConfig casConfig = FenixWebFramework.getConfig().getCasConfig(serverName);
		    if (casConfig != null && casConfig.isCasEnabled()) {
			final String casLoginUrl = casConfig.getCasLoginUrl();
			final StringBuilder url = new StringBuilder();
			url.append(casLoginUrl);
			url.append(DownloadUtil.getDownloadUrl(request, document.getLastVersionedFile()));
			response.sendRedirect(url.toString());
			return;
		    }
		}
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access to this resource is restrited");
	    }
	}
    }

    private static void process(final HttpServletRequest request, final HttpServletResponse response, final VersionedFile versionedFile)
	    throws IOException {
	response.setContentType(versionedFile.getContentType());
	response.setHeader("Content-disposition", "attachment; filename=" + versionedFile.getFilename());

	final ServletOutputStream outputStream = response.getOutputStream();
	outputStream.write(versionedFile.getContent());
	outputStream.close();
    }

}
