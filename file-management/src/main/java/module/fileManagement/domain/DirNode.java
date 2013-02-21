package module.fileManagement.domain;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jvstm.cps.ConsistencyPredicate;
import module.fileManagement.domain.exception.NoAvailableQuotaException;
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import module.fileManagement.domain.log.CreateDirLog;
import module.fileManagement.domain.log.CreateFileLog;
import module.fileManagement.domain.log.RecoverDirLog;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.SeqNumberMetadata;
import module.fileManagement.tools.StringUtils;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.groups.UnitGroup;

import org.apache.commons.io.FileUtils;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.domain.groups.EmptyGroup;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.bennu.core.domain.groups.Role;
import pt.ist.bennu.core.domain.groups.SingleUserGroup;
import pt.ist.bennu.core.domain.groups.UnionGroup;
import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

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
        setRoot(Boolean.FALSE);
    }

    public DirNode(final User user) {
        this(user.getPerson());
    }

    public DirNode(final Party party) {
        this();
        setParty(party);
        setName(party.getPresentationName());
        setRoot(Boolean.TRUE);
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

    private void setUnitGroup(final Unit unit) {
        // final AccountabilityType[] memberTypes = new AccountabilityType[] {
        // AccountabilityType.readBy("Personnel") };
        // final AccountabilityType[] childUnitTypes = new AccountabilityType[]
        // { AccountabilityType.readBy("Organizational") };
        // setReadGroup(UnitGroup.getOrCreateGroup(unit, memberTypes,
        // childUnitTypes));
        // setWriteGroup(UnitGroup.getOrCreateGroup(unit, memberTypes, null));
        // final AccountabilityType[] memberTypes = new AccountabilityType[] {
        // AccountabilityType.readBy("Personnel") };
        final AccountabilityType[] childUnitTypes =
                new AccountabilityType[] { AccountabilityType.readBy("Organizational"), AccountabilityType.readBy("Personnel") };
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
        final DirNode trash = new DirNode();
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
        final DirNode dirNode = new DirNode(this, SHARED_DIR_NAME);
        dirNode.setWriteGroup(EmptyGroup.getInstance());
        return dirNode;
    }

    public DirNode getSharedFolder() {
        if (!hasParent()) { // is root
            for (final AbstractFileNode node : getChildSet()) {
                if (node instanceof DirNode) {
                    final DirNode dirNode = (DirNode) node;
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

    public DirNode createDir(final String dirName) {
        return createDir(dirName, getContextPath());
    }

    @Atomic
    public DirNode createDir(final String dirName, final ContextPath contextPath) {
        final DirNode searchDir = searchDir(dirName);
        if (searchDir != null) {
            throw new NodeDuplicateNameException(dirName);
        }
        final DirNode resultNode = new DirNode(this, dirName);
        new CreateDirLog(Authenticate.getCurrentUser(), contextPath, resultNode);
        return resultNode;
    }

    @Atomic
    public DirNode createDir(final String dirName, final PersistentGroup readGroup, final PersistentGroup writeGroup,
            final ContextPath contextPath) {
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

    @Override
    public boolean hasUser() {
        return hasParty() && getParty() instanceof Person;
    }

    @Override
    public void setUser(User user) {
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    @Atomic
    public void setDisplayName(final String displayName) throws NodeDuplicateNameException {
        super.setDisplayName(displayName);
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
    public boolean search(final String searchText) {
        return StringUtils.matches(getDisplayName(), searchText);
    }

    public Set<AbstractFileNode> doSearch(final String searchText) {
        final Set<AbstractFileNode> nodes = new HashSet<AbstractFileNode>();
        /*
         * if (search(searchText)) { nodes.add(this); }
         */

        for (final AbstractFileNode child : getChild()) {
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

    public Map<AbstractFileNode, Map<MetadataKey, Boolean>> doSearch(final Multimap<MetadataKey, Object> searchMap) {
        final Map<AbstractFileNode, Map<MetadataKey, Boolean>> resultMap = Maps.newHashMap();

        for (final AbstractFileNode child : getChild()) {
            if (child.isDir()) {
                resultMap.putAll(((DirNode) child).doSearch(searchMap));
            }
            if (child.isFile()) {
                final FileNode file = (FileNode) child;
                resultMap.put(file, file.search(searchMap));
            }
        }
        return resultMap;
    }

    // file creation
    @SuppressWarnings("unused")
    @Atomic
    public FileNode createFile(final byte[] fileContent, final String displayName, final String fileName, final long filesize,
            final ContextPath contextPath) {
        FileNode fileNode = searchFile(displayName);

        if (fileNode != null) {

            fileNode.addNewVersion(fileContent, fileName, displayName, filesize);
            return fileNode;

        } else {

            if (!isWriteGroupMember()) {
                throw new WriteDeniedException();
            }

            if (!hasAvailableQuota(filesize)) {
                throw new NoAvailableQuotaException();
            }

            fileNode = new FileNode(this, fileContent, fileName, displayName);
            new CreateFileLog(Authenticate.getCurrentUser(), contextPath, fileNode);
            return fileNode;
        }

    }

    @Atomic
    public FileNode createFile(final File file, final String displayName, final String fileName, final long filesize,
            final ContextPath contextPath) {
        try {
            return createFile(FileUtils.readFileToByteArray(file), displayName, fileName, filesize, contextPath);
        } catch (final IOException e) {
            throw new Error(e);
        }

    }

    @Atomic
    public FileNode createFile(final File file, final String displayName, final long filesize, final ContextPath contextPath) {
        return createFile(file, displayName, displayName, filesize, contextPath);

    }

    @Atomic
    public FileNode createFile(final File file, final String fileName, final PersistentGroup readGroup,
            final PersistentGroup writeGroup) {
        final FileNode fileNode = createFile(file, fileName, file.length(), new ContextPath(this));
        fileNode.setReadGroup(readGroup);
        fileNode.setWriteGroup(writeGroup);
        return fileNode;
    }

    @Atomic
    public FileNode createFile(final File file, final String fileName, final PersistentGroup readGroup,
            final PersistentGroup writeGroup, final ContextPath contextPath) {
        final FileNode fileNode = createFile(file, fileName, file.length(), contextPath);
        fileNode.setReadGroup(readGroup);
        fileNode.setWriteGroup(writeGroup);
        return fileNode;
    }

    @Atomic
    public FileNode createFile() {
        if (hasSequenceNumber()) {
            final Integer nextSequenceNumber = getNextSequenceNumber();
            FileManagementSystem.FILENAME_TEMPLATE.setDirNode(this);
            final String fileName = FileManagementSystem.FILENAME_TEMPLATE.getValue();
            final Document document = new Document(fileName, fileName, org.apache.commons.lang.StringUtils.EMPTY.getBytes());
            if (hasDefaultTemplate()) {
                final MetadataTemplate defaultTemplate = getDefaultTemplate();
                final Set<MetadataKey> seqNumberKeys = defaultTemplate.getKeysByMetadataType(SeqNumberMetadata.class);
                for (final MetadataKey key : seqNumberKeys) {
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

    @Atomic
    void addUsedSpace(final long filesize) {
        if (hasParent()) {
            getParent().addUsedSpace(filesize);
        }
        setSize(getSize() + filesize);
    }

    @Atomic
    void removeUsedSpace(final long filesize) {
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
        return hasQuotaDefined() ? super.getQuota() : hasParent() ? getParent().getQuota() : 0;
    }

    public boolean hasQuotaDefined() {
        return super.getQuota() != null;
    }

    /*
     * The actual size of the files and folders of this dir Shared files and dirs with defined quota are ignored for this purpose.
     */
    private long getDirUsedSpace() {
        return getSize();
    }

    /*
     * If this dir has quota then the used space is the sum of it's size and contained dirs. If no quota is defined then the used
     * space is calculated by the parent
     */
    public long getUsedSpace() {
        return hasQuotaDefined() ? getDirUsedSpace() : hasParent() ? getParent().getUsedSpace() : getDirUsedSpace();
    }

    @Override
    public long getFilesize() {
        return getSize();
    }

    public boolean hasAvailableQuota(final long length) {
        return hasAvailableQuota(length, null);
    }

    public boolean hasAvailableQuota(final long length, final FileNode fileNode) {
        if (fileNode != null && fileNode.isShared()) {
            return fileNode.getParent().hasAvailableQuota(length, fileNode);
        }

        final Long quota = getQuota();
        if (quota == Long.MAX_VALUE) {
            return true;
        }

        final long fileNodeSize = fileNode != null ? fileNode.getFilesize() : 0;
        return getUsedSpace() + length <= quota + fileNodeSize;
    }

    public AbstractFileNode searchNode(final String displayName) {
        for (final AbstractFileNode node : getChildSet()) {
            if (node.getDisplayName().equals(displayName)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasNode(final String displayName) {
        return searchNode(displayName) != null;
    }

    public FileNode searchFile(final String fileName) {
        final AbstractFileNode searchNode = searchNode(fileName);
        return searchNode != null && searchNode instanceof FileNode ? (FileNode) searchNode : null;
    }

    public DirNode searchDir(final String dirName) {
        final AbstractFileNode searchNode = searchNode(dirName);
        return searchNode != null && searchNode instanceof DirNode ? (DirNode) searchNode : null;
    }

    public boolean hasSharedNode(final AbstractFileNode selectedNode) {
        for (final AbstractFileNode node : getChild()) {
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
    public void addChild(final AbstractFileNode child) throws NodeDuplicateNameException {
        if (!child.isShared()) {
            final String displayName = child.getDisplayName();
            final AbstractFileNode node = searchNode(displayName);
            if (node != null) {
                throw new NodeDuplicateNameException(displayName);
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
    public DirNode getTopDirNode() {
        if (hasParent()) {
            return getParent().getTopDirNode();
        }
        return this;
    }

    /*
     * This method guarantees that a directory is within other directory. If not it is a root.
     */
    @ConsistencyPredicate
    public boolean checkParent() {
        return hasParent() ? true : isRoot() || isInTrash();
    }

    private boolean isRoot() {
        return getRoot() != null && getRoot();
    }

    @Override
    public void unshare(final VisibilityGroup group) {
        super.unshare(group);
        for (final SharedDirNode sharedNode : getSharedDirNodes()) {
            sharedNode.deleteLink(new ContextPath(getParent()));
        }
    }

    /*
     * sequencedNumber is used to provide an sequenced unique identifier for files within this dir
     */
    public void setSequenced() {
        if (getSequenceNumber() == null) {
            setSequenceNumber(0);
        }
    }

    public boolean hasSequenceNumber() {
        return getSequenceNumber() != null;
    }

    @Atomic
    public Integer getNextSequenceNumber() {
        if (getSequenceNumber() == null) {
            throw new DomainException("Sequence Number is null: Must be 0 to use a sequence number");
        }
        setSequenceNumber(getSequenceNumber() + 1);
        return getSequenceNumber();
    }

    @Override
    @Atomic
    public void recoverTo(DirNode targetDir) {
        new RecoverDirLog(Authenticate.getCurrentUser(), targetDir.getContextPath(), this);
        setParent(targetDir);
    }
}
