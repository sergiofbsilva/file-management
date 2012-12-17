package module.fileManagement.presentationTier.servlet;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.domain.log.AccessFileLog;
import module.fileManagement.presentationTier.DownloadUtil;
import pt.ist.bennu.core.applicationTier.Authenticate;
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
		dispatch(request, response, domainObject);
	    }
	} else {
	    // TODO
	}
    }

    private void dispatch(HttpServletRequest request, HttpServletResponse response, DomainObject domainObject) {
	try {
	    if (domainObject instanceof FileNode) {
		downloadFileNode(request, response, (FileNode) domainObject);
	    } else if (domainObject instanceof VersionedFile) {
		downloadVersionedFile(request, response, (VersionedFile) domainObject);
	    } else if (domainObject instanceof DirNode) {
		downloadDirNode(request, response, (DirNode) domainObject);
	    }
	} catch (IOException io) {
	    throw new RuntimeException(io);
	}
    }

    @Service
    private static void saveAccessLog(FileNode fileNode) {
	new AccessFileLog(Authenticate.getCurrentUser(), fileNode.getParent().getContextPath(), fileNode);
    }

    public static void downloadFileNode(final HttpServletRequest request, final HttpServletResponse response,
	    final FileNode fileNode) throws IOException, DomainException {

	final Document document = fileNode.getDocument();
	final VersionedFile versionedFile = document.getLastVersionedFile();
	downloadVersionedFile(request, response, versionedFile);
    }

    public static void downloadVersionedFile(final HttpServletRequest request, final HttpServletResponse response,
	    final VersionedFile versionedFile) throws IOException {

	final Document connectedDocument = versionedFile.getConnectedDocument();
	FileNode fileNode = null;
	for (final FileNode node : connectedDocument.getFileNodeSet()) {
	    if (node.isAccessible()) {
		fileNode = node;
		break;
	    }
	}

	if (fileNode != null) {
	    if (connectedDocument.mustSaveAccessLog()) {
		saveAccessLog(fileNode);
	    }
	    response.setContentType(versionedFile.getContentType());
	    response.setHeader("Content-disposition", "attachment; filename=" + versionedFile.getFilename());
	    final ServletOutputStream outputStream = response.getOutputStream();
	    outputStream.write(versionedFile.getContent());
	    outputStream.close();
	} else {
	    redirect(request, response, versionedFile);
	}
    }

    public static void downloadDirNode(final HttpServletRequest request, final HttpServletResponse response, final DirNode dirNode)
	    throws IOException {
	if (!dirNode.isAccessible()) {
	    redirect(request, response, dirNode);
	}
	response.setContentType("application/zip");
	response.setHeader("Content-disposition", "attachment; filename=" + dirNode.getName() + ".zip");
	ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
	writeDirToZipStream(out, dirNode, "");
	out.close();
    }

    private static void redirect(final HttpServletRequest request, final HttpServletResponse response,
	    final DomainObject domainObject) throws IOException {
	final User user = Authenticate.getCurrentUser();
	if (user == null) {
	    final String serverName = request.getServerName();
	    final CasConfig casConfig = FenixWebFramework.getConfig().getCasConfig(serverName);
	    if (casConfig != null && casConfig.isCasEnabled()) {
		final String casLoginUrl = casConfig.getCasLoginUrl();
		final StringBuilder url = new StringBuilder();
		url.append(casLoginUrl);
		url.append(DownloadUtil.getDownloadUrl(request, domainObject));
		response.sendRedirect(url.toString());
		return;
	    }
	}
	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access to this resource is restrited");
    }

    private static void writeDirToZipStream(ZipOutputStream out, DirNode dirNode, String relativePath) throws IOException,
	    DomainException {
	for (AbstractFileNode node : dirNode.getChild()) {
	    if (node instanceof DirNode) {
		if (node.isAccessible()) {
		    writeDirToZipStream(out, (DirNode) node, relativePath + node.getDisplayName() + "/");
		}
	    } else if (node instanceof FileNode) {
		final FileNode fileNode = (FileNode) node;
		if (fileNode.isAccessible()) {
		    ZipEntry e = new ZipEntry(relativePath + fileNode.getDisplayName());
		    out.putNextEntry(e);
		    out.write(fileNode.getDocument().getLastVersionedFile().getContent());
		    out.closeEntry();
		}
	    }
	}
    }

}
