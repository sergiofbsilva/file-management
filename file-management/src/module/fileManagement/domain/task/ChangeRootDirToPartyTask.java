package module.fileManagement.domain.task;

import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.scheduler.WriteCustomTask;

public class ChangeRootDirToPartyTask extends WriteCustomTask {

    @Override
    protected void doService() {
	for (User user : MyOrg.getInstance().getUser()) {
	    if (user.hasFileRepository()) {
		user.getPerson().setFileRepository(user.getFileRepository());
		out.println(user.getPresentationName());
	    }
	}
    }
}
