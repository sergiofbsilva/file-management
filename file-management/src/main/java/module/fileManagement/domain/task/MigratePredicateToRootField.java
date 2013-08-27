package module.fileManagement.domain.task;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.organization.domain.Party;
import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.scheduler.CronTask;

public class MigratePredicateToRootField extends CronTask {

    @Override
    public void runTask() {
        for (final Party party : MyOrg.getInstance().getPartiesSet()) {
            if (party.getFileRepository() != null) {
                final DirNode rootDirNode = party.getFileRepository();
                rootDirNode.setRoot(Boolean.TRUE);
                rootDirNode.setFileManagementSystem(FileManagementSystem.getInstance());
            }
        }
    }
}
