package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class CreateShareFileLinkLog extends CreateShareFileLinkLog_Base {
    
    public  CreateShareFileLinkLog() {
        super();
    }
    
    public CreateShareFileLinkLog(User user, ContextPath contextPath, FileNode fileNode, DirNode targetSharedFolder) {
	super.init(user, contextPath, fileNode);
	setTargetDirNode(targetSharedFolder);
    }
    
    // <user> partilhou com <shared-user> o ficheiro <filename>
    
    @Override
    public String toString() {
	final DirNode sharedFolder = UserView.getCurrentUser().getFileRepository().getSharedFolder();
	final String sharedWith = sharedFolder == getTargetDirNode() ? "consigo" : "com " + getTargetDirNode().getOwner().getPresentationName();
	return String.format("(%s) %s partilhou %s o ficheiro %s", getLogTime(), getUserName(), sharedWith, getFileNode().getDisplayName()); 
    }
    
}
