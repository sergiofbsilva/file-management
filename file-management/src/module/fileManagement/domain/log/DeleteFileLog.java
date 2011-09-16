package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import myorg.domain.User;

public class DeleteFileLog extends DeleteFileLog_Base {
    
    public  DeleteFileLog() {
        super();
    }
    
    public DeleteFileLog(User user, ContextPath contextPath, FileNode fileNode) {
   	super();
   	init(user,contextPath,fileNode);
       }
    
}
