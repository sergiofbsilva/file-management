package module.fileManagement.domain.metadata;

public class IntegerMetadata extends IntegerMetadata_Base {

    public IntegerMetadata() {
        super();
    }

    @Override
    public Integer getValue() {
        return getIntegerValue();
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        setIntegerValue((Integer) value);
    }

    @Override
    public String getPresentationValue() {
        return getValue().toString();
    }
}
