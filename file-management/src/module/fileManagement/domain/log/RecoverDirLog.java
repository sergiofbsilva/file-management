package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import myorg.domain.User;

public class RecoverDirLog extends RecoverDirLog_Base {
    
    public  RecoverDirLog() {
        super();
    }
    
    public RecoverDirLog(User user, ContextPath contextPath, DirNode dirNode) {
	super();
	init(user,contextPath,dirNode);
    }

   
}
