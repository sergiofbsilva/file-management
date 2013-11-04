package module.fileManagement.presentationTier.component.fields;

import pt.ist.bennu.search.Search;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.ui.TimeoutSelect;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import module.organization.domain.Person;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

@SuppressWarnings("serial")
public class PersonField extends TimeoutSelect {

    private final PersonContainer container;

    public static class PersonContainer extends DomainContainer<Person> {

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
        protected List<Search> getSearch(String filterText) {
            if (!StringUtils.isEmpty(filterText)) {
                List<Search> searches = new ArrayList<>();
                filterText = StringNormalizer.normalize(filterText);
                for (String part : filterText.trim().split("\\s+")) {
                    if (part.startsWith("ist")) {
                        searches.add(new Search().must(Person.IndexableFields.PERSON_USERNAME, part));
                    }
                    searches.add(new Search().must(Person.IndexableFields.PERSON_NAME, part));
                }
                return searches;
            }
            return super.getSearch(filterText);
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
            setText(value.getUser().getUsername());
        }
    }
}
