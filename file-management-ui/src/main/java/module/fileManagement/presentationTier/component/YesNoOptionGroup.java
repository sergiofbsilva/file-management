package module.fileManagement.presentationTier.component;

import pt.ist.bennu.core.i18n.BundleUtil;

import com.vaadin.data.Item;
import com.vaadin.ui.OptionGroup;

public class YesNoOptionGroup extends OptionGroup {

    protected static String getBundle() {
        return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
        return BundleUtil.getString(getBundle(), key, args);
    }

    public YesNoOptionGroup(final String labelKey, final String yesKey, final String noKey) {
        super(getMessage(labelKey));
        addContainerProperty("name", String.class, null);
        setItemCaptionPropertyId("name");
        setMultiSelect(false);
        setNullSelectionAllowed(false); // user can not 'unselect'
        setImmediate(true); // send the change to the server at once

        addItem(Boolean.TRUE, yesKey);
        addItem(Boolean.FALSE, noKey);

        select(Boolean.FALSE.toString());
    }

    private void addItem(final Boolean b, final String key) {
        final Item comboItem = addItem(b.toString());
        comboItem.getItemProperty("name").setValue(getMessage(key));
    }

}
