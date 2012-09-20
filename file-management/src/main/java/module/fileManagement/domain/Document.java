package module.fileManagement.domain;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.tools.StringUtils;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;

public class Document extends Document_Base {

    public Document() {
	super();
	addNewVersionMetadata();
    }

    public Document(final File file, final String fileName) throws IOException {
	this(FileUtils.readFileToByteArray(file), fileName);
    }

    public Document(final byte[] fileContent, final String fileName) {
	this();
	setLastVersionedFile(new VersionedFile(fileContent, fileName));
	addMetadata(MetadataKey.getFilenameKey(), fileName);
    }

    public Document(final File file, final String fileName, final String displayName) throws IOException {
	    this(FileUtils.readFileToByteArray(file), fileName, displayName);
    }

    public Document(final byte[] fileContent, final String fileName, final String displayName) {
	this();
	setLastVersionedFile(new VersionedFile(fileContent, fileName, displayName));
	addMetadata(MetadataKey.getFilenameKey(), fileName);
	addMetadata(MetadataKey.getDisplaynameKey(), displayName);
    }

    public Document(final String displayName, final String fileName, byte[] content) {
	this();
	setLastVersionedFile(new VersionedFile(displayName, fileName, content));
	addMetadata(MetadataKey.getFilenameKey(), fileName);
    }

    public String getDisplayName() {
	final VersionedFile file = getLastVersionedFile();
	return file.getDisplayName();
    }

    public String getFileName() {
	final VersionedFile file = getLastVersionedFile();
	return file.getFilename();
    }

    public void setDisplayName(final String displayName) {
	final VersionedFile file = getLastVersionedFile();
	file.setDisplayName(displayName);
    }

    @Service
    public void delete() {
	removeReadGroup();
	removeWriteGroup();
	for (final Metadata metadata : getMetadataSet()) {
	    metadata.delete();
	}
	for (final FileNode fileNode : getFileNodeSet()) {
	    fileNode.deleteFromDocument();
	}
	while (hasLastVersionedFile()) {
	    getLastVersionedFile().delete();
	}
	deleteDomainObject();
    }

    public void trash() {
	// setLastVersionedFile(VersionedFile.getEmptyVersionFile());
	// for (final FileNode fileNode : getFileNodeSet()) {
	// fileNode.deleteFromDocument();
	// }
    }

    private boolean match(Object stack, Object needle) {
	if (stack == null || needle == null) {
	    return false;
	}

	if (needle instanceof DateTime && stack instanceof DateTime) {
	    return ((DateTime) needle).toLocalDate().equals(((DateTime) stack).toLocalDate());
	}

	if (needle instanceof String && stack instanceof String) {
	    return StringUtils.matches((String) stack, (String) needle);
	}

	return stack.equals(needle);
    }

    /*
     * Searches for string values in metadataKey if present.
     */
    public Map<MetadataKey, Boolean> search(final Multimap<MetadataKey, Object> searchMap) {
	Map<MetadataKey, Boolean> result = new HashMap<MetadataKey, Boolean>();
	for (Metadata metadata : getMetadata()) {
	    final MetadataKey metadataKey = metadata.getMetadataKey();
	    final Object metadataValue = metadata.getValue();
	    final Collection<Object> searchValues = searchMap.get(metadataKey);
	    boolean found = false;
	    for (Object searchValue : searchValues) {
		if (searchValue != null) {
		    if (match(metadataValue, searchValue)) {
			found = true;
			break;
		    }
		}
	    }
	    result.put(metadataKey, found);
	}
	// for (Entry<MetadataKey, Object> entry : searchMap.entrySet()) {
	// Metadata metadata = getMetadata(entry.getKey());
	// if (metadata != null) {
	// final Object searchValue = entry.getValue();
	// final Object metadataValue = metadata.getValue();
	// // System.out.printf("compare %s>%s with $>%s : %s\n",
	// // searchValue.getClass().getName(), searchValue, metadataValue
	// // .getClass().getName(), metadataValue, equals);
	// result.put(entry.getKey(), match(metadataValue, searchValue));
	// }
	// }
	return result;
    }

    public boolean search(String searchText) {
	return /* matches(getDisplayName(), searchText) || */searchMetadata(searchText);
    }

    private boolean searchMetadata(String searchText) {
	for (Metadata metadata : getMetadata()) {
	    final String keyValue = metadata.getKeyValue();
	    final String value = metadata.getPresentationValue();
	    if (StringUtils.matches(keyValue, searchText) || StringUtils.matches(value, searchText)) {
		return true;
	    }
	}
	return false;
    }

