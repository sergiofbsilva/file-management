package module.fileManagement.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import module.organization.domain.Party;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;


public class SharedFileNode extends SharedFileNode_Base {
    
    public SharedFileNode(FileNode fileNode) {
	super();
	setNode(fileNode);
    }
    
    @Override
    public FileNode getNode() {
        return (FileNode) super.getNode();
    }
    
    @Override
    public boolean isShared() {
	return true;
    }
    
    
    @Override
    public void deleteService() {
	getNode().deleteService();
    }

    @Override
    public PersistentGroup getReadGroup() {
	return getNode().getReadGroup();
    }

    @Override
    public PersistentGroup getWriteGroup() {
	return getNode().getWriteGroup();
    }

    @Override
    public String getDisplayName() {
	return getNode().getDisplayName();
    }

    @Override
    public boolean isAccessible() {
	return getNode().isAccessible();
    }

    @Override
    public int compareTo(AbstractFileNode node) {
	return getNode().compareTo(node);
    }

    @Override
    public String getVisibility() {
	return getNode().getVisibility();
    }

    @Override
    public String getPresentationFilesize() {
	return getNode().getPresentationFilesize();
    }

    @Override
    public VisibilityList getVisibilityGroups() {
	return getNode().getVisibilityGroups();
    }

    @Override
    public int getFilesize() {
	return getNode().getFilesize();
    }

    @Override
    public void setVisibility(VisibilityList visibilityList) {
	getNode().setVisibility(visibilityList);
    }

    @Override
    public int getSharedFileNodesCount() {
	return getNode().getSharedFileNodesCount();
    }

    @Override
    public boolean hasAnySharedFileNodes() {
	return getNode() == null ? super.hasAnySharedFileNodes() : getNode().hasAnySharedFileNodes();
    }

    @Override
    public boolean hasSharedFileNodes(SharedFileNode sharedFileNodes) {
	return getNode().hasSharedFileNodes(sharedFileNodes);
    }

    @Override
    public Set<SharedFileNode> getSharedFileNodesSet() {
	return getNode().getSharedFileNodesSet();
    }

        @Override
    public void addSharedFileNodes(SharedFileNode sharedFileNodes) {
	getNode().addSharedFileNodes(sharedFileNodes);
    }

    @Override
    public void share(User user, VisibilityGroup group, boolean createLink) {
	getNode().share(user, group, createLink);
    }
    
    
    @Override
    public void removeSharedFileNodes(SharedFileNode sharedFileNodes) {
	getNode().removeSharedFileNodes(sharedFileNodes);
    }

    
    @Override
    public List<SharedFileNode> getSharedFileNodes() {
	return getNode().getSharedFileNodes();
    }

    @Override
    public Iterator<SharedFileNode> getSharedFileNodesIterator() {
	return getNode().getSharedFileNodesIterator();
    }

    @Override
    public Document getDocument() {
	return getNode().getDocument();
    }

    @Override
    public Party getOwner() {
	return getNode().getOwner();
    }

    @Override
    public String getOwnerName() {
	return getNode().getOwnerName();
    }

    @Override
    public void setDocument(Document document) {
	getNode().setDocument(document);
    }

    @Override
    public boolean hasDocument() {
	return getNode() == null ? super.hasDocument() : getNode().hasDocument();
    }

    @Override
    public boolean isDeleted() {
	return getNode().isDeleted();
    }

    @Override
    public boolean isReadGroupMember() {
	return getNode().isReadGroupMember();
    }

    @Override
    public boolean isWriteGroupMember() {
	return getNode().isWriteGroupMember();
    }

    @Override
    public void removeDocument() {
	getNode().removeDocument();
    }

    @Override
    public String toString() {
	return getNode().toString();
    }

    @Override
    public void trash() {
	getNode().trash();
    }
    
    @Override
    protected void setReadGroup(PersistentGroup persistentGroup) {
        getNode().setReadGroup(persistentGroup);
    }
    
    @Override
    protected void setWriteGroup(PersistentGroup persistentGroup) {
	getNode().setWriteGroup(persistentGroup);
    }
    
    //TODO: check if this makes sense, never moves target file to this user target dir
    @Override
    public void setParent(DirNode parent) {
        super.setParent(parent);
    }
    
    @Override
    public DirNode getParent() {
	return getNode().getParent();
    }
    
    @Override
    public boolean hasParent() {
	return getNode().hasParent();
    }
    
    @Override
    public void removeParent() {
       getNode().removeParent();
    }
    
    @Service
    public void deleteLink() {
	super.setParent(null);
	super.setNode(null);
	deleteDomainObject();
    }
    
    @Override
    protected boolean checkDisconnected() {
        return super.getParent() == null  && super.getNode() == null;
    }
    
    
}
