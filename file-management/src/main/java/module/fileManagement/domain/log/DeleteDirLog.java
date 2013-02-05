package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import pt.ist.bennu.core.domain.User;

public class DeleteDirLog extends DeleteDirLog_Base {

    public DeleteDirLog() {
        super();
    }

    public DeleteDirLog(User user, ContextPath contextPath, DirNode dirNode) {
        super();
        init(user, contextPath, dirNode);
    }

}
