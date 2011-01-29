package module.fileManagement.domain;

import myorg.domain.User;
import pt.ist.fenixWebFramework.services.Service;

public class FileRepository {

    public static DirNode getOrCreateFileRepository(final User user) {
	return user.hasFileRepository() ? user.getFileRepository() : createFileRepository(user);
    }

    @Service
    private static DirNode createFileRepository(final User user) {
	return user.hasFileRepository() ? user.getFileRepository() : new DirNode(user);
    }

}
