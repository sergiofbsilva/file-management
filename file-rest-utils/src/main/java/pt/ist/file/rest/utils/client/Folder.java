package pt.ist.file.rest.utils.client;

import java.io.File;

import javax.ws.rs.core.MediaType;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.file.rest.utils.domain.RemoteFolder;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

public class Folder implements IFolder {
    private final FMSRestClient fmsRestClient;
    private String remoteOid;

    public Folder(final FMSRestClient fmsRestClient) {
        this(fmsRestClient, null);
    }

    public Folder(final FMSRestClient fmsRestClient, final String rootDirNodeOid) {
        setRemoteExternalId(rootDirNodeOid);
        this.fmsRestClient = fmsRestClient;
    }

    public void setRemoteExternalId(final String externalId) {
        this.remoteOid = externalId;
    }

    @Override
    public String getRemoteExternalId() {
        if (remoteOid == null) {
            remoteOid = getWebResource().path("root").get(String.class);
            setRemoteExternalId(remoteOid);
        }
        return remoteOid;
    }

    private WebResource getWebResource() {
        return fmsRestClient.getWebResource();
    }

    @Override
    public Document createDocument(final File file2upload) {
        final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", file2upload);
        final FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.bodyPart(fileDataBodyPart);

        final String documentOid =
                getWebResource().path("upload").path(getRemoteExternalId()).type(MediaType.MULTIPART_FORM_DATA_TYPE)
                        .post(String.class, formDataMultiPart);
        if (!"-1".equals(documentOid)) {
            return new Document(fmsRestClient, documentOid);
        }
        throw new ClientHandlerException("Couldn't create file" + file2upload.getName());
    }

    @Override
    public Folder createFolder(final String name) {
        final String newDir =
                getWebResource().path("createDir").path(getRemoteExternalId()).queryParam("name", name).put(String.class);
        return new Folder(fmsRestClient, newDir);
    }

    @Service
    public RemoteFolder persist() {
        return new RemoteFolder(this);
    }

    @Override
    public FMSRestClient getFMSRestClient() {
        return fmsRestClient;
    }
}
