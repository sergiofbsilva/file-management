package module.fileManagement.domain.metadata;

import org.joda.time.DateTime;

public class DateTimeMetadata extends DateTimeMetadata_Base {

    public DateTimeMetadata() {
	super();
    }

    @Override
    public DateTime getRealValue() {
	return getDatetimeValue();
    }

    @Override
    public void setRealValue(Object value) {
	setDatetimeValue((DateTime) value);
    }

    @Override
    public String getPresentationValue() {
	return getRealValue().toString("dd-MM-yyyy");
    }

}