    public String getPresentationFilesize() {
	final VersionedFile file = getLastVersionedFile();
	return file.getPresentationFilesize();
    }

    public long getFilesize() {
	final VersionedFile file = getLastVersionedFile();
	return file.getFilesize();
    }

    private int getVersionNumber(VersionedFile file) {
	return file.hasPreviousVersion() ? 1 + getVersionNumber(file.getPreviousVersion()) : 1;
    }

    public int getVersionNumber() {
	return getVersionNumber(getLastVersionedFile());
    }

    public void addVersion(final File file, final String filename) {
	try {
	    addVersion(FileUtils.readFileToByteArray(file), filename);
	} catch (IOException e) {
	    throw new Error(e);
	}
    }

    public void addVersion(final byte[] fileContent, final String filename) {
	final VersionedFile versionedFile = getLastVersionedFile();
	final VersionedFile newVersion = new VersionedFile(fileContent, filename);
	newVersion.setPreviousVersion(versionedFile);
	setLastVersionedFile(newVersion);
	addNewVersionMetadata();
    }

    public void addVersion(String displayName, String filename, byte[] content) {
	final VersionedFile versionedFile = getLastVersionedFile();
	final VersionedFile newVersion = new VersionedFile(displayName, filename, content);
	newVersion.setPreviousVersion(versionedFile);
	setLastVersionedFile(newVersion);
	addNewVersionMetadata();
    }

    /**
     * 
     * @param dirNode
     * @return the FileNode that is contained directly contained in the
     *         specified dirNode (no recursion is used)
     */
    public FileNode getFileNode(DirNode dirNode) {
	for (FileNode fileNode : getFileNode()) {
	    if (dirNode.equals(fileNode.getParent()))
		return fileNode;
	}
	return null;

    }

    private DateTime now() {
	return new DateTime();
    }

    public DateTime setModifiedDateNow() {
	final DateTime now = now();
	setLastModifiedDate(now);
	return now;
    }

    @Service
    public void addMetadata(Collection<Metadata> metadata) {
	for (Metadata md : metadata) {
	    addMetadata(md.hasDocument() ? md.getCopy() : md);
	}
    }

    @Service
    public void addMetadata(String key, String value) {
	addMetadata(MetadataKey.getOrCreateInstance(key), value);
    }

