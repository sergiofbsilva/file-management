package module.fileManagement.domain;

import java.io.File;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Unit;
import module.organization.domain.groups.UnitGroup;
import module.organizationIst.domain.IstAccountabilityType;
import myorg.domain.User;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;
import myorg.domain.groups.UserGroup;
import pt.ist.fenixWebFramework.services.Service;

public class DirNode extends DirNode_Base {
    
    public DirNode(final User user) {
        super();
        setUser(user);
        setName(user.getPresentationName());
        final PersistentGroup group = SingleUserGroup.getOrCreateGroup(user);
        setReadGroup(group);
        setWriteGroup(group);
    }

    public DirNode(final Unit unit) {
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
    }

    public DirNode(final DirNode dirNode, final String name) {
	super();
	setParent(dirNode);
	setName(name);
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

    public void delete() {
	removeUser();
	removeUnit();
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
	    if (node.isFile()) {
		result++;
	    } else {
		result += ((DirNode) node).countFiles();
	    }
	}
	return result;
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
	return new FileNode(this, file, fileName);
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	return node.isFile() ? -1 : super.compareTo(node);
    }

}
