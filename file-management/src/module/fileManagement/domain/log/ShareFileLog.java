package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import myorg.domain.User;

public class ShareFileLog extends ShareFileLog_Base {
    
    public  ShareFileLog() {
        super();
    }
    
    public ShareFileLog(User user, ContextPath contextPath, FileNode fileNode) {
   	super();
   	init(user, contextPath, fileNode);
    }
    
    @Override
    public String toString() {
        String toString = super.toString();
        final String sharedUser = getFileNode().getOwner().getPresentationName();
        return toString.concat(" com " + sharedUser); 
    }
}
