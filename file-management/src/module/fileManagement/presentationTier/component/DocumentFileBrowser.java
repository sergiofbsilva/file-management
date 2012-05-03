package module.fileManagement.presentationTier.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.presentationTier.component.viewers.VisibilityListViewer;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.RoleType;
import myorg.domain.User;
import myorg.domain.groups.Role;
import pt.ist.vaadinframework.data.VBoxProperty;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class DocumentFileBrowser extends CustomComponent implements ValueChangeNotifier, ValueChangeListener {

    private boolean accessDenied = false;

    public Component getCurrent() {
	return this;
    }

    protected String getMessage(final String key, String... args) {
	return FileManagementSystem.getMessage(key, args);
    }

    public class DocumentTable extends Table implements Table.ValueChangeListener, ItemClickListener {

	private static final int MIN = 10;
	private static final int MAX = 25;

	public java.util.Collection<?> getSortableContainerPropertyIds() {
	    return Collections.EMPTY_SET;
	};

	DocumentTable() {
	    super();
	    setSizeFull();
	    setPageLength(MIN);

	    setSelectable(true);
	    setMultiSelect(false);
	    setImmediate(true);
	    setSortDisabled(false);
	    setColumnCollapsingAllowed(true);

	    final ItemClickListener itemClickListener = this;
	    addListener(itemClickListener);
	    addGeneratedColumn("visibilidade", new ColumnGenerator() {

		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
		    VisibilityListViewer viewer = new VisibilityListViewer();
		    final DomainItem<AbstractFileNode> fileNode = (DomainItem<AbstractFileNode>) source.getItem(itemId);
		    final Property itemProperty = fileNode.getItemProperty("visibilityGroups");
		    viewer.setPropertyDataSource(itemProperty);
		    return viewer;
		}
	    });

	    addListener(new ItemSetChangeListener() {

		@Override
		public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
		    final int size = event.getContainer().size();
		    if (size < MIN) {
			setPageLength(MIN);
		    } else if (size >= MIN && size <= MAX) {
			setPageLength(0);
		    } else if (size > MAX) {
			setPageLength(MAX);
		    }
		}

	    });
	}

	@Override
	public void itemClick(final ItemClickEvent event) {

	    AbstractFileNode abstractFileNode = getNodeFromItemId(event.getItemId());

	    if (event.isDoubleClick()) {
		if (abstractFileNode.isDir()) {
		    nodeItem.setValue(abstractFileNode);
		    documentMenu.addDirNode((DirNode) abstractFileNode);
		} else if (abstractFileNode.isFile()) {
		    final FileNode fileNode = (FileNode) abstractFileNode;
		    final Document document = fileNode.getDocument();
		    final VersionedFile file = document.getLastVersionedFile();
		    final String filename = file.getFilename();

		    final FileNodeStreamSource streamSource = new FileNodeStreamSource(fileNode);
		    final StreamResource resource = new StreamResource(streamSource, filename, getApplication());
		    resource.setMIMEType(file.getContentType());
		    resource.setCacheTime(0);

		    getWindow().open(resource);
		} else {
		    throw new Error("unknown.file.type: " + abstractFileNode);
		}
	    } else { // single click

	    }
	}

    }

    public class DocumentMenu extends MenuBar {

	DocumentMenu() {
	    setWidth(100, Sizeable.UNITS_PERCENTAGE);
	}

	private class DirNodeCommand implements Command {

	    final private DirNode dirNode;

	    public DirNodeCommand(DirNode dirNode) {
		super();
		this.dirNode = dirNode;
	    }

	    @Override
	    public void menuSelected(MenuItem selectedItem) {
		if (!nodeItem.getValue().equals(dirNode)) {
		    removeAllItemsAfter(selectedItem);
		    nodeItem.setValue(dirNode);
		}
	    }

	}

	private void removeAllItemsAfter(MenuItem menuItem) {
	    final int index = getItems().indexOf(menuItem);
	    if (index != -1) {
		setItems(new ArrayList<MenuItem>(getItems().subList(0, index + 1)));
	    }
	}

	private void setItems(List<MenuItem> items2) {
	    getItems().clear();
	    getItems().addAll(items2);
	    requestRepaint();
	}

	public void addDirNode(final DirNode dirNode) {
	    if (dirNode.isAccessible()) {
		addItem(getNodeName(dirNode) + "  »", new DirNodeCommand(dirNode));
	    }
	}

	@Override
	public String toString() {
	    final List<DirNode> dirNodes = new ArrayList<DirNode>();
	    for (MenuItem item : getItems()) {
		dirNodes.add(((DirNodeCommand) item.getCommand()).dirNode);
	    }
	    return new ContextPath(dirNodes).toString();
	}

	public void setDirNodes(ContextPath contextPath) {
	    getItems().clear();
	    for (DirNode dirNode : contextPath.getDirNodes()) {
		addDirNode(dirNode);
	    }
	}

	private List<DirNode> getDirNodes() {
	    final List<DirNode> dirNodes = new ArrayList<DirNode>();
	    for (MenuItem item : getItems()) {
		dirNodes.add(((DirNodeCommand) item.getCommand()).dirNode);
	    }
	    return dirNodes;
	}

	public ContextPath getContextPath() {
	    return new ContextPath(getDirNodes());
	}
    }

    private final DocumentTable documentTable = new DocumentTable();
    private final DocumentMenu documentMenu = new DocumentMenu();

    private DomainItem<AbstractFileNode> nodeItem;
    private DomainContainer<AbstractFileNode> items;

    @Override
    public void attach() {
	super.attach();
	// outerWindow = getWindow();
	final AbstractLayout layout = new VerticalLayout();
	layout.setSizeFull();
	documentMenu.setSizeFull();
	documentTable.setSizeFull();
	layout.addComponent(documentMenu);
	layout.addComponent(documentTable);
	setCompositionRoot(layout);
	if (accessDenied) {
	    getWindow().showNotification("Access Denied.", Notification.TYPE_ERROR_MESSAGE);
	    return;
	}
    }

    public DirNode getInitialDirNode() {
	return FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
    }

    public DomainContainer<AbstractFileNode> getNodes() {
	DomainItem<AbstractFileNode> item = getNodeItem();
	if (item.getValue() != null) {
	    return (DomainContainer<AbstractFileNode>) item.getItemProperty("child");
	}
	return items;
    }

    public void setNodes(Set<AbstractFileNode> nodes) {
	if (items == null) {
	    items = new DomainContainer<AbstractFileNode>(nodes, AbstractFileNode.class);
	    nodeItem.setValue(null);
	} else {
	    items.removeAllItems();
	    items.addItemBatch(nodes);
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public void valueChange(ValueChangeEvent event) {
	final DomainContainer<AbstractFileNode> childs = getNodes();
	childs.addContainerFilter(new Filter() {

	    @Override
	    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
		AbstractFileNode node = getNodeFromItemId(itemId);
		return node != null ? node.isAccessible() : false;
	    }

	    @Override
	    public boolean appliesToProperty(Object propertyId) {
		return true;
	    }

	});

	childs.setContainerProperties("displayName", "icon");
	childs.setItemSorter(new ItemSorter() {

	    @Override
	    public void setSortProperties(final Sortable container, final Object[] propertyId, final boolean[] ascending) {
	    }

	    @Override
	    public int compare(final Object itemId1, final Object itemId2) {

		final AbstractFileNode node1 = getNodeFromItemId(itemId1);
		final AbstractFileNode node2 = getNodeFromItemId(itemId2);

		return node1.compareTo(node2);
	    }

	});
	documentTable.setContainerDataSource(childs);
	documentTable.setVisibleColumns(new String[] { "displayName", "visibilidade" });
	documentTable.setSortContainerPropertyId("displayName");
	documentTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	documentTable.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
	documentTable.setItemIconPropertyId("icon");
	documentTable.setColumnReorderingAllowed(false);
	documentTable.setColumnExpandRatio("icon", 0.15f);
	documentTable.setColumnExpandRatio("displayName", 0.60f);
	documentTable.setColumnExpandRatio("visibilidade", 0.25f);
	documentTable.sort();

	// this.dirNode.setValue(dirNode);
	// documentMenu.refresh();
    }

    private ThemeResource getThemeResource(AbstractFileNode abstractFileNode) {
	final String iconFile = abstractFileNode.isDir() ? "folder1_16x16.gif" : getIconFile(((FileNode) abstractFileNode)
		.getDocument().getLastVersionedFile().getFilename());
	return new ThemeResource("../../../images/fileManagement/" + iconFile);
    }

    private String getIconFile(final String filename) {
	final int lastDot = filename.lastIndexOf('.');
	final String fileSuffix = lastDot < 0 ? "file" : filename.substring(lastDot + 1);
	return "fileicons/" + fileSuffix + ".png";
    }

    public String getNodeName(final AbstractFileNode node) {
	final User user = UserView.getCurrentUser();
	final DirNode fileRepository = user.getFileRepository();
	return fileRepository == node ? getMessage("label.menu.home") : node.getDisplayName();
    }

    public DomainItem<AbstractFileNode> getNodeItem() {
	return nodeItem;
    }

    public DirNode getDirNode() {
	return (DirNode) nodeItem.getValue();
    }

    public DocumentTable getDocumentTable() {
	return documentTable;
    }

    public DocumentFileBrowser getFrontPage() {
	return this;
    }

    @Override
    public void addListener(ValueChangeListener listener) {
	documentTable.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
	documentTable.removeListener(listener);
    }

    public void setValue(DirNode dirNode) {
	nodeItem.setValue(dirNode);
    }

    public DocumentFileBrowser() {
	nodeItem = new DomainItem<AbstractFileNode>(DirNode.class);
	nodeItem.addListener(this);
	final DirNode initialDirNode = getInitialDirNode();
	setValue(initialDirNode);
	documentMenu.addDirNode(initialDirNode);
    }

    public DomainContainer<AbstractFileNode> getContainer() {
	return (DomainContainer<AbstractFileNode>) documentTable.getContainerDataSource();
    }

    public void addDirChangedListener(ValueChangeListener listener) {
	nodeItem.addListener(listener);
    }

    private static AbstractFileNode getNodeFromItemId(Object itemId) {
	AbstractFileNode node = null;
	if (itemId instanceof VBoxProperty) {
	    node = (AbstractFileNode) ((VBoxProperty) itemId).getValue();
	} else if (itemId instanceof AbstractFileNode) {
	    node = (AbstractFileNode) itemId;
	}
	return node;
    }

    public void removeNodeAndSelectNext(Object itemId) {
	final Container containerDataSource = documentTable.getContainerDataSource();
	List<Object> itemIds = new ArrayList<Object>(containerDataSource.getItemIds());
	Object nextItemId = null;
	int pos = itemIds.indexOf(itemId);
	if (pos != -1 && itemIds.size() > 1) {
	    nextItemId = itemIds.get(pos + (pos == itemIds.size() - 1 ? -1 : 1));
	}
	containerDataSource.removeItem(itemId);
	documentTable.select(nextItemId);
    }

    public void setContextPath(String pathString) {
	final ContextPath contextPath = new ContextPath(pathString);
	final DirNode lastDirNode = contextPath.getLastDirNode();
	if (lastLineOfDefense(lastDirNode)) {
	    documentMenu.setDirNodes(contextPath);
	    nodeItem.setValue(lastDirNode);
	} else {
	    accessDenied = true;
	}
    }

    private boolean lastLineOfDefense(DirNode lastDirNode) {
	return (lastDirNode.isAccessible() && !lastDirNode.hasTrashUser())
		|| Role.getRole(RoleType.MANAGER).isMember(UserView.getCurrentUser());
    }

    public DocumentMenu getDocumentMenu() {
	return documentMenu;
    }

    public ContextPath getContextPath() {
	return documentMenu.getContextPath();
    }

}
