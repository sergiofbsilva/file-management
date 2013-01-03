package module.fileManagement.presentationTier.component;

import java.util.ArrayList;
import java.util.Arrays;
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
import module.fileManagement.domain.exception.NodeDuplicateNameException;
import module.fileManagement.presentationTier.component.viewers.VisibilityListViewer;
import module.fileManagement.presentationTier.pages.DocumentBrowse;

import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.Role;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.EmbeddedApplication;
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
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.terminal.Resource;
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

    private static final List<String> KNOWN_FILE_EXTENSIONS = Arrays.asList("7z", "aiff", "ai", "asc", "audio", "bin", "bz2",
	    "cfc", "cfm", "chm", "class", "conf", "c", "cpp", "cs", "css", "csv", "deb", "divx", "doc", "dot", "eml", "enc",
	    "file", "gif", "gz", "hlp", "html", "htm", "image", "iso", "jar", "java", "jpeg", "jpg", "js", "lua", "mm", "mov",
	    "mp3", "mpg", "m", "odc", "odf", "odg", "odi", "odp", "ods", "odt", "ogg", "pdf", "pgp", "php", "pl", "png", "ppt",
	    "ps", "py", "ram", "rar", "rb", "rm", "rpm", "rtf", "sig", "sql", "swf", "sxc", "sxd", "sxi", "sxw", "tar", "tex",
	    "tgz", "txt", "vcf", "video", "vsd", "wav", "wma", "wmv", "xls", "xml", "xpi", "xvid", "zip");

    private static final ThemeResource defaultFileIcon = new ThemeResource("../../../images/fileManagement/fileicons/file.png");

    private boolean accessDenied = false;
    private static final int MIN = 10;
    private static final int MAX = 25;

    public Component getCurrent() {
	return this;
    }

    protected String getMessage(final String key, String... args) {
	return FileManagementSystem.getMessage(key, args);
    }

    private void open(DirNode dirNode) {
	EmbeddedApplication.open(getApplication(), DocumentBrowse.class, dirNode.getContextPath().toString());
    }

    public class DocumentTable extends Table implements Table.ValueChangeListener, ItemClickListener {

	/*
	 * public java.util.Collection<?> getSortableContainerPropertyIds() {
	 * return Collections.EMPTY_SET; };
	 */

	DocumentTable() {
	    super();
	    setSizeFull();
	    setPageLength(MIN);

	    setSelectable(true);
	    setMultiSelect(false);
	    setImmediate(true);
	    setSortDisabled(false);
	    setColumnCollapsingAllowed(false);
	    setColumnReorderingAllowed(false);
	    setDragMode(TableDragMode.ROW);
	    setDropHandler(new DropHandler() {

		@Override
		public void drop(DragAndDropEvent event) {
		    final DataBoundTransferable transferable = (DataBoundTransferable) event.getTransferable();
		    final AbstractDomainObject sourceItemId = (AbstractDomainObject) transferable.getItemId();
		    AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) event.getTargetDetails();
		    final AbstractDomainObject targetItemId = (AbstractDomainObject) dropData.getItemIdOver();

		    // No move if source and target are the same, or there is no
		    // target
		    if (sourceItemId == targetItemId || targetItemId == null) {
			return;
		    }

		    if (!DirNode.class.isAssignableFrom(targetItemId.getClass())) {
			return;
		    }

		    final AbstractFileNode sourceNode = (AbstractFileNode) sourceItemId;
		    final DirNode targetDirNode = (DirNode) targetItemId;

		    if (!targetDirNode.isWriteGroupMember()) {
			return;
		    }
		    move(sourceNode, targetDirNode);
		}

		@Override
		public AcceptCriterion getAcceptCriterion() {
		    // return new And(new SourceIs(DocumentTable.this),

		    return new ServerSideCriterion() {

			@Override
			public boolean accept(DragAndDropEvent dragEvent) {
			    AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) dragEvent.getTargetDetails();
			    final AbstractDomainObject targetItemId = (AbstractDomainObject) dropData.getItemIdOver();

			    if (targetItemId == null) {
				return false;
			    }

			    if (!DirNode.class.isAssignableFrom(targetItemId.getClass())) {
				return false;
			    }

			    DirNode targetDirNode = (DirNode) targetItemId;

			    if (!targetDirNode.isWriteGroupMember()) {
				return false;
			    }
			    return true;
			}
		    };
		}

	    });

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
		    refreshPageLength();
		}

	    });

	}

	public void move(final AbstractFileNode sourceNode, final DirNode targetDirNode) {
	    final String sourceDisplayName = sourceNode.getDisplayName();
	    final String targetDisplayName = targetDirNode.getDisplayName();
	    String message;

	    if (!sourceNode.hasSameReadWriteGroup(targetDirNode)) {
		message = String.format("Ao mover %s para %s a partilha será alterada de :\n\t%s\npara\n\t%s Deseja mover?",
			sourceDisplayName, targetDisplayName, sourceNode.getVisibilityGroups().getDescription(), targetDirNode
				.getVisibilityGroups().getDescription());
	    } else {
		message = String.format("Deseja mover %s para a pasta %s ?", sourceDisplayName, targetDisplayName);
	    }
	    final ConfirmDialog dialog = ConfirmDialog.getFactory().create("Confirmar", message, "Sim", "Não");
	    dialog.setContentMode(ConfirmDialog.CONTENT_HTML);
	    dialog.show(getWindow(), new ConfirmDialog.Listener() {

		@Override
		public void onClose(ConfirmDialog dialog) {
		    if (dialog.isConfirmed()) {
			try {
			    final Boolean wasMoved = sourceNode.moveTo(targetDirNode);
			    if (wasMoved) {
				removeItem(sourceNode);
			    }
			} catch (NodeDuplicateNameException ndne) {
			    getWindow().showNotification("Operação Inválida", ndne.getMessage(), Notification.TYPE_ERROR_MESSAGE);
			}
		    }
		}
	    }, true);

	}

	@Override
	public void itemClick(final ItemClickEvent event) {

	    AbstractFileNode abstractFileNode = getNodeFromItemId(event.getItemId());

	    if (event.isDoubleClick()) {
		if (abstractFileNode.isDir()) {
		    open((DirNode) abstractFileNode);
		    // nodeItem.setValue(abstractFileNode);
		    // changeTable();
		    // documentMenu.addDirNode((DirNode) abstractFileNode);
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
		    // removeAllItemsAfter(selectedItem);
		    // nodeItem.setValue(dirNode);
		    open(dirNode);
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

    private void refreshPageLength() {
	final int size = documentTable.getContainerDataSource().size();
	if (size < MIN) {
	    documentTable.setPageLength(MIN);
	} else if (size >= MIN && size <= MAX) {
	    documentTable.setPageLength(0);
	} else if (size > MAX) {
	    documentTable.setPageLength(MAX);
	}
    }

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

    public DomainContainer<AbstractFileNode> getNodes() {
	if (nodeItem.getValue() != null) {
	    return (DomainContainer<AbstractFileNode>) nodeItem.getItemProperty("child");
	}
	return items;
    }

    private static void addIconToItems(DomainContainer<? extends AbstractFileNode> items) {
	for (Object node : items.getItemIds()) {
	    final DomainItem<? extends AbstractFileNode> item = items.getItem(node);
	    item.addItemProperty("icon", new ObjectProperty<Resource>(getThemeResource((AbstractFileNode) node)));
	}
	items.addContainerProperty("icon", Resource.class, defaultFileIcon);
    }

    public void setNodes(Set<AbstractFileNode> nodes) {
	if (items == null) {
	    items = new DomainContainer<AbstractFileNode>(nodes, AbstractFileNode.class);
	} else {
	    items.removeAllItems();
	    items.addItemBatch(nodes);
	}
	changeTable();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void valueChange(ValueChangeEvent event) {
	changeTable();
    }

    public static ThemeResource getThemeResourceForDir() {
	return new ThemeResource("../../../images/fileManagement/folder1_16x16.gif");
    }

    public static ThemeResource getThemeResource(AbstractFileNode abstractFileNode) {
	final String iconFile;
	if (abstractFileNode.isDir()) {
	    return getThemeResourceForDir();
	} else {
	    iconFile = getIconFile(((FileNode) abstractFileNode).getDocument().getLastVersionedFile().getFilename());
	}
	final String fileName = "../../../images/fileManagement/" + iconFile;
	return new ThemeResource(fileName);
    }

    private static String getIconFile(final String filename) {
	final int lastDot = filename.lastIndexOf('.');
	final String fileSuffix = lastDot < 0 ? "file" : filename.substring(lastDot + 1);
	String ext = StringUtils.lowerCase(fileSuffix);
	if (!KNOWN_FILE_EXTENSIONS.contains(ext)) {
	    ext = "file";
	}
	return "fileicons/" + ext + ".png";
    }

    private void changeTable() {
	final DomainContainer<AbstractFileNode> childs = getNodes();
	addIconToItems(childs);
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
	childs.setContainerProperties("displayName");
	childs.setItemSorter(new ItemSorter() {
	    boolean asc = true;

	    @Override
	    public void setSortProperties(final Sortable container, final Object[] propertyId, final boolean[] ascending) {
		// System.out.printf("cont: %s, props : %s, ascs : %s\n",
		// container, Arrays.toString(propertyId),
		// Arrays.toString(ascending));
		for (boolean b : ascending) {
		    if (b != asc) {
			asc = b;
			break;
		    }
		}
	    }

	    @Override
	    public int compare(final Object itemId1, final Object itemId2) {

		final AbstractFileNode node1 = getNodeFromItemId(itemId1);
		final AbstractFileNode node2 = getNodeFromItemId(itemId2);

		return asc ? node1.compareTo(node2) : node2.compareTo(node1);
	    }

	});
	documentTable.setContainerDataSource(childs);
	documentTable.setVisibleColumns(new String[] { "displayName", "visibilidade" });
	// documentTable.setSortContainerPropertyId("displayName");
	documentTable.setColumnHeaders(new String[] { "Nome", "Visibilidade" });
	documentTable.setColumnAlignment("visibilidade", Table.ALIGN_RIGHT);
	documentTable.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
	documentTable.setItemIconPropertyId("icon");
	documentTable.setColumnExpandRatio("icon", 0.15f);
	documentTable.setColumnExpandRatio("displayName", 0.60f);
	documentTable.setColumnExpandRatio("visibilidade", 0.25f);
	documentTable.sort(new Object[] { "displayName" }, new boolean[] { Boolean.TRUE });
	refreshPageLength();
    }

    public String getNodeName(final AbstractFileNode node) {
	final User user = Authenticate.getCurrentUser();
	final DirNode fileRepository = FileRepository.getOrCreateFileRepository(user);
	if (fileRepository == node) {
	    return getMessage("label.menu.home");
	} else {
	    return node.getDisplayName();
	}
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

    private void setValue(DirNode dirNode) {
	if (nodeItem == null) {
	    nodeItem = new DomainItem<AbstractFileNode>(dirNode);
	    nodeItem.addListener(this);
	} else {
	    nodeItem.setValue(dirNode);
	}
    }

    public DocumentFileBrowser() {
	nodeItem = new DomainItem<AbstractFileNode>(DirNode.class);
	nodeItem.addListener(this);
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

    public void setContextPath(ContextPath contextPath) {
	final DirNode lastDirNode = contextPath.getLastDirNode();
	if (lastLineOfDefense(lastDirNode)) {
	    documentMenu.setDirNodes(contextPath);
	    setValue(lastDirNode);
	} else {
	    accessDenied = true;
	}
    }

    public void setContextPath(String pathString) {
	final ContextPath contextPath = new ContextPath(pathString);
	setContextPath(contextPath);
    }

    private boolean lastLineOfDefense(DirNode lastDirNode) {
	return lastDirNode.isAccessible() && !lastDirNode.hasRootDirNode()
		|| Role.getRole(RoleType.MANAGER).isMember(Authenticate.getCurrentUser());
    }

    public DocumentMenu getDocumentMenu() {
	return documentMenu;
    }

    public ContextPath getContextPath() {
	return documentMenu.getContextPath();
    }

}
