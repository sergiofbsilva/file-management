package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.FileNode;
import myorg.util.BundleUtil;
import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class FileNodeDetails extends Window {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    public FileNodeDetails(final FileNode fileNode) {
	super(getMessage("label.file.details"));
	setWidth(65, UNITS_PERCENTAGE);

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        final Window window = this;

        final GenericFile file = fileNode.getFile();
        
        final StringBuilder builder = new StringBuilder();
        builder.append(getMessage("label.file.displayName"));
        builder.append(": ");
        builder.append(file.getDisplayName());
        builder.append("<br/>");
        builder.append(getMessage("label.file.filename"));
        builder.append(": ");
        builder.append(file.getFilename());
        builder.append("<br/>");
        builder.append(getMessage("label.file.contentType"));
        builder.append(": ");
        builder.append(file.getContentType());
        builder.append("<br/>");
        builder.append(getMessage("label.file.size"));
        builder.append(": ");
        builder.append(file.getContent().length);
        builder.append("<br/>");
        builder.append(getMessage("label.file.contentKey"));
        builder.append(": ");
        builder.append(file.getContentKey());
        builder.append("<br/>");
        builder.append(getMessage("label.file.storage"));
        builder.append(": ");
        builder.append(file.getStorage().getName());
        builder.append("<br/>");
        builder.append(getMessage("label.file.group.read"));
        builder.append(": ");
        builder.append(fileNode.getReadGroup().getName());
        builder.append("<br/>");
        builder.append(getMessage("label.file.group.write"));
        builder.append(": ");
        builder.append(fileNode.getWriteGroup().getName());
        builder.append("<br/>");

        final Label label = new Label(builder.toString(), Label.CONTENT_XHTML);
        layout.addComponent(label);

        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                (getParent()).removeWindow(window);
            }
        });
        layout.addComponent(close);
        layout.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);
    }

}
