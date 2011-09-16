package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import myorg.domain.User;

public class RecoverFileLog extends RecoverFileLog_Base {
    
    public  RecoverFileLog() {
        super();
    }
    
    public RecoverFileLog(User user, ContextPath contextPath, FileNode fileNode) {
   	super();
   	init(user,contextPath,fileNode);
       }
    
}
