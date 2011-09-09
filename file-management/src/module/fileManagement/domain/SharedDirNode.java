package module.fileManagement.domain;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import module.organization.domain.Party;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;



public class SharedDirNode extends SharedDirNode_Base {
    
    public SharedDirNode() {
        super();
    }
    
    public SharedDirNode(DirNode dirNode) {
        this();
        setNode(dirNode);
    }
    
    @Override
    public boolean isShared() {
	return true;
    }
    
    @Override
    public Long getQuota() {
	return getNode().getQuota();
    }
    
    @Override
    public Long getSize() {
	return getNode().getSize();
    }
    
    @Override
    public FileNode createFile(File file, String fileName, long filesize) {
        return getNode().createFile(file, fileName, filesize);
    }

    @Override
    public boolean isSharedFilesDirectory() {
	return getNode().isSharedFilesDirectory();
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
    public String getOwnerName() {
	return getNode().getOwnerName();
    }

    @Override
    public int getCountFiles() {
	return getNode().getCountFiles();
    }

    @Override
    public String getDisplayName() {
	return getNode().getDisplayName();
    }

    @Override
    public PersistentGroup getReadGroup() {
	return getNode() != null ? getNode().getReadGroup() : super.getReadGroup();
    }

    @Override
    public PersistentGroup getWriteGroup() {
	return getNode() != null ? getNode().getWriteGroup() : super.getWriteGroup();
    }

    @Override
    public String getName() {
	return getNode().getName();
    }
    
    @Override
    public boolean hasAvailableQuota(long length) {
	return getNode().hasAvailableQuota(length);
    }
    
    @Override
    public boolean hasAvailableQuota(long length, FileNode fileNode) {
        return getNode().hasAvailableQuota(length, fileNode);
    }
    
    @Override
    public String getPresentationFilesize() {
	return getNode().getPresentationFilesize();
    }
    
    @Override
    public String getPresentationTotalUsedSpace() {
	return getNode().getPresentationTotalUsedSpace();
    }
    
    @Override
    public String getPercentOfTotalUsedSpace() {
	return getNode().getPercentOfTotalUsedSpace();
    }

    @Override
    public int getFilesize() {
	return getNode().getFilesize();
    }

    @Override
    public Party getOwner() {
	return getNode().getOwner();
    }

    @Override
    public boolean hasReadGroup() {
	return getNode() == null ? super.hasReadGroup() : getNode().hasReadGroup();
    }

    @Override
    public int getChildCount() {
	return getNode().getChildCount();
    }

    @Override
    public Set<AbstractFileNode> getChildSet() {
	return getNode().getChildSet();
    }

    @Override
    public List<AbstractFileNode> getChild() {
	return getNode().getChild();
    }

    @Override
    public Iterator<AbstractFileNode> getChildIterator() {
	return getNode().getChildIterator();
    }
    
    @Service
    public void deleteLink() {
	super.setParent(null);
	super.setNode(null);
	deleteDomainObject();
    }
}
