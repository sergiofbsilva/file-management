package module.fileManagement.domain;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.NaturalOrderComparator;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

import dml.runtime.DirectRelation;
import dml.runtime.Relation;
import dml.runtime.RelationListener;

import module.fileManagement.domain.log.DeleteDirLog;
import module.fileManagement.domain.log.DeleteFileLog;
import module.fileManagement.domain.log.ShareDirLog;
import module.fileManagement.domain.log.ShareFileLog;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.domain.groups.AnyoneGroup;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.bennu.core.domain.groups.SingleUserGroup;

public abstract class AbstractFileNode extends AbstractFileNode_Base implements Comparable<AbstractFileNode> {
    final static NaturalOrderComparator STRING_NATURAL_COMPARATOR;

    static {
	DirectRelation<AbstractFileNode, DirNode> child = DirNodeAbstractFileNode;
	child.addListener(new RelationListener<AbstractFileNode, DirNode>() {

	    @Override
	    public void afterAdd(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
		if (arg1 != null && arg2 != null && !arg1.isShared()) {
		    arg2.addUsedSpace(arg1.getFilesize());
		}
	    }

	    @Override
	    public void afterRemove(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
		if (arg1 != null && arg2 != null && !arg1.isShared()) {
		    arg2.removeUsedSpace(arg1.getFilesize());
		}
	    }

	    @Override
	    public void beforeAdd(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {

	    }

	    @Override
	    public void beforeRemove(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
		// TODO Auto-generated method stub

	    }
	});
	STRING_NATURAL_COMPARATOR = new NaturalOrderComparator();
    }

    public enum VisibilityState {
	PRIVATE, SHARED, PUBLIC;
    }

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

    public abstract int getCountFiles();

    private void createDirLogs(User user, ContextPath contextPath, DirNode dirNode) {
	for (AbstractFileNode node : dirNode.getChild()) {
	    if (node.isDir()) {
		createDirLogs(user, contextPath.concat((DirNode) node), (DirNode) node);
	    }
	    if (node.isFile()) {
		new DeleteFileLog(user, contextPath, (FileNode) node);
	    }
	}
    }

    @Service
    public void trash(ContextPath contextPath) {
	if (this instanceof FileNode) {
	    Collection<SharedFileNode> shared = new ArrayList<SharedFileNode>(((FileNode) this).getSharedFileNodes());
	    for (SharedFileNode node : shared) {
		node.deleteLink(contextPath);
	    }
	}

	if (this instanceof DirNode) {
	    Collection<SharedDirNode> shared = new ArrayList<SharedDirNode>(((DirNode) this).getSharedDirNodes());
	    for (SharedDirNode node : shared) {
		node.deleteLink(contextPath);
	    }
	}
	final User currentUser = UserView.getCurrentUser();
	// final SingleUserGroup currentUserGroup =
	// currentUser.getSingleUserGroup();
	// setReadGroup(currentUserGroup);
	// setWriteGroup(currentUserGroup);

	if (isFile()) {
	    setParent(getParent().getTrash());
	    new DeleteFileLog(currentUser, contextPath, (FileNode) this);
	} else if (isDir()) {
	    final DirNode dirNode = (DirNode) this;
	    setParent(dirNode.getTrash());
	    createDirLogs(currentUser, contextPath.concat(dirNode), dirNode);
	    new DeleteDirLog(currentUser, contextPath, dirNode);
	}
    }

    public abstract PersistentGroup getReadGroup();

    public abstract PersistentGroup getWriteGroup();

    protected abstract void setReadGroup(final PersistentGroup persistentGroup);

    protected abstract void setWriteGroup(final PersistentGroup persistentGroup);

    public abstract boolean search(final String searchText);

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
	return (isReadGroupMember() || isWriteGroupMember()) && !isInTrash();
    }

    public boolean isInTrash() {
	return hasParent() ? getParent().isInTrash() : false;
    }

    public abstract String getDisplayName();

    public VisibilityState getVisibilityState() {
	final PersistentGroup readGroup = getReadGroup();
	return readGroup instanceof SingleUserGroup ? VisibilityState.PRIVATE
		: (readGroup instanceof AnyoneGroup ? VisibilityState.PUBLIC : VisibilityState.SHARED);
    }

    public String getVisibility() {
	return FileManagementSystem.getMessage("label.visibility." + getVisibilityState().toString().toLowerCase());
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	if ((isDir() && node.isDir()) || (isFile() && node.isFile())) {
	    final String displayName1 = getDisplayName();
	    final String displayName2 = node.getDisplayName();
	    return STRING_NATURAL_COMPARATOR.compare(displayName1, displayName2);
	}

	if (isDir() && node.isFile()) {
	    return -1;
	}

	if (isFile() && node.isDir()) {
	    return 1;
	}
	return -1;
    }

