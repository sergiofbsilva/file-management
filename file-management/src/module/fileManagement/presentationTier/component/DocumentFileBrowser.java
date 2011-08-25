package module.fileManagement.presentationTier.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.VersionedFile;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DocumentFileBrowser extends CustomComponent implements EmbeddedComponentContainer, ValueChangeNotifier {

    public Component getCurrent() {
	return this;
    }

    private static class PersistentGroupHolder implements HasPersistentGroup {

	private final PersistentGroup persistentGroup;

	private PersistentGroupHolder(final PersistentGroup persistentGroup) {
	    this.persistentGroup = persistentGroup;
	}

	@Override
	public PersistentGroup getPersistentGroup() {
	    return persistentGroup;
	}

	@Override
	public void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout) {
	    // nothing to do, we already have a group :o)
	}

    }

    private static class PersistentGroupCreator implements HasPersistentGroupCreator {

	@Override
	public HasPersistentGroup createGroupFor(final Object itemId) {
	    if (itemId != null && itemId instanceof String) {
		final String externalId = (String) itemId;
		final DomainObject domainObject = AbstractDomainObject.fromExternalId(externalId);
		if (domainObject != null && domainObject instanceof PersistentGroup) {
		    final PersistentGroup persistentGroup = (PersistentGroup) domainObject;
		    return new PersistentGroupHolder(persistentGroup);
		}
	    }
	    return null;
	}

	@Override
	public void addItems(final ComboBox comboBox, final String displayItemProperty) {
	    for (final PersistentGroup persistentGroup : MyOrg.getInstance().getSystemGroupsSet()) {
		final String externalId = persistentGroup.getExternalId();
		final String name = persistentGroup.getName();

		final Item comboItem = comboBox.addItem(externalId);
		comboItem.getItemProperty(displayItemProperty).setValue(name);
	    }
	}

    }

    private static final Set<HasPersistentGroupCreator> persistentGroupCreators = new HashSet<HasPersistentGroupCreator>();

    static {
	registerPersistentGroupCreator(new PersistentGroupCreator());
    }

    public static synchronized void registerPersistentGroupCreator(final HasPersistentGroupCreator hasPersistentGroupCreator) {
	persistentGroupCreators.add(hasPersistentGroupCreator);
    }

    protected String getMessage(final String key, String... args) {
	return FileManagementSystem.getMessage(key, args);
    }

    public class DocumentTable extends Table implements Table.ValueChangeListener, ItemClickListener {
	
	public java.util.Collection<?> getSortableContainerPropertyIds() {return Collections.EMPTY_SET;};
	
	DocumentTable() {
	    super();
	    setSizeFull();
	    setPageLength(25);

	    setSelectable(true);
	    setMultiSelect(false);
	    setImmediate(true);
	    setSortDisabled(false);
	    setColumnReorderingAllowed(true);
	    setColumnCollapsingAllowed(true);

//	    addContainerProperty("displayName", String.class, null, false, "label.file.displayName");
//	    addContainerProperty("visibility", String.class, null, false, "label.file.visibility");
	    // addContainerProperty("filesize", String.class, null, true,
	    // "label.file.size");
//	    addContainerProperty("icon", Resource.class, null, true, null);
	    
	    // setColumnAlignment("filesize", Table.ALIGN_RIGHT);

	    // setColumnExpandRatio("displayName", 1);

//	    final IndexedContainer indexedContainer = (IndexedContainer) getContainerDataSource();
//	    indexedContainer.setItemSorter(new ItemSorter() {
//
//		@Override
//		public void setSortProperties(final Sortable container, final Object[] propertyId, final boolean[] ascending) {
//		}
//
//		@Override
//		public int compare(final Object itemId1, final Object itemId2) {
//		   
//		    final AbstractFileNode node1 = (AbstractFileNode) itemId1;
//		    final AbstractFileNode node2 = (AbstractFileNode) itemId2;
//
//		    return node1.compareTo(node2);
//		}
//
//	    });

//	    final Table.ValueChangeListener valueChangeListener = this;
//	    addListener(valueChangeListener);

	    final ItemClickListener itemClickListener = this;
	    addListener(itemClickListener);
	}

	@Override
	public void attach() {
	    super.attach();

//	    if (dirNode != null) {
//		for (final AbstractFileNode abstractFileNode : dirNode.getChildSet()) {
//		    if (abstractFileNode.isAccessible()) {
//			addAbstractFileNode(abstractFileNode);
//		    }
//		}
//	    }
	}

	@Override
	public void detach() {
	    super.detach();
	    removeAllItems();
	}

//	public void addContainerProperty(final Object propertyId, Class<?> type, final Object defaultValue,
//		final boolean collapseColumn, final String headerKey) throws UnsupportedOperationException {
//	    addContainerProperty(propertyId, type, null);
//	    setColumnCollapsed(propertyId, collapseColumn);
//	    if (headerKey != null) {
//		setColumnHeader(propertyId, getMessage(headerKey));
//	    }
//	}

//	public void addAbstractFileNode(final AbstractFileNode abstractFileNode) {
//	    final Item item = addItem(abstractFileNode);
//
//	    final String displayName = abstractFileNode.getDisplayName();
//	    item.getItemProperty("displayName").setValue(displayName);
//
//	    final String visibility = abstractFileNode.getVisibility();
//	    item.getItemProperty("visibility").setValue(visibility);
//
//	    item.getItemProperty("icon").setValue(getThemeResource(abstractFileNode));
//
//	    sort(new Object[] { "displayName" }, new boolean[] { true });
//	}

//	@Override
//	public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
//	    if (event instanceof ValueChangeEvent) {
//		final AbstractFileNode selectedNode = (AbstractFileNode) event.getProperty().getValue();
//		if (selectedNode != null) {
//		    changeSelectedNode(selectedNode);
//		}
//	    }
//	}

	@Override
	public void itemClick(final ItemClickEvent event) {

	    AbstractFileNode abstractFileNode = ((AbstractFileNode) event.getItemId());

	    if (event.isDoubleClick()) {
		if (abstractFileNode.isDir()) {
		    final DirNode dirNode = (DirNode) abstractFileNode;
		    changeDir(dirNode);
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

    private class DocumentMenu extends MenuBar {

	DocumentMenu() {
	    setWidth(100, Sizeable.UNITS_PERCENTAGE);
	}

	@Override
	public void attach() {
	    super.attach();
	    addDirNode(dirNode);
	}

	@Override
	public void detach() {
	    super.detach();
	    removeItems();
	}
	
	public void refresh() {
	    removeItems();
	    addDirNode(dirNode);
	}

	private void addDirNode(final DirNode dirNode) {
	    if (dirNode != null) {
		final DirNode parent = dirNode.getParent();
		addDirNode(parent);

		final String label = getNodeName(dirNode);
		addItem(label + "  »", new Command() {
		    @Override
		    public void menuSelected(final MenuItem selectedItem) {
			changeDir(dirNode);
		    }
		});
	    }
	}

    }

    private final DocumentTable documentTable = new DocumentTable();
    private final DocumentMenu documentMenu = new DocumentMenu();

    private DirNode dirNode = null;

    @Override
    public void setArguments(final String... arg0) {
    }

    @Override
    public void attach() {
	super.attach();
//	outerWindow = getWindow();
	final AbstractLayout layout = new VerticalLayout();
	layout.setSizeFull();
	documentMenu.setSizeFull();
	documentTable.setSizeFull();
	layout.addComponent(documentMenu);
	layout.addComponent(documentTable);
	setCompositionRoot(layout);
    }

    protected DirNode getInitialDirNode() {
	return FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
    }

    private void changeDir(final DirNode dirNode) {
	if (dirNode != null && !dirNode.equals(this.dirNode)) {
//	    DomainItem<AbstractFileNode> item = new DomainItem<AbstractFileNode>(dirNode) {
//		@Override
//		public Property getItemProperty(Object propertyId) {
//		    if ("icon".equals(propertyId)) {
//			return new ObjectProperty<Resource>(getThemeResource(getValue()));
//		    }
//		    return super.getItemProperty(propertyId);
//		}
//	    };
	    DomainItem<AbstractFileNode> item = new DomainItem<AbstractFileNode>(dirNode);
	    final DomainContainer<AbstractFileNode> childs = (DomainContainer<AbstractFileNode>)item.getItemProperty("child");
	    childs.setContainerProperties("displayName","visibility","icon");
	    childs.setItemSorter(new ItemSorter() {
		
		@Override
		public void setSortProperties(final Sortable container, final Object[] propertyId, final boolean[] ascending) {
		}

		@Override
		public int compare(final Object itemId1, final Object itemId2) {
		   
		    final AbstractFileNode node1 = (AbstractFileNode) itemId1;
		    final AbstractFileNode node2 = (AbstractFileNode) itemId2;

		    return node1.compareTo(node2);
		}

	    });
	    documentTable.setContainerDataSource(childs);
	    documentTable.setVisibleColumns(new String[] {"displayName", "visibility"});
	    documentTable.setSortContainerPropertyId("displayName");
	    documentTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	    documentTable.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
	    documentTable.setItemIconPropertyId("icon");
	    documentTable.setColumnReorderingAllowed(false);
	    documentTable.sort();
	    this.dirNode = dirNode;
	    documentMenu.refresh();
	}
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

    public DirNode getDirNode() {
	return dirNode;
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
    
    public DocumentFileBrowser() {
	changeDir(getInitialDirNode());
    }

}
