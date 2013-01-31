package module.fileManagement.presentationTier.component.fields;

import org.apache.commons.lang.StringUtils;
import org.vaadin.customfield.PropertyConverter;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;

public class IntegerField extends TextField {
	class IntegerConverter extends PropertyConverter<Integer, String> {

		protected IntegerConverter(Class<? extends Integer> propertyClass) {
			super(propertyClass);
		}

		public IntegerConverter(Property propertyDataSource) {
			super(propertyDataSource);
		}

		@Override
		public String format(Integer propertyValue) {
			return propertyValue == null ? "0" : propertyValue.toString();
		}

		@Override
		public Integer parse(String fieldValue) throws ConversionException {
			return Integer.parseInt(fieldValue);
		}

	}

	public IntegerField() {
		addValidator(new Validator() {

			@Override
			public void validate(Object value) throws InvalidValueException {
				try {
					Integer.parseInt((String) value);
				} catch (NumberFormatException nfe) {
					throw new InvalidValueException("O valor deve ser um número.");
				}

			}

			@Override
			public boolean isValid(Object value) {
				try {
					validate(value);
					return true;
				} catch (InvalidValueException ive) {
					return false;
				}
			}

		});
		setNullRepresentation(StringUtils.EMPTY);
	}

	@Override
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(new IntegerConverter(newDataSource));
	}

}