    @Service
    public void addMetadata(Map<String, String> keyValueMap) {
	for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
	    addMetadata(entry.getKey(), entry.getValue());
	}
    }

    @Service
    public void addMetadata(MetadataKey key, String value) {
	addMetadata(key.createMetadata(value));
    }

    public TreeSet<Metadata> getMetadataOrderedByTimestamp() {
	return new TreeSet<Metadata>(getMetadata());
    }

    public TreeSet<Metadata> getMetadataOrderedByTimestamp(MetadataKey key) {
	TreeSet<Metadata> treeSet = new TreeSet<Metadata>();
	for (Metadata metadata : getMetadata()) {
	    if (metadata.getMetadataKey().equals(key)) {
		treeSet.add(metadata);
	    }
	}
	return treeSet;
    }

    public Metadata getMetadataRecentlyChanged(MetadataKey key) {
	final TreeSet<Metadata> metadataByTimestamp = getMetadataOrderedByTimestamp(key);
	for (Metadata metadata : metadataByTimestamp.descendingSet()) {
	    if (metadata.getMetadataKey().equals(key)) {
		return metadata;
	    }
	}
	return null;
    }

    private TreeSet<Metadata> getMetadataUntil(DateTime endDate) {
	final TreeSet<Metadata> metadataByTimestamp = new TreeSet<Metadata>(Metadata.TIMESTAMP_DESC_COMPARATOR);
	final TreeSet<Metadata> metadataUntil = new TreeSet<Metadata>(Metadata.TIMESTAMP_DESC_COMPARATOR);
	metadataByTimestamp.addAll(getMetadata());
	metadataUntil.addAll(getMetadata());

	for (Metadata metadata : metadataByTimestamp) {
	    if (metadata.getTimestamp().compareTo(endDate) > 0) {
		metadataUntil.remove(metadata);
	    }
	}
	cleanup(metadataUntil);
	return metadataUntil;
    }

    public Collection<Metadata> getRecentMetadata() {
	MetadataTemplate template = getMetadataTemplateAssociated();
	TreeSet<Metadata> metadataUntil = getMetadataUntil(now());
	final Comparator<Metadata> comparator;

	if (template != null) {
	    final List<MetadataKey> positionOrderedKeys = template.getPositionOrderedKeysList();
	    comparator = new Comparator<Metadata>() {

		@Override
		public int compare(Metadata o1, Metadata o2) {
		    final int indexOfo1 = positionOrderedKeys.indexOf(o1.getMetadataKey());
		    final int indexOfo2 = positionOrderedKeys.indexOf(o2.getMetadataKey());
		    return indexOfo1 < indexOfo2 ? -1 : (indexOfo1 == indexOfo2) ? 0 : 1; // int
											  // compare
		}

	    };
	} else {
	    comparator = Metadata.KEYVALUE_COMPARATOR;
	}

	return FluentIterable.from(Ordering.from(comparator).immutableSortedCopy(metadataUntil))
		.filter(new Predicate<Metadata>() {

		    @Override
		    public boolean apply(Metadata arg0) {
			return !arg0.getMetadataKey().isReserved();
		    }
		}).toImmutableSet();
    }

    public TreeSet<Metadata> getVersions() {
	final TreeSet<Metadata> metadataSet = new TreeSet<Metadata>(Metadata.TIMESTAMP_DESC_COMPARATOR);
	metadataSet.addAll(Sets.filter(getMetadataSet(), new Predicate<Metadata>() {

	    @Override
	    public boolean apply(Metadata arg0) {
		return MetadataKey.getNewDocumentVersionKey().equals(arg0.getMetadataKey());
	    }
	}));
	return metadataSet;
    }

    private void cleanup(TreeSet<Metadata> metadataSet) {
	HashMap<MetadataKey, Metadata> metadataMap = new HashMap<MetadataKey, Metadata>();
	for (Metadata metadataIter : metadataSet) {
	    final MetadataKey metadataKey = metadataIter.getMetadataKey();
	    final Metadata metadata = metadataMap.get(metadataKey);
	    if (metadata == null) {
		metadataMap.put(metadataKey, metadataIter);
	    }
	}
	metadataSet.clear();
	metadataSet.addAll(metadataMap.values());
    }

    private void addNewVersionMetadata() {
	User currentUser = UserView.getCurrentUser();
	addMetadata(MetadataKey.getNewDocumentVersionKey(), currentUser == null ? FileManagementSystem.getMessage("user.script")
		: currentUser.getShortPresentationName());
	setModifiedDateNow();
    }

    /**
     * replace metadata value. If metadata value is not present the metadata
     * value is added.
     * 
     * @param key
     * @param value
     *            if value is empty or null removeMetadata object
     */
    @Service
    public void replaceMetadata(MetadataKey key, String value) {
	boolean changed = false;
	for (Metadata metadata : getMetadataSet()) {
	    final MetadataKey metadataKey = metadata.getMetadataKey();
	    if (metadataKey.equals(key)) {
		if (value == null || value.isEmpty()) {
		    removeMetadata(metadata);
		} else {
		    metadata.setValue(value);
		}
		changed = true;
		break;
	    }
	}
	if (!changed) {
	    addMetadata(key, value);
	}
    }

    @Service
    public void clearAllMetadata() {
	for (Metadata md : getMetadata()) {
	    md.delete();
	}
    }

    public Metadata getMetadata(MetadataKey metadataKey) {
	if (metadataKey == null) {
	    return null;
	}
	for (Metadata metadata : getMetadata()) {
	    if (metadataKey.equals(metadata.getMetadataKey())) {
		return metadata;
	    }
	}
	return null;
    }

    /**
     * 
     * @param template
     *            the {@link MetadataTemplate} to associate with this Document
     */
    public void setMetadataTemplateAssociated(MetadataTemplate template) {
	addMetadata(MetadataKey.getTemplateKey(), template.getName());
    }

    public MetadataTemplate getMetadataTemplateAssociated() {
	String templateValue = (String) getMetadataValue(MetadataKey.getTemplateKey());
	if (!StringUtils.isBlank(templateValue)) {
	    return MetadataTemplate.getMetadataTemplate(templateValue);
	}
	return null;
    }

    private Object getMetadataValue(MetadataKey key) {
	Metadata metadata = getMetadata(key);
	if (metadata != null) {
	    return metadata.getValue();
	}
	return null;

    }

    public String getTypology() {
	final Metadata metadataTemplate = getMetadataRecentlyChanged(MetadataKey.getTemplateKey());
	return metadataTemplate != null ? (String) metadataTemplate.getValue() : "-";
    }

    public void setSaveAccessLog(final Boolean shouldSave) {
	addMetadata(MetadataKey.getSaveLogKey(), shouldSave.toString());
    }

    public boolean mustSaveAccessLog() {
	final Metadata metadata = getMetadata(MetadataKey.getSaveLogKey());
	return metadata != null ? metadata.getValue().equals(Boolean.TRUE.toString()) : false;
    }

}
