package module.fileManagement.presentationTier.component;

import myorg.domain.groups.PersistentGroup;

import com.vaadin.ui.AbstractOrderedLayout;

public interface HasPersistentGroup {

    public PersistentGroup getPersistentGroup();

    public void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout);

}
