package module.fileManagement.domain;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import jvstm.cps.ConsistencyPredicate;
import module.fileManagement.domain.exception.CannotCreateFileException;
import module.fileManagement.domain.exception.NoAvailableQuotaException;
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import module.organization.domain.Party;
import myorg.domain.User;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import pt.ist.fenixWebFramework.services.Service;
import dml.runtime.DirectRelation;
import dml.runtime.Relation;
import dml.runtime.RelationListener;

public class DirNode extends DirNode_Base {

    public static final long USER_REPOSITORY_QUOTA = 50 * 1024 * 1024;
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
//		System.out.println(String.format("added child %s to %s", arg1 != null ? arg1.getDisplayName() : "null",
//			arg2 != null ? arg2.getDisplayName() : "null"));
	    }

	    @Override
	    public void afterRemove(Relation<AbstractFileNode, DirNode> arg0, AbstractFileNode arg1, DirNode arg2) {
//		System.out.println(String.format("remove child %s from %s", (arg1 != null ? arg1.getDisplayName() : "null"),
//			(arg2 != null ? arg2.getDisplayName() : "null")));
//		if (arg1 != null && arg2 != null) {
//		    if (arg1 instanceof FileNode && !arg1.isShared()) {
//			arg2.removeUsedSpace(arg1.getFilesize());
//		    }
//		}
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
	trash.setTrashUser(user);
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
    public DirNode createDir(final String dirName) {
	final DirNode searchDir = searchDir(dirName);
	if (searchDir != null) {
	    throw new NodeDuplicateNameException();
	}
	return new DirNode(this, dirName);
    }

    @Service
    public DirNode createDir(final String dirName, final PersistentGroup readGroup, final PersistentGroup writeGroup) {
	final DirNode dirNode = createDir(dirName);
	dirNode.setReadGroup(readGroup);
	dirNode.setWriteGroup(writeGroup);
	return dirNode;
    }

    @Override
    public String getDisplayName() {
	return getName();
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

    // file creation

    @Service
    public FileNode createFile(final File file, final String fileName, final long filesize) {

	FileNode fileNode = searchFile(fileName);

	if (fileNode != null) {

	    if (!fileNode.isWriteGroupMember()) {
		throw new CannotCreateFileException(fileName);
	    }

	    if (!hasAvailableQuota(filesize, fileNode)) {
		throw new NoAvailableQuotaException();
	    }
	    // TODO : check case when fileNode isShared
	    final DirNode targetDir = fileNode.isShared() ? fileNode.getParent() : this;
	    targetDir.removeUsedSpace(fileNode.getFilesize());
	    fileNode.getDocument().addVersion(file, fileName);
	    targetDir.addUsedSpace(filesize);
	    return fileNode;

	} else {

	    if (!isWriteGroupMember()) {
		throw new CannotCreateFileException(fileName);
	    }

	    if (!hasAvailableQuota(filesize)) {
		throw new NoAvailableQuotaException();
	    }

	    fileNode = new FileNode(this, file, fileName);
	    addUsedSpace(filesize);
	    return fileNode;
	}
    }

    @Service
    public FileNode createFile(final File file, final String fileName, final PersistentGroup readGroup,
	    final PersistentGroup writeGroup) throws CannotCreateFileException {
	final FileNode fileNode = createFile(file, fileName, file.length());
	fileNode.setReadGroup(readGroup);
	fileNode.setWriteGroup(writeGroup);
	return fileNode;
    }

    // size and quota

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
	setSize(getSize() + filesize);
    }

    @Service
    private void removeUsedSpace(long filesize) {
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

	for (AbstractFileNode node : getChild()) {
	    if (!node.isShared() && node.isDir()) {
		if (!((DirNode) node).hasQuotaDefined()) {
		    final long usedSpace = ((DirNode) node).getDirUsedSpace();
		    System.out.println("used space of " + node.getDisplayName() + " is " + usedSpace);
		    size += usedSpace;
		}
	    }
	}
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
    public int getFilesize() {
	return 0;
    }

    public boolean hasAvailableQuota(final long length) {
	return hasAvailableQuota(length, null);
    }

    public boolean hasAvailableQuota(final long length, FileNode fileNode) {
	if (fileNode != null && fileNode.isShared()) {
	    return fileNode.getParent().hasAvailableQuota(length, fileNode);
	}
	final Long quota = getQuota();
	final int fileNodeSize = fileNode != null ? fileNode.getFilesize() : 0;
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
    @Override
    public Party getOwner() {
	if (!hasParent()) {
	    return (TRASH_DIR_NAME.equals(getName()) ? getTrashUser() : getUser()).getPerson();
	}
	return getParent().getOwner();
    }

    public boolean hasSharedNode(AbstractFileNode selectedNode) {
	for (AbstractFileNode node : getChild()) {
	    if (selectedNode.isFile() && node instanceof SharedFileNode) {
		if (((SharedFileNode) node).getNode().equals(selectedNode)) {
		    return true;
		}
	    }
	    if (selectedNode.isDir() && node instanceof SharedDirNode) {
		if (((SharedDirNode) node).getNode().equals(selectedNode)) {
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
	final AbstractFileNode node = searchNode(child.getDisplayName());
	if (node != null) {
	    throw new NodeDuplicateNameException();
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

    @ConsistencyPredicate
    protected final boolean hasParentOrUser() {
	// return getParent() != null || getUser() != null;
	return true;
    }
    
    @Override
    public void setSize(Long size) {
	super.setSize(size);
    }
    
//    @ConsistencyPredicate
//    protected final boolean checkSize() {
//	return getSize() >= 0;
//    }

}
