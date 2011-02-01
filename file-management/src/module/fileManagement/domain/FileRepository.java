package module.fileManagement.domain;

import module.organization.domain.Unit;
import myorg.domain.User;
import pt.ist.fenixWebFramework.services.Service;

public class FileRepository {

    public static DirNode getOrCreateFileRepository(final User user) {
	return user.hasFileRepository() ? user.getFileRepository() : createFileRepository(user);
    }

    public static DirNode getOrCreateFileRepository(final Unit unit) {
	return unit.hasFileRepository() ? unit.getFileRepository() : createFileRepository(unit);
    }

    @Service
    private static DirNode createFileRepository(final Unit unit) {
	return unit.hasFileRepository() ? unit.getFileRepository() : new DirNode(unit);
    }

    @Service
    private static DirNode createFileRepository(final User user) {
	return user.hasFileRepository() ? user.getFileRepository() : new DirNode(user);
    }

}
