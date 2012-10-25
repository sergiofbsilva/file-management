package module.fileManagement.domain.task;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.organization.domain.Party;
import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

public class MigratePredicateToRootField extends WriteCustomTask {

    @Override
    protected void doService() {
	for (final Party party : MyOrg.getInstance().getPartiesSet()) {
	    if (party.hasFileRepository()) {
		final DirNode rootDirNode = party.getFileRepository();
		rootDirNode.setRoot(Boolean.TRUE);
		rootDirNode.setFileManagementSystem(FileManagementSystem.getInstance());
	    }
	}
    }
}
