package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import myorg.domain.groups.PersistentGroup;
import myorg.util.BundleUtil;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

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

public class DirNodeDetails extends Window {

    protected static String getBundle() {
	return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
	return BundleUtil.getFormattedStringFromResourceBundle(getBundle(), key, args);
    }

    final DirNode dirNode;
    final Table fileTable;

    public DirNodeDetails(final DirNode dirNode, final Table fileTable) {
	super(getMessage("label.file.details"));
	this.dirNode = dirNode;
	this.fileTable = fileTable;

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();

        final Label fileDetails = getDirDetails();
        layout.addComponent(fileDetails);

        final Component readGroupLine = createGroupLine("label.file.group.read", new ChangeGroupProcedure() {

            private PersistentGroup persistentGroup = dirNode.getReadGroup();

            @Override
            public PersistentGroup getGroup() {
        	return persistentGroup;
            }

	    @Override
	    public void execute(final PersistentGroup group) {
		persistentGroup = group;
//		dirNode.changeReadGroup(group);

		for (final Object itemId : fileTable.getItemIds()) {
		    final Item item = fileTable.getItem(itemId);
		    final String externalId = (String) itemId;
		    final FileNode fileNode = AbstractDomainObject.fromExternalId(externalId);
		    item.getItemProperty("groupRead").setValue(fileNode.getReadGroup().getName());
		}
	    }
            
        });
        layout.addComponent(readGroupLine);

        final Component writeGroupLine = createGroupLine("label.file.group.write", new ChangeGroupProcedure() {

            private PersistentGroup persistentGroup = dirNode.getWriteGroup();

            @Override
            public PersistentGroup getGroup() {
        	return persistentGroup;
            }

	    @Override
	    public void execute(final PersistentGroup group) {
		persistentGroup = group;
//		dirNode.changeWriteGroup(group);

		for (final Object itemId : fileTable.getItemIds()) {
		    final Item item = fileTable.getItem(itemId);
		    final String externalId = (String) itemId;
		    final FileNode fileNode = AbstractDomainObject.fromExternalId(externalId);
		    item.getItemProperty("groupWrite").setValue(fileNode.getWriteGroup().getName());
		}
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

    private Label getDirDetails() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getMessage("label.dir.displayName"));
        builder.append(": ");
        builder.append(dirNode.getName());
        builder.append("<br/>");
        builder.append(getMessage("label.dir.fileCount"));
        builder.append(": ");
        builder.append(dirNode.countFiles());
        builder.append("<br/>");
        builder.append(getMessage("label.dir.fileSize"));
        builder.append(": ");
        builder.append(dirNode.countFilesSize());
        builder.append("<br/>");

        return new Label(builder.toString(), Label.CONTENT_XHTML);
    }

    private Component createGroupLine(final String labelKey, final ChangeGroupProcedure procedure) {
	final Window window = this;

        final HorizontalLayout layout = new HorizontalLayout();

        final Label label = new Label();
        layout.addComponent(label);
        setGroupLabel(label, labelKey, procedure.getGroup());

        if (dirNode.isWriteGroupMember()) {
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
