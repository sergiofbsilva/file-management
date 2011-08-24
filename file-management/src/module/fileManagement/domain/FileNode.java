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
	setParent(dirNode);
	setDocument(new Document(file, fileName));
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
    public void trash() {
        super.trash();
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
    protected void setDisplayName(final String displayName) {
	final Document document = getDocument();
	document.setDisplayName(displayName);
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
    public int getFilesize() {
	return getDocument().getFilesize();
    }

}
