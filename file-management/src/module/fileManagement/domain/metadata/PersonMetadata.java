package module.fileManagement.domain.metadata;

import module.organization.domain.Person;

public class PersonMetadata extends PersonMetadata_Base {

    public PersonMetadata() {
	super();
    }

    @Override
    public Person getValue() {
	return getPerson();
    }

    @Override
    public void setValue(Object value) {
	super.setValue(value);
	setPerson((Person) value);
    }

    @Override
    public String getPresentationValue() {
	return getValue().getPresentationName();
    }

}
