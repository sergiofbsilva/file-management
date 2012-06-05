package module.fileManagement.presentationTier.data;

import java.util.Map;

import module.fileManagement.domain.metadata.DateTimeMetadata;
import module.fileManagement.domain.metadata.IntegerMetadata;
import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.MetadataTemplateRule;
import module.fileManagement.domain.metadata.PersonMetadata;
import module.fileManagement.domain.metadata.StringMetadata;
import module.fileManagement.presentationTier.component.fields.IntegerField;
import module.fileManagement.presentationTier.component.fields.PersonField;

import org.apache.commons.lang.StringUtils;

import pt.ist.vaadinframework.ui.DefaultFieldFactory;
import pt.ist.vaadinframework.ui.fields.PopupDateTimeField;

import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FMSFieldFactory extends DefaultFieldFactory {

    private interface FieldMaker {
	Field makeField(MetadataKey key);
    }

    private static final Map<Class<? extends Metadata>, FieldMaker> metadataClassMakerMap = Maps.newHashMap();

    public static FieldMaker createStringFieldMaker() {
	return new FieldMaker() {

	    @Override
	    public Field makeField(MetadataKey key) {
		if ("Observações".equals(key.getKeyValue())) {
		    return new TextArea();
		}
		return new TextField();
	    }
	};
    }

    public static FieldMaker createIntegerFieldMaker() {
	return new FieldMaker() {

	    @Override
	    public Field makeField(MetadataKey key) {
		return new IntegerField();
	    }
	};
    }

    public static FieldMaker createDateFieldMaker() {
	return new FieldMaker() {

	    @Override
	    public Field makeField(MetadataKey key) {
		final DateField dateField = new PopupDateTimeField();
		dateField.setDateFormat("dd-MM-yyyy");
		return dateField;
	    }
	};
    }

    private static FieldMaker createPersonMetadataFieldMaker() {
	return new FieldMaker() {

	    @Override
	    public Field makeField(MetadataKey key) {
		return new PersonField();
	    }
	};
    }

    static {
	metadataClassMakerMap.put(StringMetadata.class, createStringFieldMaker());
	metadataClassMakerMap.put(IntegerMetadata.class, createIntegerFieldMaker());
	metadataClassMakerMap.put(DateTimeMetadata.class, createDateFieldMaker());
	metadataClassMakerMap.put(PersonMetadata.class, createPersonMetadataFieldMaker());
    }

    private MetadataTemplate template;

    public FMSFieldFactory(String bundle, MetadataTemplate template) {
	super(bundle);
	setTemplate(template);
    }

    private void setTemplate(MetadataTemplate template) {
	this.template = template;
    }

    @Override
    protected String makeCaption(Item item, Object propertyId, Component uiContext) {
	if (MetadataKey.class.isAssignableFrom(propertyId.getClass())) {
	    return ((MetadataKey) propertyId).getKeyValue();
	}
	return super.makeCaption(item, propertyId, uiContext);
    }

    @Override
    protected Field makeField(Item item, Object propertyId, Component uiContext) {
	final Field field = makeField(propertyId);
	if (field instanceof AbstractTextField) {
	    ((AbstractTextField) field).setNullRepresentation(StringUtils.EMPTY);
	}
	final MetadataKey key = (MetadataKey) propertyId;
	final MetadataTemplateRule rule = template.getRule(key);
	if (rule != null && rule.getRequired()) {
	    field.setRequired(true);
	}
	return field;
    }

    private FieldMaker get(Class<? extends Metadata> type) {
	if (Metadata.class.equals(type)) {
	    return null;
	}
	final FieldMaker fieldMaker = metadataClassMakerMap.get(type);
	if (fieldMaker == null) {
	    return get((Class<? extends Metadata>) type.getSuperclass());
	}
	return fieldMaker;
    }

    private Field makeField(Object propertyId) {
	final MetadataKey key = (MetadataKey) propertyId;
	final Class<? extends Metadata> metadataValueType = key.getMetadataValueType();
	final FieldMaker fieldMaker = get(metadataValueType);
	if (fieldMaker != null) {
	    return fieldMaker.makeField(key);
	}
	return new TextField();
    }
}
