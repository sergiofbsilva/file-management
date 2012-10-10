package pt.ist.file.rest.utils.client;

import java.io.File;

public interface IFolder extends IFileNode {

    public IDocument createDocument(final File file2upload);

    public IFolder createDirectory(final String name);

}