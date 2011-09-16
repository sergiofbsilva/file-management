package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.domain.User;

public abstract class DirLog extends DirLog_Base {
    
    public  DirLog() {
        super();
    }
    
    public void init(User user, ContextPath contextPath, DirNode dirNode) {
        super.init(user, contextPath);
        setDirNode(dirNode);
    }
    
    public DirLog(User user, ContextPath contextPath, DirNode dirNode) {
	super();
	init(user, contextPath,dirNode);
    }
    
    @Override
    public String toString() {
	final String contextDirName = getTargetDirNode().getDisplayName();
	final String userName = getUser().getPresentationName();
	final String dirName = getDirNode().getDisplayName();
	return String.format("(%s) Em %s, %s %s %s", getLogTime(), contextDirName, userName, getOperationString(), dirName);
    }
    
    
}
