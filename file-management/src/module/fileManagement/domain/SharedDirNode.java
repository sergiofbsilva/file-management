package module.fileManagement.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import module.organization.domain.Party;
import myorg.domain.groups.PersistentGroup;



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
    public int countFiles() {
	return getNode().countFiles();
    }

    @Override
    public int getCountFiles() {
	return getNode().getCountFiles();
    }

    @Override
    public long countFilesSize() {
	return getNode().countFilesSize();
    }

    @Override
    public String getDisplayName() {
	return getNode().getDisplayName();
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
    public String getName() {
	return getNode().getName();
    }

    @Override
    public String getPresentationFilesize() {
	return getNode().getPresentationFilesize();
    }

    @Override
    public int getFilesize() {
	return getNode().getFilesize();
    }

    @Override
    public String getAbsolutePath() {
	return getNode().getAbsolutePath();
    }

    @Override
    public List<DirNode> getAllDirs() {
	return getNode().getAllDirs();
    }

    @Override
    public Party getOwner() {
	return getNode().getOwner();
    }

    @Override
    public boolean hasReadGroup() {
	return getNode().hasReadGroup();
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

}
