package module.fileManagement.presentationTier.component;

import java.io.File;

import module.contents.presentationTier.component.BaseComponent;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

import org.vaadin.easyuploads.DirectoryFileFactory;

import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import vaadin.annotation.EmbeddedComponent;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "FileRepositoryView-(.*)" })
public class FileRepositoryView extends BaseComponent
	implements EmbeddedComponentContainer, Property.ValueChangeListener, Action.Handler {

    private final Action ACTION_ADD = new Action(getMessage("label.file.repository.add.dir"));
    private final Action ACTION_EDIT = new Action(getMessage("label.file.repository.edit"));
    private final Action ACTION_DELETE = new Action(getMessage("label.file.repository.delete"));
    private final Action[] ACTIONS = new Action[] { ACTION_ADD, ACTION_EDIT, ACTION_DELETE };
    private final Action[] ACTIONS_ADD = new Action[] { ACTION_ADD };

    private DirNode dirNode = null;

    private Tree tree;

    private DirNode selectedNode;
    private Panel contentPanel;
    private Panel panel0020;

    @Override
    protected String getBundle() {
	return "resources.FileManagementResources";
    }

    @Override
    public void setArguments(final String... arguments) {
	final User user = UserView.getCurrentUser();
	dirNode = FileRepository.getOrCreateFileRepository(user);
    }

    @Override
    public void attach() {
	final VerticalLayout layout = createVerticalLayout();

	final Label title = new Label(getMessage("label.file.repository.of") + " " + dirNode.getName());
	title.setStyleName(Reindeer.LABEL_H2);
	layout.addComponent(title);
	layout.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

	final GridLayout grid = createLayout(layout);

	panel0020 = new Panel(new HorizontalLayout());
	final HorizontalLayout layout1 = (HorizontalLayout) panel0020.getContent();
        layout1.setMargin(true);
        layout1.setSpacing(true);
        grid.addComponent(panel0020, 0, 0, 2, 0);
        grid.setComponentAlignment(panel0020, Alignment.MIDDLE_CENTER);
	panel0020.setStyleName(Reindeer.PANEL_LIGHT);
	fillMainPanel();

	final Panel panel0111 = createPanel(grid, 0, 1, 1, 1);
	fillAddPanel(panel0111);

//	final Panel panel21 = createPanel(grid, 2, 1);
//        fillPanel(panel21);

	setImmediate(true);
	super.attach();
    }

    private void fillMainPanel() {
	selectedNode = dirNode;

	final AbstractOrderedLayout layout = (AbstractOrderedLayout) panel0020.getContent();
	
	layout.setSpacing(true);

        tree = new Tree();
        tree.setWidth("150px");
        layout.addComponent(tree);
        layout.setComponentAlignment(tree, Alignment.TOP_LEFT);

        // Contents from a (prefilled example) hierarchical container:
        tree.setContainerDataSource(getHardwareContainer());

        // Add Valuechangelistener and Actionhandler
        tree.addListener(this);

        // Add actions (context menu)
        tree.addActionHandler(this);

        // Cause valueChange immediately when the user selects
        tree.setImmediate(true);

        // Set tree to show the 'name' property as caption for items
        tree.setItemCaptionPropertyId("name");
        tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

        // Expand whole tree
        for (final Object id : tree.rootItemIds()) {
            tree.expandItemsRecursively(id);
        }

        listFiles(dirNode);
    }

    public HierarchicalContainer getHardwareContainer() {
        final HierarchicalContainer hwContainer = new HierarchicalContainer();
        hwContainer.addContainerProperty("name", String.class, null);
        hwContainer.addContainerProperty("icon", ThemeResource.class, new ThemeResource("../runo/icons/16/document.png"));

        final Item item = hwContainer.addItem(0);
        item.getItemProperty("name").setValue(getMessage("label.file.repository.root"));
        hwContainer.setChildrenAllowed(0, true);

        return hwContainer;
    }

    private void listFiles(final DirNode dirNode) {
	final Panel contentPanel = new Panel(getSelectedDirPath(dirNode));

        final VerticalLayout layout = (VerticalLayout) contentPanel.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();
	if (dirNode.hasAnyChildFile()) {
	    for (final AbstractFileNode abstractFileNode : dirNode.getChildSet()) {
		if (abstractFileNode.isFile()) {
		    final FileNode fileNode = (FileNode) abstractFileNode;
		    final Label label = new Label(fileNode.getExternalId());
		    contentPanel.addComponent(label);
		}
	    }
	} else {
	    final Label noFilesFound = new Label(getMessage("label.file.repository.no.files.found"));
	    contentPanel.addComponent(noFilesFound);	    
	}

	if (this.contentPanel == null) {
	    panel0020.addComponent(contentPanel);
	} else {
	    panel0020.replaceComponent(this.contentPanel, contentPanel);
	}
	((AbstractOrderedLayout) panel0020.getContent()).setComponentAlignment(contentPanel, Alignment.TOP_CENTER);
	this.contentPanel = contentPanel;

	fillAddPanel(contentPanel);
    }

    private void fillAddPanel(final Panel panel) {
        final MultiFileUpload multiFileUpload = new MultiFileUpload() {

            @Override
            protected String getAreaText() {
        	return getMessage("label.file.repository.drop.files.here");
            };

            @Override
            protected String getUploadButtonCaption() {
        	return getMessage("label.file.repository.add.file");
            }
            
	    @Override
	    protected void handleFile(File file, String fileName, String mimeType, long length) {
		final Label label = new Label(fileName);
		contentPanel.addComponent(label);
	    }

	};
	final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	multiFileUpload.setFileFactory(directoryFileFactory);
	panel.addComponent(multiFileUpload);
	//setComponentAlignment(multiFileUpload, Alignment.TOP_RIGHT);
    }

    private String getSelectedDirPath(final DirNode dirNode) {
	return dirNode.hasParent() ? getSelectedDirPath(dirNode.getParent()) + " > " + dirNode.getName() : getMessage("label.file.repository.root");
    }

    private GridLayout createLayout(final VerticalLayout layout) {
	setCompositionRoot(layout);

        final GridLayout grid = new GridLayout(3, 2);
        grid.setSpacing(true);

        // The style allows us to visualize the cell borders in this example.
        grid.addStyleName("gridexample");

        grid.setWidth(850, Sizeable.UNITS_PIXELS);
        grid.setHeight(500, Sizeable.UNITS_PIXELS);

        // Add the layout to the containing layout.
        layout.addComponent(grid);

        // Align the grid itself within its container layout.
        layout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

        return grid;
    }

    private Panel createPanel(final GridLayout grid, final int column1, final int row1, final int column2, final int row2) {
        final Panel panel = createPanel();
        grid.addComponent(panel, column1, row1, column2, row2);
        grid.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        return panel;
    }

    private Panel createPanel(final GridLayout grid, final int column, final int row) {
        final Panel panel = createPanel();
        grid.addComponent(panel, column, row);
        grid.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        return panel;
    }

    private Panel createPanel() {
        final Panel panel = new Panel();
        //panel.setHeight("200px");

        VerticalLayout layout1 = (VerticalLayout) panel.getContent();
        layout1.setMargin(true);
        layout1.setSpacing(true);

        return panel;
    }

    private void fillPanel(final Panel panel) {
	for (int i = 0; i < 2; i++) {
            panel.addComponent(new Label("The quick brown fox jumps over the lazy dog."));
        }
    }

    public void valueChange(ValueChangeEvent event) {
        if (event.getProperty().getValue() != null) {
            final Integer itemId = (Integer) event.getProperty().getValue();
            listFiles(dirNode);
        } else {
            listFiles(dirNode);
        }
    }

    public Action[] getActions(Object target, Object sender) {
	final Integer itemId = (Integer) target;
	return itemId.intValue() == 0 ? ACTIONS_ADD : ACTIONS;
    }

    public void handleAction(final Action action, final Object sender, final Object target) {
        if (action == ACTION_ADD) {
            // Allow children for the target item, and expand it
            tree.setChildrenAllowed(target, true);
            tree.expandItem(target);

            // Create new item, set parent, disallow children (= leaf node)
            Object itemId = tree.addItem();
            tree.setParent(itemId, target);
            tree.setChildrenAllowed(itemId, false);

            // Set the name for this item (we use it as item caption)
            Item item = tree.getItem(itemId);
            Property name = item.getItemProperty(ExampleUtil.hw_PROPERTY_NAME);
            name.setValue("New Item");
        } else if (action == ACTION_EDIT) {
            final Window outerWindow = getWindow();
            final Window subwindow = new Window("A subwindow");
            subwindow.setModal(true);
            subwindow.setWidth("200px");
            outerWindow.addWindow(subwindow);

            final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
            layout.setMargin(true);
            layout.setSpacing(true);

            final String itemValue = (String) tree.getItem(target).getItemProperty("name").getValue();
            final TextField editor = new TextField(null, itemValue);

            subwindow.addComponent(editor);

            final Button save = new Button(getMessage("label.save"), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    tree.getItem(target).getItemProperty("name").setValue(editor.getValue());
                    outerWindow.removeWindow(subwindow);
                }
            });

            final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    outerWindow.removeWindow(subwindow);
                }
            });

            final HorizontalLayout hlayout = new HorizontalLayout();
            layout.addComponent(hlayout);
            hlayout.addComponent(save);
            hlayout.addComponent(close);
        } else if (action == ACTION_DELETE) {
            Object parent = tree.getParent(target);
            tree.removeItem(target);
            // If the deleted object's parent has no more children, set it's
            // childrenallowed property to false (= leaf node)
            if (parent != null && tree.getChildren(parent).size() == 0) {
                tree.setChildrenAllowed(parent, false);
            }
        }
    }

}
