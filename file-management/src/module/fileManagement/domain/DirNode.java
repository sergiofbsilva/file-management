package module.fileManagement.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import module.organization.domain.Party;
import myorg.domain.User;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import myorg.domain.groups.UserGroup;
import pt.ist.fenixWebFramework.services.Service;

public class DirNode extends DirNode_Base {

    private static final long USER_REPOSITORY_QUOTA = 50 * 1024 * 1024;
    public static final String SHARED_DIR_NAME = "Ficheiros Partilhados";
    public static final String TRASH_DIR_NAME = "Lixo";
    
    public DirNode() {
	super();
    }
    
    public boolean isSharedFilesDirectory() {
	return getName().equals(SHARED_DIR_NAME);
    }
    
    public DirNode(final User user) {
        super();
        setUser(user);
        final String dirName = user.getPresentationName();
	setName(dirName);
	createSharedFolder();
	final PersistentGroup group = SingleUserGroup.getOrCreateGroup(user);
        setReadGroup(group);
        setWriteGroup(group);
        createTrashFolder(user);
    }

    /*public DirNode(final Unit unit) {
        super();
        setUnit(unit);
        setName(unit.getPresentationName());
        final AccountabilityType[] memberTypes = new AccountabilityType[] {
        	IstAccountabilityType.PERSONNEL.readAccountabilityType(),
        	IstAccountabilityType.TEACHING_PERSONNEL.readAccountabilityType(),
        	IstAccountabilityType.RESEARCH_PERSONNEL.readAccountabilityType(),
        	IstAccountabilityType.GRANT_OWNER_PERSONNEL.readAccountabilityType(),
        };
        final AccountabilityType[] childUnitTypes = new AccountabilityType[] {
        	IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType(),
        };
        setReadGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, childUnitTypes ));
        setWriteGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, null ));
    }*/

    private void createTrashFolder(User user) {
	DirNode trash = new DirNode();
	trash.setName(TRASH_DIR_NAME);
	trash.setReadGroup(getReadGroup());
	trash.setWriteGroup(getWriteGroup());
	user.setTrash(trash);
    }

    public DirNode(final DirNode dirNode, final String name) {
	super();
	setParent(dirNode);
	setName(name);
    }
    
    private DirNode createSharedFolder() {
	DirNode sharedDir = new DirNode(this,SHARED_DIR_NAME);
	addChild(sharedDir);
	return sharedDir;
    }
    
