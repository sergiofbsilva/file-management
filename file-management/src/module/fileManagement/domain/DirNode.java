package module.fileManagement.domain;

import myorg.domain.User;

public class DirNode extends DirNode_Base {
    
    public DirNode(final User user) {
        super();
        setUser(user);
        setName(user.getPresentationName());
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
}
