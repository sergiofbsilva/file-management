package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class ShareDirLog extends ShareDirLog_Base {
    
    public  ShareDirLog() {
        super();
    }
    
    public ShareDirLog(User user, ContextPath contextPath, DirNode dirNode, DirNode targetSharedFolder) {
	super();
	super.init(user, contextPath, dirNode);
	setTargetDirNode(targetSharedFolder);
    }
    
    @Override
    public String getOperationString(String... args) {
	final DirNode sharedFolder = UserView.getCurrentUser().getFileRepository().getSharedFolder();
	final String sharedWith = sharedFolder == getTargetDirNode() ? "consigo" : " com " + getTargetDirNode().getOwner().getPresentationName();
        return super.getOperationString(sharedWith);
    }
    
    @Override
    public String toString() {
	return String.format("(%s) %s %s %s", getLogTime(), getUserName(), getOperationString(), getDirNode().getDisplayName()); 
    }
    
}
