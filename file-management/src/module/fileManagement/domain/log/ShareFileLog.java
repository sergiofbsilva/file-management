package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class ShareFileLog extends ShareFileLog_Base {
    
    public  ShareFileLog() {
        super();
    }
    
    public ShareFileLog(User user, ContextPath contextPath, FileNode fileNode, DirNode targetSharedFolder) {
   	super();
   	super.init(user, contextPath, fileNode);
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
	return String.format("(%s) %s %s %s", getLogTime(), getUserName(), getOperationString(), getFileNode().getDisplayName()); 
    }
}
