package module.fileManagement.presentationTier.data;

import module.fileManagement.domain.Document;
import pt.ist.vaadinframework.data.AbstractBufferedContainer;

public class DocumentContainer extends AbstractBufferedContainer<Document, Object, DocumentItem> {
    public DocumentContainer() {
	super(Document.class);
    }

    {
	addContainerProperty("displayName", String.class, null);
    }

    @Override
    protected DocumentItem makeItem(Document itemId) {
	return new DocumentItem(itemId);
    }

    @Override
    protected DocumentItem makeItem(Class<? extends Document> type) {
	return new DocumentItem(type);
    }
}
