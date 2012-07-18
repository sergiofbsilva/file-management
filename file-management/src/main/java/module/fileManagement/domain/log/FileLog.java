package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import pt.ist.bennu.core.domain.User;

public abstract class FileLog extends FileLog_Base {
    
    public  FileLog() {
        super();
    }
    
    public void init(User user, ContextPath contextPath, FileNode fileNode) {
        super.init(user, contextPath);
        setFileNode(fileNode);
    }
    
    public FileLog(User user, ContextPath contextPath, FileNode fileNode) {
	super();
	init(user, contextPath,fileNode);
    }
    
    @Override
    public String toString() {
	return String.format("(%s) Em %s, %s %s %s", getLogTime() , getTargetDirName(), getUserName(), getOperationString(), getFileNode().getDisplayName());
    }
    
    
}
