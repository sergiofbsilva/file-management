package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.domain.User;

public class CreateDirLog extends CreateDirLog_Base {
    
    public  CreateDirLog() {
        super();
    }
    
    public CreateDirLog(User user, ContextPath contextPath, DirNode dirNode) {
	super();
	init(user,contextPath,dirNode);
    }

}
