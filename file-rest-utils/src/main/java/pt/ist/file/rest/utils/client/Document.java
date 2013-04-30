package pt.ist.file.rest.utils.client;

import java.util.Map;

import org.json.simple.JSONObject;

import pt.ist.fenixframework.Atomic;
import pt.ist.file.rest.utils.domain.RemoteDocument;

import com.sun.jersey.api.client.WebResource;

public class Document implements IDocument {
    private final FMSRestClient fmsRestClient;
    private String remoteOid;

    public Document(final FMSRestClient fmsRestClient, final String remoteOid) {
        setRemoteExternalId(remoteOid);
        this.fmsRestClient = fmsRestClient;
    }

    private void setRemoteExternalId(final String remoteOid) {
        this.remoteOid = remoteOid;
    }

    @Override
    public String info() {
        return getWebResource().path("info/" + remoteOid).get(String.class);
    }

    private WebResource getWebResource() {
        return fmsRestClient.getWebResource();
    }

    @Override
    public byte[] content() {
        return getWebResource().path("download/" + remoteOid).get(String.class).getBytes();
    }

    @Override
    public String metadata(final Map<String, String> metadataMap) {
        final JSONObject object = new JSONObject(metadataMap);
        return getWebResource().path("metadata/" + remoteOid).queryParam("metadata", object.toJSONString()).put(String.class);
    }

    @Override
    public String getRemoteExternalId() {
        return remoteOid;
    }

    @Override
    public FMSRestClient getFMSRestClient() {
        return fmsRestClient;
    }

    @Atomic
    public RemoteDocument persist() {
        return new RemoteDocument(this);
    }
}
