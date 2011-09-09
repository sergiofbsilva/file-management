package module.fileManagement.presentationTier.component.groups;

import myorg.domain.groups.PersistentGroup;

import com.vaadin.ui.AbstractOrderedLayout;

public class PersistentGroupHolder extends HasPersistentGroup {

    private final PersistentGroup persistentGroup;

    public PersistentGroupHolder(final PersistentGroup persistentGroup) {
	this.persistentGroup = persistentGroup;
    }

    @Override
    public PersistentGroup getPersistentGroup() {
	return persistentGroup;
    }

    @Override
    public void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout) {
	// nothing to do, we already have a group :o)
    }

    

}
