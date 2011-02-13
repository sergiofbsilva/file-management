package module.fileManagement.presentationTier.component;

import module.organization.domain.AccountabilityType;
import module.organizationIst.domain.IstAccountabilityType;
import myorg.util.BundleUtil;

import com.vaadin.data.Item;
import com.vaadin.ui.OptionGroup;

public class MemberAccountabilityTypeOptionGroup extends OptionGroup {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    public MemberAccountabilityTypeOptionGroup() {
	super(getMessage("label.unit.member.accountability.types"));
        addContainerProperty("name", String.class, null);
        setItemCaptionPropertyId("name");
        setMultiSelect(true);
        setNullSelectionAllowed(false); // user can not 'unselect'
        setImmediate(true); // send the change to the server at once
        
        addAccountabilityType(IstAccountabilityType.PERSONNEL);
        addAccountabilityType(IstAccountabilityType.TEACHING_PERSONNEL);
        addAccountabilityType(IstAccountabilityType.RESEARCH_PERSONNEL);
        addAccountabilityType(IstAccountabilityType.GRANT_OWNER_PERSONNEL);
    }

    private void addAccountabilityType(final IstAccountabilityType type) {
	final AccountabilityType accountabilityType = type.readAccountabilityType();
	if (accountabilityType != null) {
	    final String externalId = accountabilityType.getExternalId();
	    final String name = accountabilityType.getName().getContent();

	    final Item comboItem = addItem(externalId);
	    comboItem.getItemProperty("name").setValue(name);
	}
    }

}
