package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.FileNode;
import myorg.domain.groups.PersistentGroup;
import myorg.util.BundleUtil;
import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;

public class FileNodeDetails extends Window {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    final FileNode fileNode;
    final Table fileTable;

    public FileNodeDetails(final FileNode fileNode, final Table fileTable) {
	super(getMessage("label.file.details"));
	this.fileNode = fileNode;
	this.fileTable = fileTable;

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();

        final Label fileDetails = getFileDetails();
        layout.addComponent(fileDetails);

        final Component readGroupLine = createGroupLine("label.file.group.read", new ChangeGroupProcedure() {

            private PersistentGroup persistentGroup = fileNode.getReadGroup();

            @Override
            public PersistentGroup getGroup() {
        	return persistentGroup;
            }

	    @Override
	    public void execute(final PersistentGroup group) {
		persistentGroup = group;
		fileNode.changeReadGroup(group);
		final String externalId = fileNode.getExternalId();
		final Item item = fileTable.getItem(externalId);
		item.getItemProperty("groupRead").setValue(group.getName());
	    }
            
        });
        layout.addComponent(readGroupLine);

        final Component writeGroupLine = createGroupLine("label.file.group.write", new ChangeGroupProcedure() {

            private PersistentGroup persistentGroup = fileNode.getWriteGroup();

            @Override
            public PersistentGroup getGroup() {
        	return persistentGroup;
            }

	    @Override
	    public void execute(final PersistentGroup group) {
		persistentGroup = group;
		fileNode.changeWriteGroup(group);
		final String externalId = fileNode.getExternalId();
		final Item item = fileTable.getItem(externalId);
		item.getItemProperty("groupWrite").setValue(group.getName());
	    }
            
        });
        layout.addComponent(writeGroupLine);

        final Window window = this;
        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                (getParent()).removeWindow(window);
            }
        });
        layout.addComponent(close);
        layout.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);
    }

    private Label getFileDetails() {
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

        return new Label(builder.toString(), Label.CONTENT_XHTML);
    }

    private Component createGroupLine(final String labelKey, final ChangeGroupProcedure procedure) {
	final Window window = this;

        final HorizontalLayout layout = new HorizontalLayout();

        final Label label = new Label();
        layout.addComponent(label);
        setGroupLabel(label, labelKey, procedure.getGroup());

        if (fileNode.isWriteGroupMember()) {
            final ChangeGroupProcedure procedureWrapper = new ChangeGroupProcedure() {

        	private PersistentGroup persistentGroup = procedure.getGroup();

        	@Override
        	public void execute(final PersistentGroup group) {
        	    persistentGroup = group;
        	    procedure.execute(group);
        	    setGroupLabel(label, labelKey, group);
        	}

        	@Override
        	public PersistentGroup getGroup() {
        	    return persistentGroup;
        	}
            };
            final Button changeGroupButton = createChangeGroupButton(window, layout, labelKey, procedureWrapper);
            layout.addComponent(changeGroupButton);
        }

        return layout;
    }

    private Button createChangeGroupButton(final Window window, final AbstractLayout layout,
	    final String labelKey, final ChangeGroupProcedure procedureWrapper) {
	final Button changeGroupButton = new Button(getMessage("label.file.group.change"));
	changeGroupButton.setStyleName(BaseTheme.BUTTON_LINK);
	final Button.ClickListener listener = new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
        	final GroupChangeWindow groupChangeWindow = new GroupChangeWindow(labelKey, procedureWrapper);
        	window.getParent().addWindow(groupChangeWindow);
        	layout.replaceComponent(changeGroupButton, createChangeGroupButton(window, layout, labelKey, procedureWrapper));
            }
        };
        changeGroupButton.addListener(listener);
        return changeGroupButton;
    }

    private void setGroupLabel(final Label label, final String labelKey, final PersistentGroup group) {
        final StringBuilder builder = new StringBuilder();
        builder.append(getMessage(labelKey));
        builder.append(": ");
        builder.append(group.getName());
        label.setCaption(builder.toString());
    }

}
