package module.fileManagement.domain.metadata;

import org.joda.time.DateTime;

public class DateTimeMetadata extends DateTimeMetadata_Base {

    public DateTimeMetadata() {
		super();
    }

    @Override
    public DateTime getValue() {
		return getDatetimeValue();
    }

    @Override
    public void setValue(Object value) {
		super.setValue(value);
		setDatetimeValue((DateTime) value);
	}

    @Override
    public String getPresentationValue() {
		return getValue().toString("dd-MM-yyyy");
    }

}
