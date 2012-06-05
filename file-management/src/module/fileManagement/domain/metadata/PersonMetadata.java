package module.fileManagement.domain.metadata;

import module.organization.domain.Person;

public class PersonMetadata extends PersonMetadata_Base {

    public PersonMetadata() {
	super();
    }

    @Override
    public Person getRealValue() {
	return getPerson();
    }

    @Override
    public void setRealValue(Object value) {
	setPerson((Person) value);
    }

    @Override
    public String getPresentationValue() {
	return getRealValue().getPresentationName();
    }

}
