package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class CreateShareDirLinkLog extends CreateShareDirLinkLog_Base {
    
    public  CreateShareDirLinkLog() {
        super();
    }
    
    public CreateShareDirLinkLog(User user, ContextPath contextPath, DirNode dirNode, DirNode targetSharedFolder) {
	super.init(user, contextPath, dirNode);
	setTargetDirNode(targetSharedFolder);
    }
    
    @Override
    public String toString() {
	final DirNode sharedFolder = UserView.getCurrentUser().getFileRepository().getSharedFolder();
	final String sharedWith = sharedFolder == getTargetDirNode() ? "consigo" : " com " + getTargetDirNode().getOwner().getPresentationName();
	return String.format("(%s) %s partilhou %s a pasta %s", getLogTime(), getUserName(), sharedWith, getDirNode().getDisplayName()); 
    }
}
