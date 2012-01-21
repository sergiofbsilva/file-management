package module.fileManagement.presentationTier.data;

import module.fileManagement.domain.MetadataKey;
import pt.ist.vaadinframework.ui.DefaultFieldFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;

public class FMSFieldFactory extends DefaultFieldFactory {

    public FMSFieldFactory(String bundle) {
	super(bundle);
    }

    @Override
    protected String makeCaption(Item item, Object propertyId, Component uiContext) {
	if (MetadataKey.class.isAssignableFrom(propertyId.getClass())) {
	    return ((MetadataKey) propertyId).getKeyValue();
	}
	return super.makeCaption(item, propertyId, uiContext);
    }

}
