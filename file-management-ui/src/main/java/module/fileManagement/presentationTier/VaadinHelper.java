package module.fileManagement.presentationTier;

import java.net.MalformedURLException;
import java.net.URL;

import module.fileManagement.domain.FileManagementSystem;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.fenixframework.DomainObject;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class VaadinHelper {

    public static void showException(Application app, DomainException e) {
        Window.Notification notif =
                new Window.Notification(FileManagementSystem.getMessage("label.operation.not.allowed"), e.getMessage(),
                        Window.Notification.TYPE_TRAY_NOTIFICATION);
        notif.setDelayMsec(-1);
        app.getMainWindow().showNotification(notif);
    }

    public static void showWarning(Application app, final String message) {
        VaadinHelper.show(app, FileManagementSystem.getMessage("label.operation.not.allowed"), message, Window.Notification.TYPE_WARNING_MESSAGE);
    }

    public static void show(Application app, final String caption, final String message, int type) {
        Window.Notification notif = new Window.Notification(caption, type);
        notif.setDescription(message);
        //notif.setDelayMsec(1000);
        notif.setPosition(Window.Notification.POSITION_CENTERED);
        app.getMainWindow().showNotification(notif);
    }

    public static URL getURL(final Application application, final DomainObject domainObject) {
        try {
            return new URL(VaadinHelper.getDownloadUrl(application, domainObject));
        } catch (MalformedURLException e) {
            FileManagementSystem.getLogger().error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public static String getDownloadUrl(final Application application, final DomainObject domainObject) {
        final URL url = application.getURL();
        final String path = url.getPath();
        return DownloadUtil.getDownloadUrl(url.getProtocol(), url.getHost(), url.getPort(), path.substring(0, path.length() - 7), domainObject);
    }

}
