package module.fileManagement.domain;

import java.util.ResourceBundle;

import myorg.domain.MyOrg;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.VaadinFrameworkLogger;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class FileManagementSystem extends FileManagementSystem_Base {

    private static FileManagementSystem system;
    public static final String BUNDLE = "resources.FileManagementResources";

    private static final Logger logger = Logger.getLogger(FileManagementSystem.class);

    static {
	VaadinFrameworkLogger.getLogger().setLevel(Level.ALL);
    }

    private FileManagementSystem() {
	super();
    }

    public static String getMessage(final String key, String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(BUNDLE, key, args);
    }

    public MetadataKey getMetadataKey(String keyValue) {
	for (MetadataKey key : getMetadataKeys()) {
	    if (key.getKeyValue().equals(keyValue)) {
		return key;
	    }
	}
	return null;
    }

    public static FileManagementSystem getInstance() {
	if (system == null) {
	    system = getOrCreateInstance();
	}
	return system;

    }

    @Service
    private static FileManagementSystem getOrCreateInstance() {
	final MyOrg myorg = MyOrg.getInstance();
	if (!myorg.hasFileManagementSystem()) {
	    final FileManagementSystem fileManagementSystem = new FileManagementSystem();
	    myorg.setFileManagementSystem(fileManagementSystem);

	}
	return myorg.getFileManagementSystem();
    }

    public static ResourceBundle getBundle() {
	return ResourceBundle.getBundle(BUNDLE);
    }

    public static Logger getLogger() {
	return logger;
    }

    // public static void goTo(Class clazz) {
    //
    // for(Annotation anot : clazz.getAnnotations()) {
    // if (anot.annotationType().equals(EmbeddedComponent.class)) {
    // EmbeddedComponent embedded = (EmbeddedComponent) anot;
    //
    // }
    // }
    // }

    public static void showException(Application app, DomainException e) {
	Window.Notification notif = new Window.Notification("Operação não permitida", e.getMessage(),
		Window.Notification.TYPE_TRAY_NOTIFICATION);
	notif.setDelayMsec(-1);
	app.getMainWindow().showNotification(notif);
    }

}