    public DirNode getSharedFolder() {
	if (!hasParent()) { // is root
	    for(AbstractFileNode node : getChildSet()) {
		if (node instanceof DirNode) {
		    DirNode dirNode = (DirNode)node;
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

    @Service
    public void initIfNecessary() {
	if (!hasAnyChild()) {
	    final DirNode dirNode = new DirNode(this, "Documentos Oficiais");
	    dirNode.setWriteGroup(EmptyGroup.getInstance());
	    new DirNode(dirNode, "Contracto");
	    new DirNode(dirNode, "IRS");
	    new DirNode(dirNode, "Vencimento");

	    new DirNode(this, "Os Meus Documentos");

	    final DirNode sharedDirNode = new DirNode(this, "Documentos Partilhados");
	    sharedDirNode.setReadGroup(UserGroup.getInstance());
	}
    }

    @Override
    public boolean isDir() {
        return true;
    }
    
    @Service
    public DirNode createDir(final String dirName) {
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
    public void delete() {
	removeUser();
//	removeUnit();
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    abstractFileNode.delete();
	}
	super.delete();
    }
    
    @Service
    public void edit(final String name) {
	setName(name);
    }

    public int countFiles() {
	int result = 0;
	for (final AbstractFileNode node : getChildSet()) {
	    if (node.isFile() || node.isShared()) {
		result++;
	    } else {
		result += ((DirNode) node).countFiles();
	    }
	}
	return result;
    }
    
    public int getCountFiles() {
	return countFiles();
    }

    public long countFilesSize() {
	long result = 0;
	for (final AbstractFileNode node : getChildSet()) {
	    if (node.isFile()) {
		// TODO
	    } else {
		result += ((DirNode) node).countFiles();
	    }
	}
	return result;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void setDisplayName(final String displayName) {
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

    @Service
    public FileNode createFile(final File file, final String fileName) {
	FileNode fileNode = searchFile(fileName);
	if (fileNode != null) {
	    fileNode.getDocument().addVersion(file, fileName);
	    return fileNode;
	}
	fileNode = new FileNode(this, file, fileName);
	return fileNode;
    }
    
    
    public FileNode searchFile(final String fileName) {
	for(AbstractFileNode node : getChildSet()) {
	    if (node instanceof FileNode) {
		if (node.getDisplayName().equals(fileName)) {
		    return (FileNode) node;
		}
	    }
	}
	return null;
    }
    
    @Service
    public FileNode createFile(final File file, final String fileName,
	    final PersistentGroup readGroup, final PersistentGroup writeGroup) {
	final FileNode fileNode = createFile(file, fileName);
	fileNode.setReadGroup(readGroup);
	fileNode.setWriteGroup(writeGroup);
	return fileNode;
    }
    
    @Override
    public int compareTo(final AbstractFileNode node) {
	return node.isFile() ? -1 : super.compareTo(node);
    }

    @Override
    public String getPresentationFilesize() {
	return VersionedFile.FILE_SIZE_UNIT.prettyPring(getFilesize());
    }
    
    @Override
    public int getFilesize() {
	int sum = 0;
	for (AbstractFileNode node : getChild()) {
	    if (!node.isShared()) {
		sum += node.getFilesize();
	    }
	}
	return sum;
    }

    public boolean hasAvailableQuota(final long length) {
	return hasParent() ? getParent().hasAvailableQuota(length) : hasAvailableQuotaInRepository(length);
    }

    private boolean hasAvailableQuotaInRepository(final long length) {
	final User user = getUser();
	return user == null || getUsedSpace() + length < USER_REPOSITORY_QUOTA;
    }

    protected long getUsedSpace() {
	long result = 0;
	for (final AbstractFileNode abstractFileNode : getChildSet()) {
	    if (abstractFileNode.isDir()) {
		final DirNode dirNode = (DirNode) abstractFileNode;
		result += dirNode.getUsedSpace();
	    } else if (abstractFileNode.isFile()) {
		final FileNode fileNode = (FileNode) abstractFileNode;
		final Document document = fileNode.getDocument();
		for (VersionedFile file = document.getLastVersionedFile(); file != null; file = file.getPreviousVersion() ) {
		    result += file.getContent().length;
		}
	    }
	}
	return result;
    }
    
    private void calculatePaths(List<String> paths) {
	if (!hasParent()) {
	    paths.add(getUser().getUsername());
	} else {
	    paths.add(getDisplayName());
	    getParent().calculatePaths(paths);
	}
    }
    
    public String getAbsolutePath() {
	String ret = new String();
	ArrayList<String> paths = new ArrayList<String>();
	calculatePaths(paths);
	final ListIterator<String> iterator = paths.listIterator(paths.size());
	while(iterator.hasPrevious()) {
	    ret += iterator.previous();
	    if (iterator.hasPrevious()) {
		ret += " > ";
	    }
	}
	return ret;
    }
    
    public List<DirNode> getAllDirs() {
	List<DirNode> nodes = new ArrayList<DirNode>();
	nodes.add(this);
	for(AbstractFileNode node : getChildSet()) {
	    if (node.isDir()) {
		final DirNode dirNode = (DirNode) node;
		nodes.addAll(dirNode.getAllDirs());
	    }
	}
	return nodes;
    }
    
    @Override
    public Party getOwner() {
	if (!hasParent()) {
	    return getUser().getPerson();
	}
	return getParent().getOwner();
    }
}

