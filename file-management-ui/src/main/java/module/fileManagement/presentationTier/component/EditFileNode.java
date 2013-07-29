package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.FileNode;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EditFileNode extends Window {

    protected static String getBundle() {
        return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
        return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    public EditFileNode(final FileNode fileNode, final Table fileTable) {
        super(getMessage("label.file.edit"));
        setWidth(50, UNITS_PERCENTAGE);

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        final Window window = this;

        final GenericFile file = null; //fileNode.getFile();

        final TextField displayNameField = new TextField(getMessage("label.file.displayName"));
        displayNameField.setValue(file.getDisplayName());
        final TextField fileNameField = new TextField(getMessage("label.file.filename"));
        fileNameField.setValue(file.getFilename());

        layout.addComponent(displayNameField);
        layout.addComponent(fileNameField);

        final HorizontalLayout horizontalLayout = new HorizontalLayout();
        layout.addComponent(horizontalLayout);
        layout.setComponentAlignment(horizontalLayout, Alignment.BOTTOM_RIGHT);

        final Button save = new Button(getMessage("label.save"), new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                final String displayName = (String) displayNameField.getValue();
                final String filename = (String) fileNameField.getValue();

//        	fileNode.edit(displayName, filename);

                (getParent()).removeWindow(window);

                final Item item = fileTable.getItem(fileNode.getExternalId());
                final Link displayNameLink = (Link) item.getItemProperty("displayName").getValue();
                displayNameLink.setCaption(displayName);
                displayNameLink.setDescription(filename);
                final Link filenameLink = (Link) item.getItemProperty("filename").getValue();
                filenameLink.setCaption(filename);
                filenameLink.setDescription(displayName);
            }
        });
        horizontalLayout.addComponent(save);
        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                (getParent()).removeWindow(window);
            }
        });
        horizontalLayout.addComponent(close);
    }

}
