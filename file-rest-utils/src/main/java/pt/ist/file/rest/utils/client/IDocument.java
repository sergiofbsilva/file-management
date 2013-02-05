package pt.ist.file.rest.utils.client;

import java.util.Map;

public interface IDocument extends IFileNode {

    public String info();

    public byte[] content();

    public String metadata(final Map<String, String> metadataMap);
}