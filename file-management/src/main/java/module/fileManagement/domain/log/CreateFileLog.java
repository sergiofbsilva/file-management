package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import pt.ist.bennu.core.domain.User;

public class CreateFileLog extends CreateFileLog_Base {
    
    public  CreateFileLog() {
        super();
    }
    
    public CreateFileLog(User user, ContextPath contextPath, FileNode fileNode) {
	super();
	init(user,contextPath,fileNode);
    }
    
}
