package module.fileManagement.presentationTier.component;

import java.io.File;

import module.contents.presentationTier.component.BaseComponent;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.organization.domain.Accountability;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

import org.vaadin.easyuploads.DirectoryFileFactory;

import pt.ist.fenixframework.plugins.fileSupport.domain.GenericFile;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import vaadin.annotation.EmbeddedComponent;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
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

    private AbstractOrderedLayout directoryView;    
    private Tree tree;

    private AbstractOrderedLayout folderView;
    private Table fileTable;

    @Override
    protected String getBundle() {
	return "resources.FileManagementResources";
    }

    @Override
    public void setArguments(final String... arguments) {
	final User user = UserView.getCurrentUser();
	dirNode = FileRepository.getOrCreateFileRepository(user);
	dirNode.initIfNecessary();
    }

    @Override
    public void attach() {
	final VerticalLayout layout = createVerticalLayout();
	layout.setSpacing(true);
	layout.setMargin(true);
	setCompositionRoot(layout);

	// Title
	final Label title = new Label(getMessage("label.file.repository.of") + " " + dirNode.getName());
	title.setStyleName(Reindeer.LABEL_H2);
	layout.addComponent(title);
	layout.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

	// Organize page body
        final GridLayout grid = new GridLayout(3, 2);
        grid.setSpacing(true);
        grid.addStyleName("gridexample");
        grid.setWidth(875, Sizeable.UNITS_PIXELS);
        grid.setHeight(525, Sizeable.UNITS_PIXELS);
        layout.addComponent(grid);
        layout.setComponentAlignment(grid, Alignment.TOP_LEFT);

        final Panel panel00 = createGridPanel(grid, 0, 0, 250, 350);
        attachTree(panel00);

        final Panel panel1020 = createGridPanel(grid, 1, 0, 2, 0, 600, 350);
        attachFolderView(panel1020);

        final Panel panel01 = createGridPanel(grid, 0, 1, 250, 150);
        attachChangeRepositoryView(panel01);

        final Panel panel11 = createGridPanel(grid, 1, 1, 290, 150);
        panel11.addComponent(new Label("Quota and Space Widget"));

        final Panel panel21 = createGridPanel(grid, 2, 1, 290, 150);
        attachAddPanel(panel21);

	setImmediate(true);
	super.attach();
    }

    private Panel createGridPanel(final GridLayout grid, final int column, final int row, final int width, int height) {
	return createGridPanel(grid, column, row, column, row, width, height);
    }

    private Panel createGridPanel(final GridLayout grid, final int column1, final int row1, final int column2, final int row2, final int width, int height) {
        final Panel panel = new Panel(new HorizontalLayout());
        final AbstractOrderedLayout layout = (AbstractOrderedLayout) panel.getContent();
        layout.setSpacing(true);
        layout.setMargin(true);
        panel.setWidth(width, Sizeable.UNITS_PIXELS);
        panel.setHeight(height, Sizeable.UNITS_PIXELS);
        grid.addComponent(panel, column1, row1, column2, row2);
        grid.setComponentAlignment(panel, Alignment.TOP_LEFT);
	return panel;
    }

    private void attachTree(final Panel panel) {
	directoryView = createDirectoryView();
	panel.addComponent(directoryView);
    }

    private AbstractOrderedLayout createDirectoryView() {
	final AbstractOrderedLayout layout = new VerticalLayout();
	
        tree = new Tree();
        tree.setSizeFull();
        layout.addComponent(tree);
        layout.setComponentAlignment(tree, Alignment.TOP_LEFT);

        // Contents from a (prefilled example) hierarchical container:
        tree.setContainerDataSource(getHierarchicalContainer());

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

        return layout;
    }

    private void attachFolderView(final Panel panel) {
	folderView = createFolderView();
	panel.addComponent(folderView);
    }

    private AbstractOrderedLayout createFolderView() {
	final AbstractOrderedLayout layout = new VerticalLayout();
	layout.setSpacing(true);
	layout.setMargin(false);

	final Label selectedDirPrefix = new Label(getMessage("label.file.repository.selected.dir")
		+ "<strong>" + getSelectedDirPath(dirNode) + "</strong>", Label.CONTENT_XHTML);
	layout.addComponent(selectedDirPrefix);

	fileTable = new Table();
	layout.addComponent(fileTable);
	fileTable.setWidth(550, Sizeable.UNITS_PIXELS);
	fileTable.setHeight(275, Sizeable.UNITS_PIXELS);
	fileTable.setSelectable(true);
	fileTable.setMultiSelect(true);
	fileTable.setImmediate(true);

        // connect data source
	fileTable.setContainerDataSource(getFileContainer());

        // turn on column reordering and collapsing
	fileTable.setColumnReorderingAllowed(true);
	fileTable.setColumnCollapsingAllowed(true);

        // set column headers
	fileTable.setColumnHeaders(new String[] { "File Name", "File Size" });

        return layout;
    }

    public IndexedContainer getFileContainer() {
        IndexedContainer c = new IndexedContainer();
        c.addContainerProperty("filename", String.class, null);
        c.addContainerProperty("filesize", String.class, null);

        for (final AbstractFileNode abstractFileNode : dirNode.getChildSet()) {
            if (abstractFileNode.isFile()) {
        	final FileNode fileNode = (FileNode) abstractFileNode;
        	final GenericFile file = fileNode.getFile();

        	final String oid = fileNode.getExternalId();
        	final String filename = file.getFilename();
        	final String filesize = Integer.toString(file.getContent().length);

                final Item item = (Item) c.addItem(oid);
                item.getItemProperty("filename").setValue(filename);
                item.getItemProperty("filesize").setValue(filesize);
            }
        }

        c.sort(new Object[] { "filename" }, new boolean[] { true });
        return c;
    }

    public HierarchicalContainer getHierarchicalContainer() {
        final HierarchicalContainer hwContainer = new HierarchicalContainer();
        hwContainer.addContainerProperty("name", String.class, null);
        hwContainer.addContainerProperty("icon", ThemeResource.class, new ThemeResource("../runo/icons/16/document.png"));

        addItem(hwContainer, dirNode);
        addChildNodes(hwContainer, dirNode);

        return hwContainer;
    }

    private void addChildNodes(final HierarchicalContainer hwContainer, final DirNode parentNode) {
	for (final AbstractFileNode abstractFileNode : parentNode.getChildSet()) {
	    if (abstractFileNode.isDir()) {
		final DirNode dirNode = (DirNode) abstractFileNode;
		addItem(hwContainer, dirNode);
		addChildNodes(hwContainer, dirNode);
	    }
	}
    }

    private Item addItem(final HierarchicalContainer hwContainer, final DirNode dirNode) {
	final String oid = dirNode.getExternalId();
	final Item item = hwContainer.addItem(oid);
	if (dirNode.hasParent()) {
	    item.getItemProperty("name").setValue(dirNode.getName());
	    final DirNode parent = dirNode.getParent();
	    final String parentOid = parent.getExternalId();
	    hwContainer.setParent(oid, parentOid);
	} else {
	    item.getItemProperty("name").setValue(getMessage("label.file.repository.root"));
	}
	final boolean hasChildren = dirNode.hasAnyChildDir();
	hwContainer.setChildrenAllowed(oid, hasChildren);
	return item;
    }

    private void attachAddPanel(final Panel panel) {
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
	    protected void handleFile(final File file, final String fileName, final String mimeType, final long length) {
        	final FileNode fileNode = dirNode.createFile(file, fileName);

        	final String oid = fileNode.getExternalId();
        	final String filename = fileName;
        	final String filesize = Long.toString(length);

                final Item item = (Item) fileTable.addItem(oid);
                item.getItemProperty("filename").setValue(filename);
                item.getItemProperty("filesize").setValue(filesize);
	    }

	};
	final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	multiFileUpload.setFileFactory(directoryFileFactory);

	final AbstractOrderedLayout layout = (AbstractOrderedLayout) panel.getContent();
        layout.addComponent(multiFileUpload);
        layout.setComponentAlignment(multiFileUpload, Alignment.TOP_CENTER);
    }

    private String getSelectedDirPath(final DirNode dirNode) {
	return dirNode.hasParent() ? getSelectedDirPath(dirNode.getParent()) + " > " + dirNode.getName() : getMessage("label.file.repository.root");
    }

    public void valueChange(ValueChangeEvent event) {
        if (event.getProperty().getValue() != null) {
            final String itemId = (String) event.getProperty().getValue();
            dirNode = getDomainObject(itemId);

            final AbstractComponentContainer parent = (AbstractComponentContainer) folderView.getParent();
            final AbstractOrderedLayout newFolderView = createFolderView();
            parent.replaceComponent(folderView, newFolderView);
            folderView = newFolderView;
        }
    }

    public Action[] getActions(Object target, Object sender) {
	final String dirNodeOid = (String) target;
	final DirNode dirNode = AbstractDomainObject.fromExternalId(dirNodeOid);
	return dirNode.hasParent() ? ACTIONS : ACTIONS_ADD;
    }

    public void handleAction(final Action action, final Object sender, final Object target) {
        if (action == ACTION_ADD) {
            // Allow children for the target item, and expand it
            tree.setChildrenAllowed(target, true);
            tree.expandItem(target);

            final String parentOid = (String) target;

            final DirNode selectedDirNode = getDomainObject(parentOid);
            final DirNode dirNode = selectedDirNode.createDir("New Item");
            final String oid = dirNode.getExternalId();

            final Item item = tree.addItem(oid);
            tree.setParent(oid, parentOid);
            tree.setChildrenAllowed(oid, false);
            final Property name = item.getItemProperty(ExampleUtil.hw_PROPERTY_NAME);
            name.setValue(dirNode.getName());
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
                    final String value = (String) editor.getValue();
                    final DirNode dirNode = getDomainObject((String) target);
                    dirNode.edit(value);
                    tree.getItem(target).getItemProperty("name").setValue(value);
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
            final DirNode dirNode = getDomainObject((String) target);
            dirNode.delete();

            final Object parent = tree.getParent(target);
            tree.removeItem(target);
            if (parent != null && tree.getChildren(parent).size() == 0) {
                tree.setChildrenAllowed(parent, false);
            }
        }
    }

    private void attachChangeRepositoryView(final Panel panel) {
	final OptionGroup optionGroup = new OptionGroup(getMessage("label.file.repository.select"));
	optionGroup.setNullSelectionAllowed(false);
	optionGroup.setImmediate(true);
	optionGroup.addContainerProperty("name", String.class, null);
	optionGroup.setItemCaptionPropertyId("name");
	optionGroup.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

	final String oid = dirNode.getExternalId();
	final Item item = optionGroup.addItem(oid);
	item.getItemProperty("name").setValue(getMessage("label.file.repository.my"));
	optionGroup.select(oid);

	final User user = UserView.getCurrentUser();
	final Person person = user.getPerson();
	for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
	    if (accountability.isActiveNow()) {
		final Party party = accountability.getParent();
		if (party.isUnit()) {
		    final Unit unit = (Unit) party;
		    final DirNode fileRepository = FileRepository.getOrCreateFileRepository(unit);
		    final String repositoryOid = fileRepository.getExternalId();
		    final Item repositoryItem = optionGroup.addItem(repositoryOid);
		    repositoryItem.getItemProperty("name").setValue(unit.getPartyName().getContent());
		}
	    }
	}

	optionGroup.addListener(new ValueChangeListener() {
	    
	    @Override
	    public void valueChange(final ValueChangeEvent event) {
	        if (event.getProperty().getValue() != null) {
	            final String dirNodeOid = (String) event.getProperty().getValue();
	            dirNode = getDomainObject(dirNodeOid);

	            final AbstractComponentContainer parentFolder = (AbstractComponentContainer) folderView.getParent();
	            final AbstractOrderedLayout newFolderView = createFolderView();
	            parentFolder.replaceComponent(folderView, newFolderView);
	            folderView = newFolderView;

	            final AbstractComponentContainer parentDirectory = (AbstractComponentContainer) directoryView.getParent();
	            final AbstractOrderedLayout newDirectoryView = createDirectoryView();
	            parentDirectory.replaceComponent(directoryView, newDirectoryView);
	            directoryView = newDirectoryView;
	        }
	    }

	});

	panel.addComponent(optionGroup);
    }

}
