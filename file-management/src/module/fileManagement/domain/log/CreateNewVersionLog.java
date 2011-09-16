package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import myorg.domain.User;

public class CreateNewVersionLog extends CreateNewVersionLog_Base {
    
    public  CreateNewVersionLog() {
        super();
    }
    
    public CreateNewVersionLog(User user, ContextPath contextPath, FileNode fileNode) {
	super();
	init(user,contextPath,fileNode);
	setVersionNumber(fileNode.getDocument().getVersionNumber());
    }
    
    @Override
    public String getOperationString(String... args) {
        return super.getOperationString(getVersionNumber().toString());
    }
}
