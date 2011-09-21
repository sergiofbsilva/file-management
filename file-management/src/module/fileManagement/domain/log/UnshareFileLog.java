package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class UnshareFileLog extends UnshareFileLog_Base {
    
    public  UnshareFileLog() {
        super();
    }
    
    public UnshareFileLog(User user, FileNode fileNode) {
	super();
	final DirNode linkSharedFolder = fileNode.getParent().getSharedFolder();
	super.init(user, new ContextPath(linkSharedFolder), fileNode);
	setTargetDirNode(linkSharedFolder);
    }
    
    @Override
    public String toString() {
	final DirNode sharedFolder = UserView.getCurrentUser().getFileRepository().getSharedFolder();
	final String sharedWith = sharedFolder == getTargetDirNode() ? "consigo" : " com " + getTargetDirNode().getOwner().getPresentationName();
	return String.format("(%s) %s deixou de partilhar %s o ficheiro %s", getLogTime(), getUserName(), sharedWith, getFileNode().getDisplayName()); 
    }
    
}
