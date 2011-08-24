package module.fileManagement.domain;

import java.text.Collator;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Party;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.AnyoneGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import myorg.domain.groups.UnionGroup;
import pt.ist.fenixWebFramework.services.Service;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

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

    public boolean isShared() {
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

    @Service
    public void trash() {
	setParent(UserView.getCurrentUser().getTrash());
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
	final String key = readGroup instanceof SingleUserGroup ? "label.visibility.private"
		: readGroup instanceof AnyoneGroup ? "label.visibility.public" : "label.visibility.shared";
	return FileManagementSystem.getMessage(key);
    }

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

    public abstract int getFilesize();

    protected abstract void setDisplayName(final String displayName);

    @Service
    public void updateDisplayName(final String displayName) {
	setDisplayName(displayName);
    }

    @Service
    public void share(User user) {
	if (isShared()) {
	    return;
	}
	final DirNode userRootDir = FileRepository.getOrCreateFileRepository(user);
	final AbstractFileNode sharedNode = makeSharedNode();
	userRootDir.getSharedFolder().addChild(sharedNode);
	Set<PersistentGroup> readGroupSet = new HashSet<PersistentGroup>();
	final PersistentGroup readGroup = getReadGroup();
	if (readGroup instanceof UnionGroup) {
	    for(PersistentGroup group : ((UnionGroup)readGroup).getPersistentGroups()) {
		readGroupSet.add(group);
	    }
	}
	readGroupSet.add(user.getSingleUserGroup());
	setReadGroup(new UnionGroup(readGroupSet));
    }

    private AbstractFileNode makeSharedNode() {
	if (isDir()) {
	    return new SharedDirNode((DirNode) this);
	}
	if (isFile()) {
	    return new SharedFileNode((FileNode) this);
	}
	throw new UnsupportedOperationException("File can't be shared");
    }

    public Party getOwner() {
	return hasParent() ? getParent().getOwner() : null;
    }

    public String getOwnerName() {
	Party owner = getOwner();
	return owner != null ? owner.getPartyName().getContent() : "-";
    }
    
    private ThemeResource getThemeResource(AbstractFileNode abstractFileNode) {
	final String iconFile = abstractFileNode.isDir() ? "folder1_16x16.gif" : getIconFile(((FileNode) abstractFileNode)
		.getDocument().getLastVersionedFile().getFilename());
	return new ThemeResource("../../../images/fileManagement/" + iconFile);
    }

    private String getIconFile(final String filename) {
	final int lastDot = filename.lastIndexOf('.');
	final String fileSuffix = lastDot < 0 ? "file" : filename.substring(lastDot + 1);
	return "fileicons/" + fileSuffix + ".png";
    }
    
    public Resource getIcon() {
	return getThemeResource(this);
    }
}
