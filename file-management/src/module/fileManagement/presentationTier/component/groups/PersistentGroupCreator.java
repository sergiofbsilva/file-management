package module.fileManagement.presentationTier.component.groups;

import java.util.Collection;
import java.util.HashSet;

import myorg.domain.MyOrg;
import myorg.domain.Presentable;
import myorg.domain.groups.PersistentGroup;

public class PersistentGroupCreator implements HasPersistentGroupCreator {

    @Override
    public HasPersistentGroup createGroupFor(final Object itemId) {
	if (itemId != null && itemId instanceof PersistentGroup) {
	    final PersistentGroup persistentGroup = (PersistentGroup) itemId;
	    return new PersistentGroupHolder(persistentGroup);
	}
	return null;
    }

    @Override
    public Collection<Presentable> getElements(String filterText) {
	Collection<Presentable> presentables = new HashSet<Presentable>();

	for (final PersistentGroup persistentGroup : MyOrg.getInstance().getSystemGroupsSet()) {
	    if (filterText == null || persistentGroup.getPresentationName().contains(filterText)) {
		presentables.add(persistentGroup);
	    }
	}

	return presentables;
    }

}