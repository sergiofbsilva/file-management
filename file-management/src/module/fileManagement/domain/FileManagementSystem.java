package module.fileManagement.domain;

import myorg.domain.MyOrg;
import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.services.Service;

public class FileManagementSystem extends FileManagementSystem_Base {
    
    private static FileManagementSystem system;
    
    private FileManagementSystem() {
        super();
    }
    
    public static String getBundle() {
	return "resources.FileManagementResources";
    }

    public static String getMessage(final String key, String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }
    
    public MetadataKey getMetadataKey(String keyValue) {
	for(MetadataKey key : getMetadataKeys()) {
	    if (key.getKeyValue().equals(keyValue)) {
		return key;
	    }
	}
	return null;
    }
    
    
    public static FileManagementSystem getInstance() {
	if (system == null) {
	    system = initFileManagementSystem();
	}
	return system;
	
    }
    
    @Service
    private static FileManagementSystem initFileManagementSystem() {
	final FileManagementSystem fileManagementSystem = new FileManagementSystem();
	MyOrg.getInstance().setFileManagementSystem(fileManagementSystem);
	return fileManagementSystem;
    }
    
}
