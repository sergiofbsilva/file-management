package module.fileManagement.domain.metadata;

public class StringMetadata extends StringMetadata_Base {

    public StringMetadata() {
	super();
    }

    @Override
    public String getRealValue() {
	return getStringValue();
    }

    @Override
    public void setRealValue(Object value) {
	setStringValue((String) value);
    }

    @Override
    public String getPresentationValue() {
	return getRealValue();
    }
}
