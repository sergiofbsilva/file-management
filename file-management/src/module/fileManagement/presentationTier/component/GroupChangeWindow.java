package module.fileManagement.presentationTier.component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.groups.UnitGroup;
import module.organizationIst.domain.IstAccountabilityType;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import myorg.util.BundleUtil;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class GroupChangeWindow extends Window {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    private Party selectedParty = null;
    private Set<AccountabilityType> selectedAccountabilityTypes = new HashSet<AccountabilityType>();
    private boolean includeSubUnits = false;
    private final ChangeGroupProcedure changeGroupProcedure;

    public GroupChangeWindow(final String labelKey, final ChangeGroupProcedure changeGroupProcedure) {
	super(getMessage(labelKey));
	setModal(true);
	setWidth(500, UNITS_PIXELS);

	final PersistentGroup group = changeGroupProcedure.getGroup();
        if (group instanceof SingleUserGroup) {
            final SingleUserGroup singleUserGroup = (SingleUserGroup) group;
            selectedParty = singleUserGroup.getUser().getPerson();
        } else if (group instanceof UnitGroup) {
            final UnitGroup unitGroup = (UnitGroup) group;
            selectedParty = unitGroup.getUnit();
        }

        this.changeGroupProcedure = changeGroupProcedure;
    }

    @Override
    public void attach() {
        super.attach();

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        final ComboBox comboBox = new PartyComboBox();
        layout.addComponent(comboBox);

        final OptionGroup yesNoOptionGroup = new YesNoOptionGroup("label.include.child.unit.members", "label.yes", "label.no");
        layout.addComponent(yesNoOptionGroup);

        final OptionGroup accountabilityTypeOptions = new MemberAccountabilityTypeOptionGroup();
        layout.addComponent(accountabilityTypeOptions);

        if (selectedParty != null) {
            comboBox.select(selectedParty.getExternalId());
            if (selectedParty.isPerson()) {
        	accountabilityTypeOptions.setVisible(false);
        	yesNoOptionGroup.setVisible(false);
            }
        }

        comboBox.addListener(new ValueChangeListener() {
	    @Override
	    public void valueChange(final ValueChangeEvent event) {
	        if (event.getProperty().getValue() != null) {
	            final String partyOid = (String) event.getProperty().getValue();
	            comboBox.select(partyOid);
	            selectedParty = AbstractDomainObject.fromExternalId(partyOid);

	            if (selectedParty.isPerson()) {
	        	accountabilityTypeOptions.setVisible(false);
	        	yesNoOptionGroup.setVisible(false);
	            } else if (selectedParty.isUnit()) {
	        	accountabilityTypeOptions.setVisible(true);
	        	yesNoOptionGroup.setVisible(true);
	            } else {
	        	throw new Error("unknown.party.type");
	            }
	        }
	    }
	});
        accountabilityTypeOptions.addListener(new ValueChangeListener() {
	    
	    @Override
	    public void valueChange(final ValueChangeEvent event) {
	        if (event.getProperty().getValue() != null) {
	            selectedAccountabilityTypes.clear();
	            for (Object itemId : (Collection) event.getProperty().getValue()) {
	        	final String accountabilityTypeOid = (String) itemId;
	        	final AccountabilityType accountabilityType = AbstractDomainObject.fromExternalId(accountabilityTypeOid);
	        	selectedAccountabilityTypes.add(accountabilityType);
	            }
	        }
	    }
	});
        yesNoOptionGroup.addListener(new ValueChangeListener() {
	    @Override
	    public void valueChange(ValueChangeEvent event) {
		includeSubUnits = !includeSubUnits;
	    }
	});

        final Window window = this;

        final HorizontalLayout buttonLayout = new HorizontalLayout();
        final Button save = new Button(getMessage("label.save"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
        	final PersistentGroup group;
        	if (selectedParty.isPerson()) {
        	    final Person person = (Person) selectedParty;
        	    group = SingleUserGroup.getOrCreateGroup(person.getUser());
        	} else if (selectedParty.isUnit()) {
        	    final Unit unit = (Unit) selectedParty;
        	    final AccountabilityType[] memberTypes = selectedAccountabilityTypes.toArray(new AccountabilityType[0]);
        	    final AccountabilityType[] childTypes = includeSubUnits ?
        		    new AccountabilityType[] { IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType() } : null;
        	    group = UnitGroup.getOrCreateGroup(unit, memberTypes, childTypes);
        	} else {
        	    throw new Error("unknown.party.type");
        	}
        	changeGroupProcedure.execute(group);

        	getParent().removeWindow(window);
            }
        });
        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
        	getParent().removeWindow(window);
            }
        });
        buttonLayout.addComponent(save);
        buttonLayout.addComponent(close);
        layout.addComponent(buttonLayout);
        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
    }

    private void addAccountabilityType(final OptionGroup optionGroup, final IstAccountabilityType type) {
	final AccountabilityType accountabilityType = type.readAccountabilityType();
	final String externalId = accountabilityType.getExternalId();
	final String name = accountabilityType.getName().getContent();

	final Item comboItem = optionGroup.addItem(externalId);
	comboItem.getItemProperty("name").setValue(name);
    }

}
