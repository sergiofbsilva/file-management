package pt.ist.file.rest.utils.domain;

import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.fenixWebFramework.services.Service;

public class FileRestUtilsSystem extends FileRestUtilsSystem_Base {

	private FileRestUtilsSystem() {
		super();
	}

	public static FileRestUtilsSystem getInstance() {
		if (!MyOrg.getInstance().hasFileRestUtilsSystem()) {
			init();
		}
		return MyOrg.getInstance().getFileRestUtilsSystem();
	}

	@Service
	private static void init() {
		MyOrg.getInstance().setFileRestUtilsSystem(new FileRestUtilsSystem());
	}

}
