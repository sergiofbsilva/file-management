package module.fileManagement.domain;

import java.io.File;

import module.organization.domain.Unit;
import myorg.domain.User;
import pt.ist.fenixWebFramework.services.Service;

public class DirNode extends DirNode_Base {
    
    public DirNode(final User user) {
        super();
        setUser(user);
        setName(user.getPresentationName());
    }

    public DirNode(final Unit unit) {
        super();
        setUnit(unit);
        setName(unit.getPresentationName());
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
	    new DirNode(dirNode, "Contracto");
	    new DirNode(dirNode, "IRS");
	    new DirNode(dirNode, "Vencimento");
	}
    }

    @Override
    public boolean isDir() {
        return true;
    }

    @Service
    public FileNode createFile(final File file, final String fileName) {
	return new FileNode(this, file, fileName);
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

}
