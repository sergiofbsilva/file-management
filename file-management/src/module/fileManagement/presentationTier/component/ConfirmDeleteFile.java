package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.FileNode;
import myorg.util.BundleUtil;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class ConfirmDeleteFile extends Window {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    public ConfirmDeleteFile(final FileNode fileNode, final Table fileTable) {
	super(getMessage("label.file.delete"));
	setWidth(50, UNITS_PERCENTAGE);

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        final Window window = this;

        final Label label = new Label(getMessage("label.file.delete.confirm"));
        layout.addComponent(label);

        final HorizontalLayout horizontalLayout = new HorizontalLayout();
        layout.addComponent(horizontalLayout);
        layout.setComponentAlignment(horizontalLayout, Alignment.BOTTOM_RIGHT);

        final Button delete = new Button(getMessage("label.file.delete"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
        	fileTable.removeItem(fileNode.getExternalId());
        	fileNode.deleteService();
        	(getParent()).removeWindow(window);
            }
        });
        horizontalLayout.addComponent(delete);
        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                (getParent()).removeWindow(window);
            }
        });
        horizontalLayout.addComponent(close);
    }

}
