package module.fileManagement.presentationTier.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.presentationTier.component.viewers.VisibilityListViewer;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import pt.ist.vaadinframework.data.VBoxProperty;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

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

@SuppressWarnings("serial")
public class DocumentFileBrowser extends CustomComponent implements EmbeddedComponentContainer, ValueChangeNotifier, ValueChangeListener {

    public Component getCurrent() {
	return this;
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
	    setColumnCollapsingAllowed(true);

	    final ItemClickListener itemClickListener = this;
	    addListener(itemClickListener);
	    addGeneratedColumn("cenas", new ColumnGenerator() {
	        
	        @Override
	        public Component generateCell(Table source, Object itemId, Object columnId) {
	        	VisibilityListViewer viewer = new VisibilityListViewer();
	        	final DomainItem<AbstractFileNode> fileNode = (DomainItem<AbstractFileNode>) source.getItem(itemId);
	        	final Property itemProperty = fileNode.getItemProperty("visibilityGroups");
	        	viewer.setPropertyDataSource(itemProperty);
	        	return viewer;
	        }
	    });
	}
	
	@Override
	public void itemClick(final ItemClickEvent event) {

	    AbstractFileNode abstractFileNode = getNodeFromItemId(event.getItemId());

	    if (event.isDoubleClick()) {
		if (abstractFileNode.isDir()) {
		    nodeItem.setValue(abstractFileNode);
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
	
	public void refresh() {
	    removeItems();
	    addDirNode((DirNode) nodeItem.getValue());
	}

	private void addDirNode(final DirNode dirNode) {
	    if (dirNode != null) {
		final DirNode parent = dirNode.getParent();
		addDirNode(parent);

		final String label = getNodeName(dirNode);
		addItem(label + "  »", new Command() {
		    @Override
		    public void menuSelected(final MenuItem selectedItem) {
			nodeItem.setValue(dirNode);
		    }
		});
	    }
	}

    }

    private final DocumentTable documentTable = new DocumentTable();
    private final DocumentMenu documentMenu = new DocumentMenu();

    private DomainItem<AbstractFileNode> nodeItem;

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

    
    public DirNode getInitialDirNode() {
	return FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void valueChange(ValueChangeEvent event) {
//	DomainItem<AbstractFileNode> item = (DomainItem<AbstractFileNode>) event.getProperty();
	DomainItem<AbstractFileNode> item = getNodeItem();
	final DomainContainer<AbstractFileNode> childs = (DomainContainer<AbstractFileNode>) item.getItemProperty("child");
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
	documentTable.setVisibleColumns(new String[] { "displayName", "cenas" });
	documentTable.setSortContainerPropertyId("displayName");
	documentTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	documentTable.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
	documentTable.setItemIconPropertyId("icon");
	documentTable.setColumnReorderingAllowed(false);
	documentTable.sort();
//	this.dirNode.setValue(dirNode);
	documentMenu.refresh();
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
	setValue(getInitialDirNode());
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
	    node = (AbstractFileNode) ((VBoxProperty)itemId).getValue();
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
    
}