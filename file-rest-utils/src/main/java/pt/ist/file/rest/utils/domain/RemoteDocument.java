package pt.ist.file.rest.utils.domain;

import java.util.Map;

import module.webserviceutils.domain.ClientHost;
import pt.ist.file.rest.utils.client.Document;
import pt.ist.file.rest.utils.client.FMSRestClient;

public class RemoteDocument extends RemoteDocument_Base {

    private Document document;

    public RemoteDocument(final Document document) {
        this(document.getFMSRestClient().getClientHost(), document.getFMSRestClient().getUserToLogin(), document
                .getRemoteExternalId());
        this.document = document;
    }

    private RemoteDocument(final ClientHost clientHost, final String userToLogin, final String remoteExternalId) {
        super();
        setClientHost(clientHost);
        setRemoteExternalId(remoteExternalId);
        setUsername(userToLogin);
    }

    public Document getDocument() {
        if (document == null) {
            document = new Document(new FMSRestClient(getClientHost(), getUsername()), getRemoteExternalId());
        }
        return document;
    }

    @Override
    public String info() {
        return getDocument().info();
    }

    @Override
    public byte[] content() {
        return getDocument().content();
    }

    @Override
    public String metadata(final Map<String, String> metadataMap) {
        return getDocument().metadata(metadataMap);
    }

    @Override
    public FMSRestClient getFMSRestClient() {
        throw new UnsupportedOperationException();
    }

}