    public VisibilityList getVisibilityGroups() {
	final PersistentGroup writeGroup = getWriteGroup();
	final PersistentGroup readGroup = getReadGroup();
	return new VisibilityList(writeGroup, readGroup);
    }

    public void removeVisibilityGroup(VisibilityGroup group) {
	final VisibilityList visibilityGroups = getVisibilityGroups();
	visibilityGroups.remove(group);
	setVisibility(visibilityGroups);
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

    public abstract long getFilesize();

    public void addVisibilityGroup(VisibilityGroup group) {
	final VisibilityList visibilityList = getVisibilityGroups();
	visibilityList.add(group);
	setVisibility(visibilityList);
    }

    private boolean hasSharedNode(DirNode targetFolder, AbstractFileNode node) {
	if (targetFolder.hasSharedNode(node)) {
	    return true;
	}
	return node.hasParent() ? hasSharedNode(targetFolder, node.getParent()) : false;
    }

    @Service
    public void share(User user, VisibilityGroup group, ContextPath contextPath) {
	if (isShared()) {
	    return;
	}
	final DirNode userRootDir = FileRepository.getOrCreateFileRepository(user);

	final DirNode sharedFolder = userRootDir.getSharedFolder();
	if (!hasSharedNode(sharedFolder, this)) { // TODO: deal with same name
						  // when shared later
	    final AbstractFileNode sharedNode = makeSharedNode(UserView.getCurrentUser(), contextPath, group, sharedFolder);
	    sharedFolder.addChild(sharedNode);
	} else {
	    FileManagementSystem.getLogger().warn(
		    String.format("No need to create link for %s. already present", getDisplayName()));
	}

	addVisibilityGroup(group);
	// final User currentUser = UserView.getCurrentUser();
	// Set<PersistentGroup> readGroupSet = new HashSet<PersistentGroup>();
	// final PersistentGroup readGroup = getReadGroup();
	// if (readGroup instanceof UnionGroup) {
	// for(PersistentGroup group :
	// ((UnionGroup)readGroup).getPersistentGroups()) {
	// readGroupSet.add(group);
	// }
	// }
	// readGroupSet.add(user.getSingleUserGroup());
	// setReadGroup(new UnionGroup(readGroupSet));
    }

    private AbstractFileNode makeSharedNode(User user, ContextPath contextPath, VisibilityGroup group, DirNode targetSharedFolder) {

	if (isDir()) {
	    final SharedDirNode sharedDirNode = new SharedDirNode((DirNode) this);
	    // final ShareDirLog log = new ShareDirLog(user, contextPath,
	    // sharedDirNode);
	    final ShareDirLog log = new ShareDirLog(user, contextPath, sharedDirNode, targetSharedFolder);
	    log.setVisibilityGroup(group.getDescription());
	    return sharedDirNode;
	}
	if (isFile()) {
	    final SharedFileNode sharedFileNode = new SharedFileNode((FileNode) this);
	    // final ShareFileLog log = new ShareFileLog(user, contextPath,
	    // sharedFileNode);
	    final ShareFileLog log = new ShareFileLog(user, contextPath, sharedFileNode, targetSharedFolder);
	    log.setVisibilityGroup(group.getDescription());
	    return sharedFileNode;
	}
	throw new UnsupportedOperationException("File can't be shared");
    }

    public User getOwner() {
	return hasParent() ? getParent().getOwner() : null;
    }

    private ThemeResource getThemeResource(AbstractFileNode abstractFileNode) {
	final String iconFile = abstractFileNode.isDir() ? "folder1_16x16.gif" : getIconFile(((FileNode) abstractFileNode)
		.getDocument().getLastVersionedFile().getFilename());
	return new ThemeResource("../../../images/fileManagement/" + iconFile);
    }

    private String getIconFile(final String filename) {
	final int lastDot = filename.lastIndexOf('.');
	final String fileSuffix = lastDot < 0 ? "file" : filename.substring(lastDot + 1);
	return "fileicons/" + StringUtils.lowerCase(fileSuffix) + ".png";
    }

    public Resource getIcon() {
	return getThemeResource(this);
    }

    public void unshare(VisibilityGroup group) {
	removeVisibilityGroup(group);
    }

    public String getType() {
	if (isFile()) {
	    return getMessage("node.type.file");
	}
	if (isDir()) {
	    return getMessage("node.type.dir");
	}
	return getMessage("label.empty");
    }
}