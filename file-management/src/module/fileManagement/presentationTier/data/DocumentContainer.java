package module.fileManagement.presentationTier.data;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.Document;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.vaadinframework.data.BufferedContainer;
import pt.ist.vaadinframework.data.HintedProperty;



public class DocumentContainer extends BufferedContainer<Document, Object, DocumentItem> {

	public DocumentContainer(HintedProperty value, Class<? extends Document> elementType) {
	    super(value, elementType);
	    setWriteThrough(false);
	}
	
	@SuppressWarnings("rawtypes")
	public DocumentContainer() {
	    this(new ObjectHintedProperty<Collection>(new HashSet<Document>(), Collection.class), Document.class);
	}
	
	{
	    addContainerProperty("displayName", String.class, null);
	}

	@Override
	protected DocumentItem makeItem(HintedProperty itemId) {
	   return new DocumentItem(itemId);
	}
}
