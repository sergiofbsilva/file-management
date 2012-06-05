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
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.SeqNumberMetadata;
import module.fileManagement.tools.StringUtils;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.groups.UnitGroup;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.RoleType;
import myorg.domain.User;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.Role;
import myorg.domain.groups.SingleUserGroup;
import myorg.domain.groups.UnionGroup;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.FFDomainException;

/**
 * @author Sergio Silva
*/
public class DirNode extends DirNode_Base {

    public static final long PERSON_REPOSITORY_QUOTA = 50 * 1024 * 1024;
    public static final long UNIT_REPOSITORY_QUOTA = 1024 * 1024 * 1024;
    private static final long TRASH_REPOSITORY_QUOTA = Long.MAX_VALUE;
    public static final String SHARED_DIR_NAME = "Ficheiros Partilhados";
    public static final String TRASH_DIR_NAME = "Lixo";

    private static Set<String> INVALID_USER_DIR_NAMES = new HashSet<String>();

    static {
	INVALID_USER_DIR_NAMES.add(SHARED_DIR_NAME);
	INVALID_USER_DIR_NAMES.add(TRASH_DIR_NAME);
    }

    // constructors

    public DirNode() {
	super();
	setQuota(null);
	setSize((long) 0);
    }

    public DirNode(final User user) {
	this(user.getPerson());
    }

    public DirNode(final Party party) {
	this();
	setParty(party);
	setName(party.getPresentationName());
	if (party.isUnit()) {
	    setUnitGroup((Unit) party);
	    setQuota(UNIT_REPOSITORY_QUOTA);
	} else {
	    if (party.isPerson()) {
		createSharedFolder();
		final PersistentGroup group = SingleUserGroup.getOrCreateGroup(((Person) party).getUser());
		setReadGroup(group);
		setWriteGroup(group);
		setQuota(PERSON_REPOSITORY_QUOTA);
	    }
	}
	createTrashFolder();
    }

    private void setUnitGroup(Unit unit) {
	// final AccountabilityType[] memberTypes = new AccountabilityType[] {
	// AccountabilityType.readBy("Personnel") };
	// final AccountabilityType[] childUnitTypes = new AccountabilityType[]
	// { AccountabilityType.readBy("Organizational") };
	// setReadGroup(UnitGroup.getOrCreateGroup(unit, memberTypes,
	// childUnitTypes));
	// setWriteGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, null));
	// final AccountabilityType[] memberTypes = new AccountabilityType[] {
	// AccountabilityType.readBy("Personnel") };
	final AccountabilityType[] childUnitTypes = new AccountabilityType[] { AccountabilityType.readBy("Organizational"),
		AccountabilityType.readBy("Personnel") };
	final UnitGroup group = UnitGroup.getOrCreateGroup(unit, childUnitTypes, childUnitTypes);
	final UnionGroup unitManagerGroup = UnionGroup.getOrCreateUnionGroup(group, Role.createRole(RoleType.MANAGER));
	setReadGroup(unitManagerGroup);
	setWriteGroup(unitManagerGroup);
    }

    public DirNode(final DirNode dirNode, final String name) {
	this();
	setParent(dirNode);
	setName(name);
    }

    public void createTrashFolder() {
	DirNode trash = new DirNode();
	trash.setQuota(TRASH_REPOSITORY_QUOTA);
	trash.setName(TRASH_DIR_NAME);
	trash.setReadGroup(getReadGroup());
	trash.setWriteGroup(getWriteGroup());
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

    @Override
    public DirNode getTrash() {
	return hasParent() ? getParent().getTrash() : super.getTrash();
    }

    public String getRepositoryName() {
	return hasParent() ? getParent().getRepositoryName() : getName();
    }

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

    @Service
    public FileNode createFile() {
	if (hasSequenceNumber()) {
	    final Integer nextSequenceNumber = getNextSequenceNumber();
	    FileManagementSystem.FILENAME_TEMPLATE.setDirNode(this);
	    final String fileName = FileManagementSystem.FILENAME_TEMPLATE.getValue();
	    final Document document = new Document(fileName, fileName, StringUtils.EMPTY.getBytes());
	    if (hasDefaultTemplate()) {
		final MetadataTemplate defaultTemplate = getDefaultTemplate();
		final Set<MetadataKey> seqNumberKeys = defaultTemplate.getKeysByMetadataType(SeqNumberMetadata.class);
		for (MetadataKey key : seqNumberKeys) {
		    document.addMetadata(key.createMetadata(nextSequenceNumber));
		}
		document.setMetadataTemplateAssociated(defaultTemplate);
	    }
	    return new FileNode(this, document);
	}
	return null;
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
    void addUsedSpace(long filesize) {
	if (hasParent()) {
	    getParent().addUsedSpace(filesize);
	}
	setSize(getSize() + filesize);
    }

    @Service
    void removeUsedSpace(long filesize) {
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
	return getSize();
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
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    abstractFileNode.delete();
	}
	super.delete();
    }

    @Override
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

    /*
     * This method guarantees that a directory is within other directory. If not
     * it is a root.
     */
    @ConsistencyPredicate
    public boolean checkParent() {
	return hasParent() ? true : hasUser() || hasParty() || hasRootDirNode();
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

    /*
     * sequencedNumber is used to provide an sequenced unique identifier for
     * files within this dir
     */
    public void setSequenced() {
	if (getSequenceNumber() == null) {
	    setSequenceNumber(0);
	}
    }

    public boolean hasSequenceNumber() {
	return getSequenceNumber() != null;
    }

    @Service
    public Integer getNextSequenceNumber() {
	if (getSequenceNumber() == null) {
	    throw new FFDomainException("Sequence Number is null: Must be 0 to use a sequence number");
	}
	setSequenceNumber(getSequenceNumber() + 1);
	return getSequenceNumber();
    }
}
