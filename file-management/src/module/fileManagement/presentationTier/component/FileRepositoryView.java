package module.fileManagement.presentationTier.component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import myorg.domain.MyOrg;
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
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "FileRepositoryView-(.*)" })
public class FileRepositoryView extends BaseComponent
	implements EmbeddedComponentContainer, Property.ValueChangeListener, Action.Handler {

    private final Action ACTION_DOWNLOAD_DIR = new Action(getMessage("label.file.repository.download.dir"));
    private final Action ACTION_EDIT = new Action(getMessage("label.file.repository.edit"));
    private final Action ACTION_DIR_SHOW_DETAILS = new Action(getMessage("label.dir.show.details"));
    private final Action ACTION_ADD = new Action(getMessage("label.file.repository.add.dir"));
    private final Action ACTION_DELETE = new Action(getMessage("label.file.repository.delete"));
    private final Action[] ACTIONS = new Action[] { ACTION_DOWNLOAD_DIR, ACTION_EDIT, ACTION_DIR_SHOW_DETAILS, ACTION_ADD, ACTION_DELETE };
    private final Action[] ACTIONS_ADD = new Action[] { /* ACTION_DOWNLOAD_DIR, */ ACTION_DIR_SHOW_DETAILS, ACTION_ADD };

    private final Action ACTION_FILE_EDIT = new Action(getMessage("label.file.edit"));
    private final Action ACTION_FILE_SHOW_DETAILS = new Action(getMessage("label.file.show.details"));
    private final Action ACTION_FILE_DELETE = new Action(getMessage("label.file.delete"));
    private final Action[] ACTIONS_FILES = new Action[] { ACTION_FILE_EDIT, ACTION_FILE_SHOW_DETAILS, ACTION_FILE_DELETE };

    private DirNode dirNode = null;

    private Label title;

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
	super.attach();

	final VerticalLayout layout = createVerticalLayout();
	layout.setSpacing(true);
	layout.setMargin(true);
	setCompositionRoot(layout);

	title = new Label();
	setTitle(dirNode.getName());
	title.setStyleName(Reindeer.LABEL_H2);
	layout.addComponent(title);
	layout.setComponentAlignment(title, Alignment.MIDDLE_LEFT);


        final GridLayout grid1 = new GridLayout(2, 1);
        grid1.setSpacing(true);
        grid1.setWidth(875, Sizeable.UNITS_PIXELS);
        grid1.setHeight(365, Sizeable.UNITS_PIXELS);
        layout.addComponent(grid1);
        layout.setComponentAlignment(grid1, Alignment.TOP_LEFT);

        final Panel panel00 = createGridPanel(grid1, 0, 0, 250, 350);
        attachTree(panel00);

        final Panel panel1020 = createGridPanel(grid1, 1, 0, 600, 350);
        attachFolderView(panel1020);


        final GridLayout grid2 = new GridLayout(2, 1);
        grid2.setSpacing(true);
        grid2.setWidth(875, Sizeable.UNITS_PIXELS);
        grid2.setHeight(150, Sizeable.UNITS_PIXELS);
        layout.addComponent(grid2);
        layout.setComponentAlignment(grid2, Alignment.TOP_LEFT);

        final Panel panel01 = createGridPanel(grid2, 0, 0, 550, 150);
        attachChangeRepositoryView(panel01);

        final Panel panel21 = createGridPanel(grid2, 1, 0, 290, 150);
        attachAddPanel(panel21);

        setImmediate(true);
    }

    private void setTitle(final String repositoryName) {
	title.setValue(getMessage("label.file.repository.of") + " " + dirNode.getName());
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

    private int fileCount = 0;
    private long totalFilesize = 0l;

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
	fileTable.setSelectable(false);
	fileTable.setMultiSelect(false);
	fileTable.setImmediate(true);
	fileTable.setSortDisabled(false);

        // turn on column reordering and collapsing
	fileTable.setColumnReorderingAllowed(true);
	fileTable.setColumnCollapsingAllowed(true);

	fileTable.setFooterVisible(true);

	updateFileTableFooter();

        // add initial data
	initFileContainer();

        // set column headers
	fileTable.setColumnHeaders(new String[] {
		getMessage("label.file.displayName"),
		getMessage("label.file.filename"),
		getMessage("label.file.contentType"),
		getMessage("label.file.size"),
		getMessage("label.file.contentKey"),
		getMessage("label.file.storage"),
		getMessage("label.file.group.read"),
		getMessage("label.file.group.write")
	});

	fileTable.addActionHandler(new Action.Handler() {

	    public Action[] getActions(final Object target, final Object sender) {
		return ACTIONS_FILES;
            }

            public void handleAction(final Action action, final Object sender, final Object target) {
        	final FileNode fileNode = getDomainObject((String) target);
        	final Window window = getWindow();
        	final Window actionWindow;
        	if (action == ACTION_FILE_EDIT) {
        	    actionWindow = new EditFileNode(fileNode, fileTable);
        	} else if (action == ACTION_FILE_SHOW_DETAILS) {
        	    actionWindow = new FileNodeDetails(fileNode, fileTable);
        	} else if (action == ACTION_FILE_DELETE) {
        	    actionWindow = new ConfirmDeleteFile(fileNode, fileTable);
        	} else {
        	    throw new Error("unknown.action");
        	}
        	window.addWindow(actionWindow);
        	actionWindow.center();
            }
        });

        return layout;
    }

    private void updateFileTableFooter() {
	fileTable.setColumnFooter("displayName", getMessage("label.file.total.count", Integer.toString(fileCount)));
	fileTable.setColumnFooter("filesize", Long.toString(totalFilesize));
    }

    public void initFileContainer() {
	fileTable.addContainerProperty("displayName", Link.class, null);
        fileTable.addContainerProperty("filename", Link.class, null);
        fileTable.addContainerProperty("contentType", String.class, null);
        fileTable.addContainerProperty("filesize", Integer.class, null);
        fileTable.addContainerProperty("contentKey", String.class, null);
        fileTable.addContainerProperty("storage", String.class, null);
        fileTable.addContainerProperty("groupRead", String.class, null);
        fileTable.addContainerProperty("groupWrite", String.class, null);

        fileTable.setColumnCollapsed("filename", true);
        fileTable.setColumnCollapsed("contentType", true);
        fileTable.setColumnCollapsed("contentKey", true);
        fileTable.setColumnCollapsed("storage", true);
        fileTable.setColumnCollapsed("groupRead", true);
        fileTable.setColumnCollapsed("groupWrite", true);

        for (final AbstractFileNode abstractFileNode : dirNode.getChildSet()) {
            if (abstractFileNode.isFile()) {
        	final FileNode fileNode = (FileNode) abstractFileNode;
        	final String oid = fileNode.getExternalId();
        	fileTable.addItem(createFileNodeItem(fileNode), oid);
            }
        }
    }

    public Object[] createFileNodeItem(final FileNode fileNode) {
	final GenericFile file = fileNode.getFile();
	final String filename = file.getFilename();
	final String displayName = file.getDisplayName();
	final int length = file.getContent().length;

	final FileNodeStreamSource streamSource = new FileNodeStreamSource(fileNode);
	final StreamResource resource = new StreamResource(streamSource, filename, getApplication());
	resource.setMIMEType(file.getContentType());
	resource.setCacheTime(0);

        final Link displayNameLink = new Link(displayName, resource);
        displayNameLink.setDescription(filename);
        displayNameLink.setTargetName("_new");

        final Link filenameLink = new Link(filename, resource);
        filenameLink.setDescription(displayName);
        filenameLink.setTargetName("_new");

	fileTable.addContainerProperty("displayName", Link.class, null);
        fileTable.addContainerProperty("filename", Link.class, null);
        fileTable.addContainerProperty("contentType", String.class, null);
        fileTable.addContainerProperty("filesize", Integer.class, null);
        fileTable.addContainerProperty("contentKey", String.class, null);
        fileTable.addContainerProperty("storage", String.class, null);
        fileTable.addContainerProperty("groupRead", String.class, null);
        fileTable.addContainerProperty("groupWrite", String.class, null);

        fileCount++;
        totalFilesize += length;
        updateFileTableFooter();

        return new Object[] {
        	displayNameLink,
        	filenameLink,
        	file.getContentType(),
        	Integer.valueOf(length),
        	file.getContentKey(),
        	file.getStorage().getName(),
        	fileNode.getReadGroup().getName(),
        	fileNode.getWriteGroup().getName()
        };
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
        	fileTable.addItem(createFileNodeItem(fileNode), fileNode.getExternalId());
	    }

	};
	final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	multiFileUpload.setFileFactory(directoryFileFactory);

	final AbstractOrderedLayout layout = (AbstractOrderedLayout) panel.getContent();
        layout.addComponent(multiFileUpload);
        layout.setComponentAlignment(multiFileUpload, Alignment.MIDDLE_CENTER);
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
	if (action == ACTION_DOWNLOAD_DIR) {
	    final DirNode dirNode = getDomainObject((String) target);

	    final StreamSource streamSource = new StreamSource() {
		@Override
		public InputStream getStream() {
		    try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final ZipOutputStream out = new ZipOutputStream(byteArrayOutputStream);
			zip(out, dirNode, "");
			out.close();
			return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		    } catch(final Exception e) {
			throw new Error(e);
		    }
		}

		private void zip(final ZipOutputStream out, final AbstractFileNode abstractFileNode, final String path) throws IOException {
		    if (abstractFileNode.isFile()) {
			final FileNode fileNode = (FileNode) abstractFileNode;
			final GenericFile file = fileNode.getFile();

			final BufferedInputStream origin = new BufferedInputStream(file.getStream());
			final ZipEntry entry = new ZipEntry(path + file.getFilename());
			out.putNextEntry(entry);
			int count;
			final int BUFFER = 4096;
			final byte[] data = new byte[BUFFER];
			while((count = origin.read(data, 0, BUFFER)) != -1) {
			    out.write(data, 0, count);
			}
			origin.close();
		    } else if (abstractFileNode.isDir()) {
			final DirNode dirNode = (DirNode) abstractFileNode;
			final String newPath = path + dirNode.getName() + "/";
			for (final AbstractFileNode child : dirNode.getChildSet()) {
			    zip(out, child, newPath);
			}
		    } else {
			throw new Error("unreachable.code");
		    }
		}
	    };
	    final StreamResource resource = new StreamResource(streamSource, dirNode.getName() + ".zip", getApplication());
	    resource.setMIMEType("application/octet-stream");
	    resource.setCacheTime(0);

	    getWindow().open(resource);
	} else if (action == ACTION_ADD) {
            final Window outerWindow = getWindow();
            final Window subwindow = new Window(getMessage("label.file.repository.add.dir"));
            subwindow.setModal(true);
            subwindow.setWidth("200px");
            outerWindow.addWindow(subwindow);

            final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
            layout.setMargin(true);
            layout.setSpacing(true);

            final TextField editor = new TextField();

            subwindow.addComponent(editor);

            final Button save = new Button(getMessage("label.save"), new Button.ClickListener() {
                public void buttonClick(ClickEvent event) {
                    tree.setChildrenAllowed(target, true);
                    tree.expandItem(target);

                    final String parentOid = (String) target;

                    final DirNode selectedDirNode = getDomainObject(parentOid);
                    final DirNode dirNode = selectedDirNode.createDir((String) editor.getValue());
                    final String oid = dirNode.getExternalId();

                    final Item item = tree.addItem(oid);
                    tree.setParent(oid, parentOid);
                    tree.setChildrenAllowed(oid, false);
                    final Property name = item.getItemProperty(ExampleUtil.hw_PROPERTY_NAME);
                    name.setValue(dirNode.getName());

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

        } else if (action == ACTION_EDIT) {
            final Window outerWindow = getWindow();
            final Window subwindow = new Window(getMessage("label.file.repository.edit"));
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
        } else if (action == ACTION_DIR_SHOW_DETAILS) {
            final DirNode dirNode = getDomainObject((String) target);
            final Window window = getWindow();
            final Window actionWindow = new DirNodeDetails(dirNode, fileTable);
            window.addWindow(actionWindow);
            actionWindow.center();
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

    String lastRepositoryOption = null;
    int count = 1;

    private void attachChangeRepositoryView(final Panel panel) {
	final VerticalLayout layout = new VerticalLayout();
	layout.setSpacing(true);
	//layout.setMargin(true);
	panel.addComponent(layout);

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

		    lastRepositoryOption = repositoryOid;
		    count++;

		    if (count == 3) {
			break;
		    }
		}
	    }
	}

	optionGroup.addListener(new ValueChangeListener() {
	    
	    @Override
	    public void valueChange(final ValueChangeEvent event) {
	        if (event.getProperty().getValue() != null) {
	            final String dirNodeOid = (String) event.getProperty().getValue();
	            switchRepository((DirNode) getDomainObject(dirNodeOid));
	        }
	    }

	});

	layout.addComponent(optionGroup);

	final HorizontalLayout horizontalLayout = new HorizontalLayout();
	horizontalLayout.setSpacing(true);
	layout.addComponent(horizontalLayout);

	final Label label = new Label(getMessage("label.other"));
	horizontalLayout.addComponent(label);

        final ComboBox comboBox = new ComboBox();
        comboBox.setWidth(400, UNITS_PIXELS);
        comboBox.setNullSelectionAllowed(false);
        comboBox.addContainerProperty("name", String.class, null);
        comboBox.setItemCaptionPropertyId("name");
        comboBox.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        comboBox.setImmediate(true);
        comboBox.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        comboBox.addListener(new ValueChangeListener() {
	    
	    @Override
	    public void valueChange(final ValueChangeEvent event) {
	        if (event.getProperty().getValue() != null) {
	            final String partyOid = (String) event.getProperty().getValue();
	            final Party party = AbstractDomainObject.fromExternalId(partyOid);

	            final DirNode fileRepository;
	            if (party.isPerson()) {
	        	final Person person = (Person) party;
	        	fileRepository = FileRepository.getOrCreateFileRepository(person.getUser());
	            } else if (party.isUnit()) {
	        	final Unit unit = (Unit) party;
	        	fileRepository = FileRepository.getOrCreateFileRepository(unit);
	            } else {
	        	throw new Error("unreachable.code");
	            }

	            switchRepository(fileRepository);

	            comboBox.setValue(null);

		    final String repositoryOid = fileRepository.getExternalId();
		    if (!optionGroup.containsId(repositoryOid)) {

			if (count == 3) {
			    optionGroup.removeItem(lastRepositoryOption);
			}

			final Item repositoryItem = optionGroup.addItem(repositoryOid);
			repositoryItem.getItemProperty("name").setValue(party.getPartyName().getContent());

			lastRepositoryOption = repositoryOid;

		    }

		    optionGroup.select(repositoryOid);
	        }
	    }
	});

        for (final Party party : MyOrg.getInstance().getPartiesSet()) {
            if (party.isUnit() || (party.isPerson() && ((Person) party).hasUser())) {
        	final String externalId = party.getExternalId();
        	final String name = party.getPresentationName();

        	final Item comboItem = comboBox.addItem(externalId);
        	comboItem.getItemProperty("name").setValue(name);
            }
        }

        horizontalLayout.addComponent(comboBox);
    }

    private void switchRepository(final DirNode fileRepository) {
	dirNode = fileRepository;
	setTitle(dirNode.getName());

	final AbstractComponentContainer parentFolder = (AbstractComponentContainer) folderView.getParent();
	final AbstractOrderedLayout newFolderView = createFolderView();
	parentFolder.replaceComponent(folderView, newFolderView);
	folderView = newFolderView;

	final AbstractComponentContainer parentDirectory = (AbstractComponentContainer) directoryView.getParent();
	final AbstractOrderedLayout newDirectoryView = createDirectoryView();
	parentDirectory.replaceComponent(directoryView, newDirectoryView);
	directoryView = newDirectoryView;

	fileCount = 0;
	totalFilesize = 0l;
    }

}
