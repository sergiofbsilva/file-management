package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileNode;
import myorg.domain.User;

public class AccessFileLog extends AccessFileLog_Base {

    public AccessFileLog() {
	super();
    }

    public AccessFileLog(User user, ContextPath contextPath, FileNode fileNode) {
	super();
	init(user, contextPath, fileNode);
    }

}
