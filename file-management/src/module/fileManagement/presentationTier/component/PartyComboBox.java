package module.fileManagement.presentationTier.component;

import module.organization.domain.Party;
import module.organization.domain.Person;
import myorg.domain.MyOrg;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;

public class PartyComboBox extends ComboBox {

    public PartyComboBox() {
        setWidth(400, UNITS_PIXELS);
        setNullSelectionAllowed(false);
        addContainerProperty("name", String.class, null);
        setItemCaptionPropertyId("name");
        setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        setImmediate(true);
        setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);

        for (final Party party : MyOrg.getInstance().getPartiesSet()) {
            if (party.isUnit() || (party.isPerson() && ((Person) party).hasUser())) {
        	final String externalId = party.getExternalId();
        	final String name = party.getPresentationName();

        	final Item comboItem = addItem(externalId);
        	comboItem.getItemProperty("name").setValue(name);
            }
        }
    }

}
