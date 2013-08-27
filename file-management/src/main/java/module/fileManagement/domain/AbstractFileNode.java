package module.fileManagement.domain;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import module.fileManagement.domain.exception.FileManagementDomainException;
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import module.fileManagement.domain.log.DeleteDirLog;
import module.fileManagement.domain.log.DeleteFileLog;
import module.fileManagement.domain.log.ShareDirLog;
import module.fileManagement.domain.log.ShareFileLog;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.legacy.AnyoneGroup;
import pt.ist.bennu.core.domain.groups.legacy.PersistentGroup;
import pt.ist.bennu.core.domain.groups.legacy.SingleUserGroup;
import pt.ist.bennu.core.security.Authenticate;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.NaturalOrderComparator;

public abstract class AbstractFileNode extends AbstractFileNode_Base implements Comparable<AbstractFileNode> {
    final static NaturalOrderComparator STRING_NATURAL_COMPARATOR;

    static {
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
        setParent(null);
        deleteDomainObject();
    }

    @Atomic
    public void deleteService() {
        delete();
    }

    public abstract int getCountFiles();

    public abstract DirNode getTopDirNode();

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

    public ContextPath getContextPath() {
        final List<DirNode> nodes = new ArrayList<DirNode>();
        getPath(this, nodes);
        return new ContextPath(nodes);
    }

    private void getPath(final AbstractFileNode child, final List<DirNode> nodes) {
        if (child.getParent() != null) {
            getPath(child.getParent(), nodes);
        }
        if (child instanceof DirNode) {
            nodes.add((DirNode) child);
        }
    }

    public void trash() {
        trash(getContextPath());
    }

    @Atomic
    public void trash(ContextPath contextPath) {
        if (!isWriteGroupMember()) {
            throw new FileManagementDomainException("no.write.permissions", getDisplayName());
        }
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
        final User currentUser = Authenticate.getUser();
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
        final User user = Authenticate.getUser();
        return group.isMember(user);
    }

    public boolean isAccessible() {
        return (isReadGroupMember() || isWriteGroupMember()) && !isInTrash();
    }

    public boolean isInTrash() {
        return (getParent() != null) ? getParent().isInTrash() : false;
    }

    public abstract String getDisplayName();

    @Atomic
    public Boolean moveTo(final DirNode dirNode) throws NodeDuplicateNameException {
        if (dirNode != null) {
            if (dirNode.isWriteGroupMember()) {
                if (dirNode.hasNode(getDisplayName())) {
                    throw new NodeDuplicateNameException(getDisplayName());
                }
                setParent(dirNode);
                return true;
            }
        }
        return false;
    }

    public void setDisplayName(String displayName) throws NodeDuplicateNameException {
        DirNode parent = getParent();

        if (!isWriteGroupMember()) {
            throw new WriteDeniedException();
        }

        if (parent == null) {
            parent = (DirNode) this;
        }

        final AbstractFileNode searchNode = parent.searchNode(displayName);
        if (searchNode != null && !searchNode.equals(this)) {
            throw new NodeDuplicateNameException(displayName);
        }

    }

    public VisibilityState getVisibilityState() {
        final PersistentGroup readGroup = getReadGroup();
        return readGroup instanceof SingleUserGroup ? VisibilityState.PRIVATE : readGroup instanceof AnyoneGroup ? VisibilityState.PUBLIC : VisibilityState.SHARED;
    }

    public String getVisibility() {
        return FileManagementSystem.getMessage("label.visibility." + getVisibilityState().toString().toLowerCase());
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
        if (isDir() && node.isDir() || isFile() && node.isFile()) {
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

    @Atomic
    public void setVisibility(final VisibilityList visibilityList) {
        final PersistentGroup writeGroup = getWriteGroup();
        final PersistentGroup newWriteGroup = visibilityList.getWriteGroup();
        if (newWriteGroup == null) {
            throw new FileManagementDomainException("error.file.must.have.at.least.one.write");
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
        return node.getParent() != null ? hasSharedNode(targetFolder, node.getParent()) : false;
    }

    @Atomic
    public void share(User user, VisibilityGroup group, ContextPath contextPath) {
        if (isShared()) {
            return;
        }
        final DirNode userRootDir = FileRepository.getOrCreateFileRepository(user);

        final DirNode sharedFolder = userRootDir.getSharedFolder();
        if (!hasSharedNode(sharedFolder, this)) { // TODO: deal with same name
            // when shared later
            final AbstractFileNode sharedNode = makeSharedNode(Authenticate.getUser(), contextPath, group, sharedFolder);
            sharedFolder.addChild(sharedNode);
        } else {
            FileManagementSystem.getLogger().warn(
                    String.format("No need to create link for %s. already present", getDisplayName()));
        }

        addVisibilityGroup(group);
        // final User currentUser = Authenticate.getUser();
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
        return (getParent() != null) ? getParent().getOwner() : null;
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

    public abstract void recoverTo(DirNode targetDir);

    public boolean hasSameReadWriteGroup(AbstractFileNode node) {
        return getReadGroup().equals(node.getReadGroup()) && getWriteGroup().equals(node.getWriteGroup());
    }
}
