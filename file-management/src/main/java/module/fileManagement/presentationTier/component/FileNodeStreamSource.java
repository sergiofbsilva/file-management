package module.fileManagement.presentationTier.component;

import java.io.IOException;
import java.io.InputStream;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

import com.vaadin.terminal.StreamResource.StreamSource;

public class FileNodeStreamSource extends InputStream implements StreamSource {

	private final String fileNodeOid;
	private InputStream stream;

	public FileNodeStreamSource(final FileNode fileNode) {
		fileNodeOid = fileNode.getExternalId();
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public InputStream getStream() {
		if (stream == null) {
			final FileNode fileNode = AbstractDomainObject.fromExternalId(fileNodeOid);
			final Document document = fileNode.getDocument();
			final GenericFile file = document.getLastVersionedFile();
			stream = file.getStream();
		}
		return this;
	}

	@Override
	public void close() throws IOException {
		super.close();
		stream.close();
		stream = null;
	}

	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public synchronized void mark(final int readlimit) {
		stream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return stream.read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return stream.read(b, off, len);
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.read();
	}

	@Override
	public long skip(final long n) throws IOException {
		return stream.skip(n);
	}

}
