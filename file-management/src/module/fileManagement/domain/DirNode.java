package module.fileManagement.domain;

import myorg.domain.User;
import pt.ist.fenixWebFramework.services.Service;

public class DirNode extends DirNode_Base {
    
    public DirNode(final User user) {
        super();
        setUser(user);
        setName(user.getPresentationName());
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

}
