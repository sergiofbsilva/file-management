package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class UnshareDirLog extends UnshareDirLog_Base {
    
    public  UnshareDirLog() {
        super();
    }
    
    public UnshareDirLog(User user, ContextPath contextPath, DirNode dirNode, DirNode targetSharedFolder) {
	super();
	super.init(user, contextPath, dirNode);
	setTargetDirNode(targetSharedFolder);
    }
    
    @Override
    public String toString() {
	final DirNode sharedFolder = UserView.getCurrentUser().getFileRepository().getSharedFolder();
	final String sharedWith = sharedFolder == getTargetDirNode() ? "consigo" : " com " + getTargetDirNode().getOwner().getPresentationName();
	return String.format("(%s) %s deixou de partilhar %s a pasta %s", getLogTime(), getUserName(), sharedWith, getDirNode().getDisplayName()); 
    }
    
}
