package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.domain.User;

public class ShareDirLog extends ShareDirLog_Base {
    
    public  ShareDirLog() {
        super();
    }
    
    public ShareDirLog(User user, ContextPath contextPath, DirNode dirNode) {
	super();
	init(user,contextPath,dirNode);
    }
    
    @Override
    public String toString() {
        String toString = super.toString();
        final String sharedUser = getDirNode().getOwner().getPresentationName();
        return toString.concat(" com " + sharedUser); 
    }

}
