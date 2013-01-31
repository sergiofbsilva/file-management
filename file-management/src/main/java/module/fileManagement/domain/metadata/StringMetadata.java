package module.fileManagement.domain.metadata;

public class StringMetadata extends StringMetadata_Base {

	public StringMetadata() {
		super();
	}

	@Override
	public String getValue() {
		return getStringValue();
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		setStringValue((String) value);
	}

	@Override
	public String getPresentationValue() {
		return getValue();
	}
}
