package module.fileManagement.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.VisibilityGroup.VisibilityOperation;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import myorg.domain.groups.UnionGroup;

public class VisibilityList extends ArrayList<VisibilityGroup> {

    public VisibilityList(final PersistentGroup writeGroup, final PersistentGroup readGroup) {
	if (writeGroup != null) {
	    if (writeGroup instanceof UnionGroup) {
		final UnionGroup unionGroup = (UnionGroup) writeGroup;
		for (final PersistentGroup persistentGroup : unionGroup.getPersistentGroupsSet()) {
		    add(new VisibilityGroup(persistentGroup, VisibilityOperation.WRITE));
		}
	    } else {
		add(new VisibilityGroup(writeGroup, VisibilityOperation.WRITE));
	    }
	}

	if (readGroup != null) {
	    if (readGroup instanceof UnionGroup) {
		final UnionGroup unionGroup = (UnionGroup) readGroup;
		for (final PersistentGroup persistentGroup : unionGroup.getPersistentGroupsSet()) {
		    if (!contains(persistentGroup)) {
			add(new VisibilityGroup(persistentGroup, VisibilityOperation.READ));
		    }
		}
	    } else {
		if (!contains(readGroup)) {
		    add(new VisibilityGroup(readGroup, VisibilityOperation.READ));
		}
	    }
	}
    }

    public boolean contains(final PersistentGroup persistentGroup) {
	for (final VisibilityGroup visibilityGroup : this) {
	    if (visibilityGroup.persistentGroup == persistentGroup) {
		return true;
	    }
	}
	return false;
    }

    public PersistentGroup getWriteGroup() {
	return getGroup(VisibilityOperation.WRITE);
    }

    public PersistentGroup getReadGroup() {
	return getGroup(VisibilityOperation.READ, VisibilityOperation.WRITE);
    }

    public PersistentGroup getGroup(final VisibilityOperation... visibilityOperations) {
	final Collection<PersistentGroup> result = new HashSet<PersistentGroup>();
	for (final VisibilityGroup visibilityGroup : this) {
	    if (visibilityGroup.visibilityOperation.isMember(visibilityOperations)) {
		final PersistentGroup persistentGroup = visibilityGroup.persistentGroup;
		result.add(persistentGroup);
	    }
	}
	if (result.size() == 1) {
	    final PersistentGroup persistentGroup = result.iterator().next();
	    if (persistentGroup instanceof SingleUserGroup) {
		return persistentGroup;
	    }
	}
	return result.size() == 0 ? null : new UnionGroup(result);
    }

    public boolean contains(Class clazz) {
	for (VisibilityGroup group : this) {
	    if (group.persistentGroup.getClass().isAssignableFrom(clazz)) {
		return true;
	    }
	}
	return false;
    }

}
