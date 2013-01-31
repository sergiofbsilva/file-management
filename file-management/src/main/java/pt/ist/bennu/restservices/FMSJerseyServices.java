package pt.ist.bennu.restservices;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.domain.metadata.Metadata;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/fms")
public class FMSJerseyServices {

	private Document resolveDocument(final String documentOid) {
		// Authenticate.authenticate(User.findByUsername("ist152416"));
		final Document doc = AbstractDomainObject.fromExternalId(documentOid);
		if (doc == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!doc.isAccessible()) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return doc;
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("info/{oid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getInfo(@PathParam("oid") final String documentOid) {
		final Document doc = resolveDocument(documentOid);
		final Collection<Metadata> metadataSet = doc.getRecentMetadata();
		final JSONObject jsonDoc = new JSONObject();
		jsonDoc.put("displayName", doc.getDisplayName());
		jsonDoc.put("filesize", doc.getFilesize());
		jsonDoc.put("versionNumber", doc.getVersionNumber());

		final JSONObject jsonMetadata = new JSONObject();
		for (final Metadata metadata : metadataSet) {
			jsonMetadata.put(metadata.getKeyValue(), metadata.getPresentationValue());
		}
		jsonDoc.put("metadata", jsonMetadata);
		return jsonDoc.toJSONString();
	}

	@PUT
	@Path("metadata/{oid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String addMetadata(@PathParam("oid") final String documentOid, @QueryParam("metadata") final String metadata) {
		final Document doc = resolveDocument(documentOid);
		final JSONObject parse = (JSONObject) JSONValue.parse(metadata);
		final Map<String, String> metadataMap = new HashMap<String, String>();
		metadataMap.putAll(parse);
		doc.addMetadata(metadataMap);
		return getInfo(documentOid);
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("upload/{dirNodeOid}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String upload(@PathParam("dirNodeOid") final String dirNodeOid, @FormDataParam("file") final File file,
			@FormDataParam("file") final FormDataContentDisposition fileDetails) {
		Authenticate.authenticate(User.findByUsername("ist152416"));
		final DirNode dirNode = AbstractDomainObject.fromExternalId(dirNodeOid);
		try {
			final FileNode createFile =
					dirNode.createFile(file, fileDetails.getFileName(), fileDetails.getSize(), dirNode.getContextPath());
			return createFile.getDocument().getExternalId();
		} catch (final DomainException de) {
			return "-1";
		}
	}

	@GET
	@Path("download/{oid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam("oid") final String documentOid) {
		final Document document = resolveDocument(documentOid);
		for (final FileNode fileNode : document.getFileNodeSet()) {
			if (fileNode.isAccessible()) {
				final VersionedFile versionedFile = document.getLastVersionedFile();
				return createFileAttachmentResponse(versionedFile);
			}
		}
		return Response.noContent().build();
	}

	private Response createFileAttachmentResponse(final VersionedFile versionedFile) {
		return Response.ok(versionedFile.getContent(), versionedFile.getContentType())
				.header("Content-disposition", "attachment; filename=" + versionedFile.getFilename()).build();
	}

	@GET
	@Path("root")
	public Response getRoot() {
		final User currentUser = Authenticate.getCurrentUser();
		if (currentUser == null) {
			return Response.noContent().build();
		}
		return Response.ok(FileRepository.getOrCreateFileRepository(currentUser).getExternalId(), MediaType.TEXT_PLAIN).build();
	}

	@SuppressWarnings("unchecked")
	@PUT
	@Path("createDir/{oid}")
	public Response createDir(@PathParam("oid") final String rootDirNodeOid, @QueryParam("name") final String name) {
		if ("-1".equals(rootDirNodeOid)) {

		}
		final DirNode fileRepository = AbstractDomainObject.fromExternalId(rootDirNodeOid);
		DirNode searchDir = fileRepository.searchDir(name);
		if (searchDir == null) {
			searchDir = fileRepository.createDir(name, fileRepository.getContextPath());
		}
		return Response.ok(searchDir.getExternalId(), MediaType.TEXT_PLAIN).build();
	}
}
