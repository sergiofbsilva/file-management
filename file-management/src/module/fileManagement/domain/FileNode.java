package module.fileManagement.domain;

import java.io.File;

import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

public class FileNode extends FileNode_Base {

    public FileNode(final DirNode dirNode, final File file, final String fileName) {
	super();
	if (dirNode == null) {
	    throw new DomainException("must.specify.a.dir.node.when.creating.a.file.node");
	}
	setDocument(new Document(file, fileName));
	setParent(dirNode);
    }
    
    public FileNode() {
	super();
    }
    
    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    @Service
    public void delete() {
	final Document document = getDocument();
	document.delete();
	getParent().removeChild(this);
	deleteDomainObject();
    }
    
    void deleteFromDocument() {
	removeDocument();
        super.delete();
    }

    @Service
    public void deleteService() {
	delete();
    }
    
    
    @Override
    public PersistentGroup getReadGroup() {
	final PersistentGroup group = getDocument().getReadGroup();
	return group == null && hasParent() ? getParent().getReadGroup() : group;
    }

    @Override
    public PersistentGroup getWriteGroup() {
	final PersistentGroup group = getDocument().getWriteGroup();
	return group == null && hasParent() ? getParent().getWriteGroup() : group;
    }

    @Override
    public String getDisplayName() {
	final Document document = getDocument();
	return document.getDisplayName();
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	return node.isDir() ? 1 : super.compareTo(node);
    }

    @Override
    protected void setReadGroup(final PersistentGroup persistentGroup) {
	final Document document = getDocument();
	document.setReadGroup(persistentGroup);
    }

    @Override
    protected void setWriteGroup(PersistentGroup persistentGroup) {
	final Document document = getDocument();
	document.setWriteGroup(persistentGroup);
    }

    @Override
    public String getPresentationFilesize() {
	final Document document = getDocument();
	return document.getPresentationFilesize();
    }
    
    @Override
    public long getFilesize() {
	return Long.valueOf(getDocument().getFilesize());
    }
    
    @Override
    public void unshare(VisibilityGroup group) {
        super.unshare(group);
        for(SharedFileNode sharedNode : getSharedFileNodes()) {
            sharedNode.deleteLink(new ContextPath(getParent()));
        }
    }
}
