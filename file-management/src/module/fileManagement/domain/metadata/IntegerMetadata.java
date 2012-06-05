package module.fileManagement.domain.metadata;

public class IntegerMetadata extends IntegerMetadata_Base {

    public IntegerMetadata() {
	super();
    }

    @Override
    public Integer getRealValue() {
	return getIntegerValue();
    }

    @Override
    public void setRealValue(Object value) {
	setIntegerValue((Integer) value);
    }

    @Override
    public String getPresentationValue() {
	return getRealValue().toString();
    }
}
