package module.fileManagement.domain;

import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

public class AbstractFileNode extends AbstractFileNode_Base {
    
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

    @Override
    public PersistentGroup getReadGroup() {
	final PersistentGroup group = super.getReadGroup();
        return group == null && hasParent() ? getParent().getReadGroup() : group;
    }

    @Override
    public PersistentGroup getWriteGroup() {
	final PersistentGroup group = super.getWriteGroup();
	return group == null && hasParent() ? getParent().getWriteGroup() : group;
    }

    @Service
    public void changeReadGroup(final PersistentGroup group) {
        setReadGroup(group);
    }

    @Service
    public void changeWriteGroup(final PersistentGroup group) {
        setWriteGroup(group);
    }

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
}
