package module.fileManagement.domain;

import myorg.util.BundleUtil;

public class DocumentSystem {

    public static String getBundle() {
	return "resources.FileManagementResources";
    }

    public static String getMessage(final String key, String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

}
