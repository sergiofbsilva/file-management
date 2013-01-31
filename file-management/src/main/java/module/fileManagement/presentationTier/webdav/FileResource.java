package module.fileManagement.presentationTier.webdav;

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.GetableResource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import module.fileManagement.domain.FileNode;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileResource extends AbstractResource<FileNode> implements GetableResource {

	private static final Logger log = LoggerFactory.getLogger(FileResource.class);

	public FileResource(FileNode fileNode, FolderResource parent) {
		super(fileNode, parent);
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException, BadRequestException, NotFoundException {
		log.info("sendContent");
		sendInternalContent(out, range, params, contentType);
	}

	private void sendInternalContent(OutputStream out, Range range, Map<String, String> params, String contentType)
			throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		if (getNode().isAccessible()) {
			out.write(getNode().getDocument().getLastVersionedFile().getContent());
			out.close();
		} else {
			throw new NotAuthorizedException(this);
		}
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public String getContentType(String accepts) {
		return getNode().getDocument().getLastVersionedFile().getContentType();
	}

	@Override
	public Long getContentLength() {
		return getNode().getFilesize();
	}

	@Override
	public String getName() {
		return getNode().getDisplayName();
	}

	@Override
	public Date getModifiedDate() {
		final DateTime modifiedDate = getNode().getDocument().getLastModifiedDate();
		return modifiedDate != null ? modifiedDate.toDate() : null;
	}

	@Override
	public Date getCreateDate() {
		final DateTime creationDate = getNode().getDocument().getCreationDate();
		return creationDate != null ? creationDate.toDate() : null;
	}

}
