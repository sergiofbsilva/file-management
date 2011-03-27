package module.fileManagement.domain;

import java.text.Collator;

import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.AnyoneGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import pt.ist.fenixWebFramework.services.Service;

public abstract class AbstractFileNode extends AbstractFileNode_Base implements Comparable<AbstractFileNode> {
    
    public AbstractFileNode() {
        super();
    }

    public boolean isFile() {
	return false;
    }

    public boolean isDir() {
	return false;
    }

    public void delete() {
	removeParent();
	deleteDomainObject();
    }

    @Service
    public void deleteService() {
	delete();
    }

    public abstract PersistentGroup getReadGroup();

    public abstract PersistentGroup getWriteGroup();

    protected abstract void setReadGroup(final PersistentGroup persistentGroup);

    protected abstract void setWriteGroup(final PersistentGroup persistentGroup);

    public boolean isReadGroupMember() {
	return isGroupMember(getReadGroup());
    }

    public boolean isWriteGroupMember() {
	return isGroupMember(getWriteGroup());
    }

    private boolean isGroupMember(final PersistentGroup group) {
	final User user = UserView.getCurrentUser();
	return group != null && group.isMember(user);
    }

    public boolean isAccessible() {
	return isReadGroupMember() || isWriteGroupMember();
    }

    public abstract String getDisplayName();

    public String getVisibility() {
	final PersistentGroup readGroup = getReadGroup();
	final String key = readGroup instanceof SingleUserGroup ?
		"label.visibility.private" : readGroup instanceof AnyoneGroup ?
			"label.visibility.public" : "label.visibility.shared";
	return DocumentSystem.getMessage(key);
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	final String displayName1 = getDisplayName();
	final String displayName2 = node.getDisplayName();
	return Collator.getInstance().compare(displayName1, displayName2);
    }

    public VisibilityList getVisibilityGroups() {
	final PersistentGroup writeGroup = getWriteGroup();
	final PersistentGroup readGroup = getReadGroup();
	return new VisibilityList(writeGroup, readGroup);
    }

    @Service
    public void setVisibility(final VisibilityList visibilityList) {
	final PersistentGroup writeGroup = getWriteGroup();
	final PersistentGroup newWriteGroup = visibilityList.getWriteGroup();
	if (newWriteGroup == null) {
	    throw new DomainException("error.file.must.have.at.least.one.write");
	}
	if (writeGroup != newWriteGroup) {
	    setWriteGroup(newWriteGroup);
	}
	final PersistentGroup readGroup = getReadGroup();
	final PersistentGroup newReadGroup = visibilityList.getReadGroup();
	if (readGroup != newReadGroup) {
	    setReadGroup(newReadGroup);
	}
    }

    public abstract String getPresentationFilesize();

    protected abstract void setDisplayName(final String displayName);

    @Service
    public void updateDisplayName(final String displayName) {
	setDisplayName(displayName);
    }

}
