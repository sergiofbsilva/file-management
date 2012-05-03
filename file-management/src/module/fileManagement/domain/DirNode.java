package module.fileManagement.domain;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jvstm.cps.ConsistencyPredicate;
import module.fileManagement.domain.exception.CannotCreateFileException;
import module.fileManagement.domain.exception.NoAvailableQuotaException;
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import module.fileManagement.domain.log.CreateDirLog;
import module.fileManagement.domain.log.CreateFileLog;
import module.fileManagement.domain.log.CreateNewVersionLog;
import module.fileManagement.tools.StringUtils;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import pt.ist.fenixWebFramework.services.Service;
import dml.runtime.DirectRelation;
import dml.runtime.Relation;
import dml.runtime.RelationListener;

public class DirNode extends DirNode_Base {

    public static final long USER_REPOSITORY_QUOTA = 500 * 1024 * 1024;
    private static final long TRASH_REPOSITORY_QUOTA = Long.MAX_VALUE;
    public static final String SHARED_DIR_NAME = "Ficheiros Partilhados";
    public static final String TRASH_DIR_NAME = "Lixo";

    private static Set<String> INVALID_USER_DIR_NAMES = new HashSet<String>();

    static {
	INVALID_USER_DIR_NAMES.add(SHARED_DIR_NAME);
	INVALID_USER_DIR_NAMES.add(TRASH_DIR_NAME);
	DirectRelation<AbstractFileNode, DirNode> child = (DirectRelation<AbstractFileNode, DirNode>) DirNodeAbstractFileNode
		.getInverseRelation();
	child.addListener(new RelationListener<AbstractFileNode, DirNode>() {

	    @Override
	    public void afterAdd(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
		// System.out.println(String.format("added child %s to %s", arg1
		// != null ? arg1.getDisplayName() : "null",
		// arg2 != null ? arg2.getDisplayName() : "null"));
		if (arg1 != null && arg2 != null && !arg1.isShared()) {
		    arg2.addUsedSpace(arg1.getFilesize());
		}

	    }

	    @Override
	    public void afterRemove(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
		// System.out.println(String.format("remove child %s from %s",
		// (arg1 != null ? arg1.getDisplayName() : "null"),
		// (arg2 != null ? arg2.getDisplayName() : "null")));
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
    }

    // constructors

    public DirNode() {
	super();
	setQuota(null);
	setSize((long) 0);
    }

    public DirNode(final User user) {
	this();
	setQuota(USER_REPOSITORY_QUOTA);
	setUser(user);
	final String dirName = user.getPresentationName();
	setName(dirName);
	createSharedFolder();
	final PersistentGroup group = SingleUserGroup.getOrCreateGroup(user);
	setReadGroup(group);
	setWriteGroup(group);
	createTrashFolder(user);
    }

    public DirNode(final DirNode dirNode, final String name) {
	this();
	setParent(dirNode);
	setName(name);
    }

    /*
     * public DirNode(final Unit unit) { super(); setUnit(unit);
     * setName(unit.getPresentationName()); final AccountabilityType[]
     * memberTypes = new AccountabilityType[] {
     * IstAccountabilityType.PERSONNEL.readAccountabilityType(),
     * IstAccountabilityType.TEACHING_PERSONNEL.readAccountabilityType(),
     * IstAccountabilityType.RESEARCH_PERSONNEL.readAccountabilityType(),
     * IstAccountabilityType.GRANT_OWNER_PERSONNEL.readAccountabilityType(), };
     * final AccountabilityType[] childUnitTypes = new AccountabilityType[] {
     * IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType(), };
     * setReadGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, childUnitTypes
     * )); setWriteGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, null ));
     * }
     */

    public void createTrashFolder(User user) {
	DirNode trash = new DirNode();
	trash.setQuota(TRASH_REPOSITORY_QUOTA);
	trash.setName(TRASH_DIR_NAME);
	trash.setReadGroup(getReadGroup());
	trash.setWriteGroup(getWriteGroup());
	/* trash.setTrashUser(user); */
	setTrash(trash);
    }

    // shared

    public boolean isSharedFilesDirectory() {
	return getName().equals(SHARED_DIR_NAME);
    }

    public DirNode createSharedFolder() {
	DirNode dirNode = new DirNode(this, SHARED_DIR_NAME);
	dirNode.setWriteGroup(EmptyGroup.getInstance());
	return dirNode;
    }

    public DirNode getSharedFolder() {
	if (!hasParent()) { // is root
	    for (AbstractFileNode node : getChildSet()) {
		if (node instanceof DirNode) {
		    DirNode dirNode = (DirNode) node;
		    if (SHARED_DIR_NAME.equals(dirNode.getName())) {
			return dirNode;
		    }
		}
	    }
	    return createSharedFolder();
	}
	return getParent().getSharedFolder();
    }

    public DirNode getTrash() {
	return hasParent() ? getParent().getTrash() : super.getTrash();
    }

    public String getRepositoryName() {
	return hasParent() ? getParent().getRepositoryName() : getName();
    }

    /*
     * @Service public void initIfNecessary() { if (!hasAnyChild()) { final
     * DirNode dirNode = new DirNode(this, "Documentos Oficiais");
     * dirNode.setWriteGroup(EmptyGroup.getInstance()); new DirNode(dirNode,
     * "Contracto"); new DirNode(dirNode, "IRS"); new DirNode(dirNode,
     * "Vencimento");
     * 
     * new DirNode(this, "Os Meus Documentos");
     * 
     * final DirNode sharedDirNode = new DirNode(this,
     * "Documentos Partilhados");
     * sharedDirNode.setReadGroup(UserGroup.getInstance()); } }
     */

    @Override
    public boolean isDir() {
	return true;
    }

    @Service
    public DirNode createDir(final String dirName, ContextPath contextPath) {
	final DirNode searchDir = searchDir(dirName);
	if (searchDir != null) {
	    throw new NodeDuplicateNameException();
	}
	final DirNode resultNode = new DirNode(this, dirName);
	new CreateDirLog(UserView.getCurrentUser(), contextPath, resultNode);
	return resultNode;
    }

    @Service
    public DirNode createDir(final String dirName, final PersistentGroup readGroup, final PersistentGroup writeGroup,
	    ContextPath contextPath) {
	final DirNode dirNode = createDir(dirName, contextPath);
	dirNode.setReadGroup(readGroup);
	dirNode.setWriteGroup(writeGroup);
	return dirNode;
    }

    public DirNode createDir(final String dirName, final PersistentGroup readGroup, final PersistentGroup writeGroup) {
	final DirNode dirNode = createDir(dirName, this.getContextPath());
	dirNode.setReadGroup(readGroup);
	dirNode.setWriteGroup(writeGroup);
	return dirNode;
    }

    @Override
    public String getDisplayName() {
	return hasUser() ? FileManagementSystem.getMessage("label.menu.home") : getName();
    }

    @Service
    public void setDisplayName(final String displayName) throws NodeDuplicateNameException {
	final DirNode parent = getParent();

	if (parent == null) {
	    throw new NodeDuplicateNameException();
	}

	final AbstractFileNode searchNode = parent.searchNode(displayName);
	if (searchNode != null) {
	    throw new NodeDuplicateNameException();
	}

	setName(displayName);
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

    @Override
    public boolean search(String searchText) {
	return StringUtils.matches(getDisplayName(), searchText);
    }

    public Set<AbstractFileNode> doSearch(String searchText) {
	Set<AbstractFileNode> nodes = new HashSet<AbstractFileNode>();
	/*
	 * if (search(searchText)) { nodes.add(this); }
	 */

	for (AbstractFileNode child : getChild()) {
	    if (child.isDir()) {
		nodes.addAll(((DirNode) child).doSearch(searchText));
	    }
	    if (child.isFile()) {
		if (((FileNode) child).search(searchText)) {
		    nodes.add(child);
		}
	    }
	}

	return nodes;
    }

    // file creation

    @Service
    public FileNode createFile(final File file, final String fileName, final long filesize, final ContextPath contextPath) {

	FileNode fileNode = searchFile(fileName);

	if (fileNode != null) {

	    if (!fileNode.isWriteGroupMember()) {
		throw new CannotCreateFileException(fileName);
	    }

	    if (!hasAvailableQuota(filesize, fileNode)) {
		throw new NoAvailableQuotaException();
	    }
	    final DirNode targetDir = fileNode.isShared() ? ((SharedFileNode) fileNode).getNode().getParent() : this;
	    targetDir.removeUsedSpace(fileNode.getFilesize());
	    final Document document = fileNode.getDocument();
	    document.addVersion(file, fileName);
	    targetDir.addUsedSpace(filesize);
	    new CreateNewVersionLog(UserView.getCurrentUser(), contextPath, fileNode);
	    return fileNode;

	} else {

	    if (!isWriteGroupMember()) {
		throw new WriteDeniedException();
	    }

	    if (!hasAvailableQuota(filesize)) {
		throw new NoAvailableQuotaException();
	    }

	    fileNode = new FileNode(this, file, fileName);
	    new CreateFileLog(UserView.getCurrentUser(), contextPath, fileNode);
	    return fileNode;
	}
    }

    @Service
    public FileNode createFile(final File file, final String fileName, final PersistentGroup readGroup,
	    final PersistentGroup writeGroup) {
	final FileNode fileNode = createFile(file, fileName, file.length(), new ContextPath(this));
	fileNode.setReadGroup(readGroup);
	fileNode.setWriteGroup(writeGroup);
	return fileNode;
    }

    @Service
    public FileNode createFile(final File file, final String fileName, final PersistentGroup readGroup,
	    final PersistentGroup writeGroup, final ContextPath contextPath) {
	final FileNode fileNode = createFile(file, fileName, file.length(), contextPath);
	fileNode.setReadGroup(readGroup);
	fileNode.setWriteGroup(writeGroup);
	return fileNode;
    }

    // size and quota

    @Override
    public int getCountFiles() {
	int result = 0;
	for (final AbstractFileNode node : getChildSet()) {
	    if (node.isFile() || node.isShared()) {
		result++;
	    } else {
		result += ((DirNode) node).getCountFiles();
	    }
	}
	return result;
    }

    @Service
    private void addUsedSpace(long filesize) {
	if (hasParent()) {
	    getParent().addUsedSpace(filesize);
	}
	setSize(getSize() + filesize);
    }

    @Service
    private void removeUsedSpace(long filesize) {
	if (hasParent()) {
	    getParent().removeUsedSpace(filesize);
	}
	setSize(getSize() - filesize);
    }

    @Override
    public String getPresentationFilesize() {
	return VersionedFile.FILE_SIZE_UNIT.prettyPrint(getDirUsedSpace());
    }

    public String getPresentationQuota() {
	return VersionedFile.FILE_SIZE_UNIT.prettyPrint(getQuota());
    }

    public String getPercentOfTotalUsedSpace() {
	return new DecimalFormat("0.0#%").format(getUsedSpace() / (getQuota() * 1.0F));
    }

    public String getPresentationTotalUsedSpace() {
	return VersionedFile.FILE_SIZE_UNIT.prettyPrint(getUsedSpace());
    }

    public long getAvailableSpace() {
	return getQuota() - getUsedSpace();
    }

    @Override
    public Long getQuota() {
	return hasQuotaDefined() ? super.getQuota() : (hasParent() ? getParent().getQuota() : 0);
    }

    public boolean hasQuotaDefined() {
	return super.getQuota() != null;
    }

    /*
     * The actual size of the files and folders of this dir Shared files and
     * dirs with defined quota are ignored for this purpose.
     */
    private long getDirUsedSpace() {
	long size = getSize();

	// for (AbstractFileNode node : getChild()) {
	// if (!node.isShared() && node.isDir()) {
	// if (!((DirNode) node).hasQuotaDefined()) {
	// final long usedSpace = ((DirNode) node).getDirUsedSpace();
	// System.out.println("used space of " + node.getDisplayName() + " is "
	// + usedSpace);
	// size += usedSpace;
	// }
	// }
	// }
	return size;
    }

    /*
     * If this dir has quota then the used space is the sum of it's size and
     * contained dirs. If no quota is defined then the used space is calculated
     * by the parent
     */
    public long getUsedSpace() {
	return hasQuotaDefined() ? getDirUsedSpace() : (hasParent() ? getParent().getUsedSpace() : getDirUsedSpace());
    }

    @Override
    public long getFilesize() {
	return getSize();
    }

    public boolean hasAvailableQuota(final long length) {
	return hasAvailableQuota(length, null);
    }

    public boolean hasAvailableQuota(final long length, FileNode fileNode) {
	if (fileNode != null && fileNode.isShared()) {
	    return fileNode.getParent().hasAvailableQuota(length, fileNode);
	}
	final Long quota = getQuota();
	final long fileNodeSize = fileNode != null ? fileNode.getFilesize() : 0;
	return (getUsedSpace() + length) <= (quota + fileNodeSize);
    }

    public AbstractFileNode searchNode(final String displayName) {
	for (AbstractFileNode node : getChildSet()) {
	    if (node.getDisplayName().equals(displayName)) {
		return node;
	    }
	}
	return null;
    }

    public FileNode searchFile(final String fileName) {
	final AbstractFileNode searchNode = searchNode(fileName);
	return searchNode != null && searchNode instanceof FileNode ? (FileNode) searchNode : null;
    }

    public DirNode searchDir(final String dirName) {
	final AbstractFileNode searchNode = searchNode(dirName);
	return searchNode != null && searchNode instanceof DirNode ? (DirNode) searchNode : null;
    }

    /*
     * private boolean hasAvailableQuotaInRepository(final long length) { final
     * User user = getUser(); return user == null || getUsedSpace() + length <
     * USER_REPOSITORY_QUOTA; }
     */
    /*
     * private void calculatePaths(List<String> paths) { if (!hasParent()) {
     * paths.add(getUser().getUsername()); } else { paths.add(getDisplayName());
     * getParent().calculatePaths(paths); } }
     */

    /*
     * public String getAbsolutePath() { String ret = new String();
     * ArrayList<String> paths = new ArrayList<String>(); calculatePaths(paths);
     * final ListIterator<String> iterator = paths.listIterator(paths.size());
     * while (iterator.hasPrevious()) { ret += iterator.previous(); if
     * (iterator.hasPrevious()) { ret += " > "; } } return ret; }
     */

    /*
     * public List<DirNode> getAllDirs() { List<DirNode> nodes = new
     * ArrayList<DirNode>(); nodes.add(this); for (AbstractFileNode node :
     * getChildSet()) { if (node.isDir()) { final DirNode dirNode = (DirNode)
     * node; nodes.addAll(dirNode.getAllDirs()); } } return nodes; }
     */

    public boolean hasSharedNode(AbstractFileNode selectedNode) {
	for (AbstractFileNode node : getChild()) {
	    if (!(node instanceof SharedNode)) {
		if (node.isDir()) {
		    return ((DirNode) node).hasSharedNode(selectedNode);
		}
	    } else {
		if (selectedNode.equals(((SharedNode) node).getNode())) {
		    return true;
		}
	    }
	}
	return false;
    }

    public boolean hasAnyChildFile() {
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    if (abstractFileNode.isFile()) {
		return true;
	    }
	}
	return false;
    }

    public boolean hasAnyChildDir() {
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    if (abstractFileNode.isDir()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void addChild(AbstractFileNode child) throws NodeDuplicateNameException {
	if (!child.isShared()) {
	    final AbstractFileNode node = searchNode(child.getDisplayName());
	    if (node != null) {
		throw new NodeDuplicateNameException();
	    }
	}
	super.addChild(child);
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	return node.isFile() ? -1 : super.compareTo(node);
    }

    @Override
    public void delete() {
	removeUser();
	// removeUnit();
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    abstractFileNode.delete();
	}
	super.delete();
    }

    public boolean isInTrash() {
	if (hasParent()) {
	    return getParent().isInTrash();
	}
	return hasRootDirNode();
    }

    @Override
    public User getOwner() {
	if (hasParent()) {
	    return getParent().getOwner();
	}
	return hasRootDirNode() ? getRootDirNode().getUser() : getUser();
    }

    // @ConsistencyPredicate
    // protected final boolean checkSize() {
    // return getSize() >= 0;
    // }

    /*
     * the top level dirs are the root dir or the trash bin.
     */
    @ConsistencyPredicate
    public boolean checkParent() {
	return !hasParent() ? hasTrashUser() || hasUser() || hasRootDirNode() : true;
    }

    @Override
    public void unshare(VisibilityGroup group) {
	super.unshare(group);
	for (SharedDirNode sharedNode : getSharedDirNodes()) {
	    sharedNode.deleteLink(new ContextPath(getParent()));
	}
    }

    public ContextPath getContextPath() {
	final List<DirNode> nodes = new ArrayList<DirNode>();
	getPath(this, nodes);
	return new ContextPath(nodes);
    }

    private void getPath(DirNode child, List<DirNode> nodes) {
	if (child.hasParent()) {
	    getPath(child.getParent(), nodes);
	}
	nodes.add(child);
    }
}
