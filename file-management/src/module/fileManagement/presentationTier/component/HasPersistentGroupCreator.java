package module.fileManagement.presentationTier.component;

import com.vaadin.ui.ComboBox;

public interface HasPersistentGroupCreator {

    public HasPersistentGroup createGroupFor(final Object itemId);

    public void addItems(final ComboBox comboBox, final String displayItemProperty);

}
