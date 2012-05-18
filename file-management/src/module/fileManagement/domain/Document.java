package module.fileManagement.domain;

import static module.fileManagement.tools.StringUtils.matches;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import myorg.applicationTier.Authenticate.UserView;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;

public class Document extends Document_Base {

    public static String DOCUMENT_NEW_VERSION_KEY = "document.new.version.key.value";

    public Document() {
	super();
	addNewVersionMetadata();
    }

    public Document(final File file, final String fileName) {
	this();
	setLastVersionedFile(new VersionedFile(file, fileName));
	addMetadata(MetadataKey.FILENAME_KEY_VALUE, fileName);
    }

    public Document(final String displayName, final String fileName, byte[] content) {
	this();
	setLastVersionedFile(new VersionedFile(displayName, fileName, content));
	addMetadata(MetadataKey.FILENAME_KEY_VALUE, fileName);
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

    public boolean search(String searchText) {
	return /* matches(getDisplayName(), searchText) || */searchMetadata(searchText);
    }

    private boolean searchMetadata(String searchText) {
	for (Metadata metadata : getMetadata()) {
	    final String keyValue = metadata.getKeyValue();
	    final String value = metadata.getValue();
	    if (matches(keyValue, searchText) || matches(value, searchText)) {
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
	final VersionedFile versionedFile = getLastVersionedFile();
	final VersionedFile newVersion = new VersionedFile(file, filename);
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
	addMetadata(new Metadata(key, value));
    }

    @Service
    public void addMetadata(Map<String, String> keyValueMap) {
	for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
	    addMetadata(entry.getKey(), entry.getValue());
	}
    }

    @Service
    public void addMetadata(MetadataKey key, String value) {
	addMetadata(new Metadata(key, value));
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
	final TreeSet<Metadata> metadataUntil = getMetadataUntil(now());
	Iterator<Metadata> iter = metadataUntil.iterator();
	final Set<String> keysToFilter = new HashSet<String>();
	keysToFilter.add(DOCUMENT_NEW_VERSION_KEY);
	keysToFilter.add(MetadataKey.FILENAME_KEY_VALUE);
	while (iter.hasNext()) {
	    Metadata metadata = iter.next();
	    if (keysToFilter.contains(metadata.getKeyValue())) {
		iter.remove();
	    }
	}
	final TreeSet<Metadata> metadataByKeyValue = new TreeSet<Metadata>(Metadata.KEYVALUE_COMPARATOR);
	metadataByKeyValue.addAll(metadataUntil);
	return metadataByKeyValue;
    }

    public TreeSet<Metadata> getVersions() {
	final TreeSet<Metadata> metadataSet = new TreeSet<Metadata>(Metadata.TIMESTAMP_DESC_COMPARATOR);
	final TreeSet<Metadata> resultingSet = new TreeSet<Metadata>(Metadata.TIMESTAMP_DESC_COMPARATOR);
	metadataSet.addAll(getMetadata());
	for (Metadata metadata : metadataSet) {
	    if (DOCUMENT_NEW_VERSION_KEY.equals(metadata.getKeyValue())) {
		resultingSet.add(metadata);
	    }
	}
	return resultingSet;
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
	addMetadata(DOCUMENT_NEW_VERSION_KEY, UserView.getCurrentUser().getShortPresentationName());
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
	addMetadata(MetadataKey.TEMPLATE_KEY_VALUE, template.getName());
    }

    public MetadataTemplate getMetadataTemplateAssociated()
    {
	String templateValue = getMetadataValue(MetadataKey.TEMPLATE_KEY_VALUE);
	if (!StringUtils.isBlank(templateValue)) {
	    return MetadataTemplate.getMetadataTemplate(templateValue);
	}
	return null;
    }

    private String getMetadataValue(String keyValue) {
	Metadata metadata = getMetadata(MetadataKey.getInstance(keyValue));
	if (metadata != null) {
	    return metadata.getValue();
	}
	return null;

    }

    public String getTypology() {
	final Metadata metadataTemplate = getMetadataRecentlyChanged(MetadataKey.getTemplateKey());
	return metadataTemplate != null ? metadataTemplate.getValue() : "-";
    }

    public void setSaveAccessLog(final Boolean shouldSave) {
	addMetadata(getSaveAccessLogKey(), shouldSave.toString());
    }

    private MetadataKey getSaveAccessLogKey() {
	return MetadataKey.getOrCreateInstance(MetadataKey.SAVE_ACCESS_LOG, Boolean.TRUE);
    }

    public boolean mustSaveAccessLog() {
	final Metadata metadata = getMetadata(getSaveAccessLogKey());
	return metadata != null ? metadata.getValue().equals(Boolean.TRUE.toString()) : false;
    }
}
