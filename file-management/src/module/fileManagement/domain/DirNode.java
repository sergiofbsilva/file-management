package module.fileManagement.domain;

import myorg.domain.User;

public class DirNode extends DirNode_Base {
    
    public DirNode(final User user) {
        super();
        setUser(user);
        setName(user.getPresentationName());
    }
    
}
