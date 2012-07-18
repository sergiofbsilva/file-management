package module.fileManagement.presentationTier.component.fields;

import java.util.Collection;

import module.fileManagement.tools.StringUtils;
import module.organization.domain.Person;
import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.BuildingState;
import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.DSLState;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.ui.TimeoutSelect;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

public class PersonField extends TimeoutSelect {

    private final PersonContainer container;

    private static class PersonContainer extends DomainContainer<Person> {
	public PersonContainer() {
	    super(Person.class);
	}

	public PersonContainer(Collection<Person> elements, Class<? extends Person> elementType,
		pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
	    super(elements, elementType, hints);
	    // TODO Auto-generated constructor stub
	}

	public PersonContainer(Property wrapped, Class<? extends Person> elementType,
		pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
	    super(wrapped, elementType, hints);
	    // TODO Auto-generated constructor stub
	}

	public PersonContainer(Class<? extends Person> elementType, pt.ist.vaadinframework.data.HintedProperty.Hint[] hints) {
	    super(elementType, hints);
	}

	@Override
	protected DSLState createFilterExpression(String filterText) {
	    if (!StringUtils.isEmpty(filterText)) {
		final String[] split = filterText.trim().split("\\s+");
		BuildingState expr = new BuildingState();
		for (int i = 0; i < split.length; i++) {
		    if (i == split.length - 1) {
			if (split[i].startsWith("ist")) {
			    return expr.matches(Person.IndexableFields.PERSON_USERNAME, split[i]);
			}
			return expr.matches(Person.IndexableFields.PERSON_NAME, split[i]);
		    }
		    if (split[i].startsWith("ist")) {
			expr = expr.matches(Person.IndexableFields.PERSON_USERNAME, split[i]).and();
		    }
		    expr = expr.matches(Person.IndexableFields.PERSON_NAME, split[i]).and();
		}
	    }
	    return new BuildingState();
	}
    }

    public PersonField() {
	container = new PersonContainer();
	setImmediate(true);
	setContainerDataSource(container);
	setItemCaptionPropertyId("presentationName");
	addListener(new TextChangeListener() {

	    @Override
	    public void textChange(TextChangeEvent event) {
		container.search(event.getText());
	    }
	});
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
	super.setPropertyDataSource(newDataSource);
	if (newDataSource != null && newDataSource.getValue() != null) {
	    Person value = (Person) newDataSource.getValue();
	    setValue(value);
	    // setText(value.getUser().getUsername());
	}
    }
}
