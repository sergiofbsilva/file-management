package module.fileManagement.domain.task;

import java.util.HashSet;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.VersionedFile;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.scheduler.WriteCustomTask;

public class InitializeQuotaAndSizeTask extends WriteCustomTask {
    
    public void executeTask() {
	HashSet<User> usersWithRepository = new HashSet<User>();
	for(User user : MyOrg.getInstance().getUser()) {
		if (user.hasFileRepository()) {
		    final DirNode repository = user.getFileRepository();
		    repository.setQuota(DirNode.USER_REPOSITORY_QUOTA);
		    if (!user.hasTrash()) {
			repository.createTrashFolder(user);
		    }
		    //creates if it doesnt exists.
		    repository.getSharedFolder();
		    out.printf("User %s , trash : %s , shared : %s, quota : %s\n", user.getUsername(), user.getTrash().getDisplayName(), repository.getSharedFolder().getDisplayName(), repository.getQuota());
		    usersWithRepository.add(user);
		}
		
	}
	
	out.printf("Users processed : %s\n", usersWithRepository.size());
	
	for(User user : usersWithRepository) {
	    final long repositorySize = updateFileRepositorySize(user.getFileRepository());
	    out.printf("User : %s Size: %s\n", user.getUsername() , VersionedFile.FILE_SIZE_UNIT.prettyPrint(repositorySize));
	}
    }

    private long updateFileRepositorySize(DirNode repository) {
	long size = 0;
	for(AbstractFileNode node : repository.getChild()) {
	    if (node instanceof DirNode) {
		size += updateFileRepositorySize((DirNode) node); 
	    }
	    if (node instanceof FileNode) {
//		size += node.getFilesize();
		size += 1024;
	    }
	}
	repository.setSize(size);
	return size;
    }

    @Override
    protected void doService() {
	executeTask();
    }
    
}
