package module.fileManagement.presentationTier.component;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.DocumentSystem;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.VersionedFile;
import module.fileManagement.domain.VisibilityGroup;
import module.fileManagement.domain.VisibilityList;
import module.fileManagement.domain.VisibilityGroup.VisibilityOperation;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.groups.UnitGroup;
import module.organizationIst.domain.IstAccountabilityType;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.SingleUserGroup;

import org.vaadin.easyuploads.DirectoryFileFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import vaadin.annotation.EmbeddedComponent;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "DocumentFrontPage-(.*)" })
public class DocumentFrontPage extends CustomComponent implements EmbeddedComponentContainer {

    protected String getMessage(final String key, String... args) {
	return DocumentSystem.getMessage(key, args);
    }

    private class DirNodeLink extends Button implements ClickListener {

	private final DirNode linkDirNode;

	public DirNodeLink(final DirNode dirNode, final String caption) {
	    super(caption);
	    setStyleName(BaseTheme.BUTTON_LINK);
	    this.linkDirNode = dirNode;
	    addListener((ClickListener) this);
	}

	public DirNodeLink(final DirNode dirNode) {
	    this(dirNode, dirNode.getDisplayName());
	}

	@Override
	public void buttonClick(final ClickEvent event) {
	    changeDir(linkDirNode);
	}
	
    }

    private class FileNodeLink extends Button implements ClickListener {

	private final FileNode fileNode;

	public FileNodeLink(final FileNode fileNode) {
	    super(fileNode.getDisplayName());
	    setStyleName(BaseTheme.BUTTON_LINK);
	    this.fileNode = fileNode;
	    addListener((ClickListener) this);
	}

	@Override
	public void buttonClick(final ClickEvent event) {
	    final Document document = fileNode.getDocument();
	    final VersionedFile file = document.getLastVersionedFile();
	    final String filename = file.getFilename();

	    final FileNodeStreamSource streamSource = new FileNodeStreamSource(fileNode);
	    final StreamResource resource = new StreamResource(streamSource, filename, getApplication());
	    resource.setMIMEType(file.getContentType());
	    resource.setCacheTime(0);

	    getWindow().open(resource);
	}
	
    }

    private class DocumentTable extends Table implements Table.ValueChangeListener, ItemClickListener {

	private class NodeTextField extends TextField {

	    private NodeTextField(final AbstractFileNode abstractFileNode) {
		setValue(abstractFileNode.getDisplayName());
		setReadOnly(true);
	    }

	}

	DocumentTable() {
	    setSizeFull();
	    setPageLength(0);

	    setSelectable(true);
	    setMultiSelect(false);
	    setImmediate(true);
	    setSortDisabled(false);
	    setColumnReorderingAllowed(true);
	    setColumnCollapsingAllowed(true);

	    addContainerProperty("displayName", TextField.class, null, false, "label.file.displayName");
	    addContainerProperty("visibility", String.class, null, false, "label.file.visibility");
	    addContainerProperty("filesize", String.class, null, true, "label.file.size");
	    addContainerProperty("icon", Resource.class, null, true, null);

	    setColumnExpandRatio("displayName", 1);

	    setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
	    setItemIconPropertyId("icon");

	    final IndexedContainer indexedContainer = (IndexedContainer) getContainerDataSource();
	    indexedContainer.setItemSorter(new ItemSorter() {
	        
	        @Override
	        public void setSortProperties(final Sortable container, final Object[] propertyId, final boolean[] ascending) {
	        }
	        
	        @Override
	        public int compare(final Object itemId1, final Object itemId2) {
	            final AbstractFileNode node1 = AbstractDomainObject.fromExternalId((String) itemId1);
	            final AbstractFileNode node2 = AbstractDomainObject.fromExternalId((String) itemId2);
	            return node1.compareTo(node2);
	        }

	    });

	    final Table.ValueChangeListener valueChangeListener = this;
	    addListener(valueChangeListener);

	    final ItemClickListener itemClickListener = this;
	    addListener(itemClickListener);
	}

	@Override
	public void attach() {
	    super.attach();

	    if (dirNode != null) {
		for (final AbstractFileNode abstractFileNode : dirNode.getChildSet()) {
		    if (abstractFileNode.isAccessible()) {
		    	addAbstractFileNode(abstractFileNode);
		    }
		}
	    }
	}

	@Override
	public void detach() {
	    super.detach();
	    removeAllItems();
	}

	public void addContainerProperty(final Object propertyId, Class<?> type, final Object defaultValue,
		final boolean collapseColumn, final String headerKey) throws UnsupportedOperationException {
	    addContainerProperty(propertyId, type, null);
	    setColumnCollapsed(propertyId, collapseColumn);
	    if (headerKey != null) {
		setColumnHeader(propertyId, getMessage(headerKey));
	    }
	}

	public void addAbstractFileNode(final AbstractFileNode abstractFileNode) {
	    final String oid = abstractFileNode.getExternalId();
	    final Item item = addItem(oid);

	    final TextField displayNameLink = new NodeTextField(abstractFileNode);
	    item.getItemProperty("displayName").setValue(displayNameLink);

	    final String visibility = abstractFileNode.getVisibility();
	    item.getItemProperty("visibility").setValue(visibility);

	    final String filesize = abstractFileNode.getPresentationFilesize();
	    item.getItemProperty("filesize").setValue(filesize);

	    item.getItemProperty("icon").setValue(getThemeResource(abstractFileNode));

	    sort(new Object[] { "displayName" }, new boolean[] { true });
	}

	@Override
	public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
	    if (event instanceof ValueChangeEvent) {
		final Object selection = event.getProperty().getValue();
		if (selection != null && selection instanceof String) {
		    final String externalId = (String) selection;
		    final AbstractFileNode abstractFileNode = null != externalId && !externalId.isEmpty() ?
			    (AbstractFileNode) AbstractDomainObject.fromExternalId(externalId) : null;
		    changeSelectedNode(abstractFileNode);
		} else {
		    changeSelectedNode(dirNode);
		}
	    }
	}

	@Override
	public void itemClick(final ItemClickEvent event) {
	    final String oid = (String) event.getItemId();
	    final AbstractFileNode abstractFileNode = AbstractDomainObject.fromExternalId(oid);

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
	    } else {
//		changeSelectedNode(abstractFileNode);
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

    private class SearchDocuments extends TextField {

	SearchDocuments() {
	    setWidth(45, Sizeable.UNITS_PERCENTAGE);

	    setInputPrompt(getMessage("label.search.documents.input.prompt"));

	    setTextChangeEventMode(TextChangeEventMode.LAZY);
	    setTextChangeTimeout(200);
	}

    }

    private class UploadFileArea extends MultiFileUpload {

	UploadFileArea() {
	    final DirectoryFileFactory directoryFileFactory = new DirectoryFileFactory(new File("/tmp"));
	    setFileFactory(directoryFileFactory);
	    setWidth(75, UNITS_PERCENTAGE);
	    setMargin(true);
	}

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
	    final DirNode destination = dirNode == null || !dirNode.isWriteGroupMember() ?
		    UserView.getCurrentUser().getFileRepository() : dirNode;
	    if (destination != null) {
		final FileNode fileNode = destination.createFile(file, fileName);
		if (destination == dirNode) {
		    documentTable.addAbstractFileNode(fileNode);
		}
	    }
	}

    }

    private class IconMenu extends GridLayout {

	private class HomeIconDirNodeLink extends DirNodeLink {

	    public HomeIconDirNodeLink() {
		super(UserView.getCurrentUser().getFileRepository(), null);
		final ThemeResource icon = new ThemeResource("../../../images/fileManagement/HomeBlack32.png");
		setIcon(icon);
		setDescription(getMessage("label.menu.home"));
	    }
	    
	}

	private class NewDirNodeLink extends Button implements ClickListener {

	    private class AddDirWindow extends Window {

		private class CloseButton extends Button implements ClickListener {

		    CloseButton(final String labelKey) {
			super(getMessage(labelKey));
			ClickListener clickListener = this;
			addListener(clickListener);
		    }

		    CloseButton() {
			this("label.close");
		    }

		    @Override
		    public void buttonClick(final ClickEvent event) {
			outerWindow.removeWindow(addDirWindow);
		    }
		}

		private class SaveButton extends CloseButton implements ClickListener {

		    SaveButton() {
			super("label.save");
		    }

		    @Override
		    public void buttonClick(final ClickEvent event) {
			final DirNode newDirNode = dirNode.createDir((String) editor.getValue());
			documentTable.addAbstractFileNode(newDirNode);
			super.buttonClick(event);
		    }
		}

		private final AddDirWindow addDirWindow;
		private final TextField editor = new TextField();

		AddDirWindow() {
		    super(getMessage("label.file.repository.add.dir"));
		    addDirWindow = this;
		    setModal(true);
		    setWidth(50, UNITS_PERCENTAGE);

		    final VerticalLayout layout = (VerticalLayout) getContent();
		    layout.setMargin(true);
		    layout.setSpacing(true);

		    editor.setWidth(100, UNITS_PERCENTAGE);
		    addComponent(editor);

		    final HorizontalLayout horizontalLayout = new HorizontalLayout();
		    layout.addComponent(horizontalLayout);
		    horizontalLayout.addComponent(new SaveButton());
		    horizontalLayout.addComponent(new CloseButton());
		}

	    }

	    public NewDirNodeLink() {
		super();
		setStyleName(BaseTheme.BUTTON_LINK);
		final ThemeResource icon = new ThemeResource("../../../images/fileManagement/AddFolderBlack32.png");
		setIcon(icon);
		addListener((ClickListener) this);
		setDescription(getMessage("label.menu.new.folder"));
	    }

	    @Override
	    public void buttonClick(final ClickEvent event) {
		final AddDirWindow addDirWindow = new AddDirWindow();
		outerWindow.addWindow(addDirWindow);
	    }
	}

	private class IconLink extends Button implements ClickListener {

	    public IconLink(final String description, final String iconFile) {
		super();
		setStyleName(BaseTheme.BUTTON_LINK);
		addListener((ClickListener) this);
		final ThemeResource icon = new ThemeResource("../../../images/fileManagement/" + iconFile);
		setIcon(icon);
		setDescription(description);
	    }

	    @Override
	    public void buttonClick(final ClickEvent event) {
		outerWindow.showNotification("Esta funcionalidade ainda não foi implementada.");
	    }	    
	}

	public IconMenu() {
	    super(5, 1);
//	    setSizeFull();
	    setWidth(100, UNITS_PERCENTAGE);
	    setSpacing(true);
	    setMargin(true);
	}

	public void addComponent(final Component component, int column) {
	    final Panel panel = new Panel();
	    panel.setSizeFull();
//	    panel.setStyleName(Reindeer.PANEL_LIGHT);
	    panel.addComponent(component);
	    super.addComponent(panel, column, 0);
	    setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
	    final VerticalLayout layout = (VerticalLayout) panel.getContent();
	    layout.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
	}

	@Override
	public void attach() {
	    super.attach();

	    outerWindow = getWindow();

	    addComponent(new HomeIconDirNodeLink(), 0);
	    addComponent(new NewDirNodeLink(), 1);
	    addComponent(new IconLink("Search", "SearchesBlack32.png"), 2);
	    addComponent(new IconLink(getMessage("label.menu.shared.documents"), "FolderContactsBlack32.png"), 3);
	    addComponent(new IconLink("Trash", "TrashBlack32.png"), 4);

/*
	    setColumnExpandRatio(0, 1f);
	    setColumnExpandRatio(1, 1.25f);
	    setColumnExpandRatio(2, 1f);
	    setColumnExpandRatio(3, 1.75f);
	    setColumnExpandRatio(4, 1f);
*/
/*
	    addComponent("label.menu.home", "HomeBlack.png"));
	    addComponent("label.menu.new.folder", "HomeBlack.png"));
	    addComponent("label.menu.shared.documents", "HomeBlack.png"));
	    //addComponent("Search", "HomeBlack.png"));
	    //addComponent("Trash", "HomeBlack.png"));

	        final Panel searchPanel = new Panel();
	        searchPanel.setSizeFull();
	        searchPanel.setStyleName(Reindeer.PANEL_LIGHT);
	        gridLayout.addComponent(searchPanel, 0, 0, 0, 0);
	        gridLayout.setComponentAlignment(searchPanel, Alignment.MIDDLE_LEFT);
	        searchPanel.addComponent(searchDocuments);

	        final Panel uploadPanel = new Panel(new HorizontalLayout());
	        uploadPanel.setSizeFull();
	        uploadPanel.setStyleName(Reindeer.PANEL_LIGHT);
	        final HorizontalLayout uploadPanelLayout = (HorizontalLayout) uploadPanel.getContent();
	        uploadPanelLayout.setSizeFull();
	        gridLayout.addComponent(uploadPanel, 1, 0, 1, 0);
	        gridLayout.setComponentAlignment(uploadPanel, Alignment.MIDDLE_CENTER);
	        uploadPanel.addComponent(uploadFileArea);

	        final Panel breadCrumbPanel = new Panel();
	        breadCrumbPanel.setStyleName(Reindeer.PANEL_LIGHT);
	        gridLayout.addComponent(breadCrumbPanel, 0, 1, 1, 1);
	        gridLayout.setComponentAlignment(breadCrumbPanel, Alignment.MIDDLE_LEFT);
	        breadCrumbPanel.addComponent(new Label("TODO"));
*/
	}

    }

    private class AbstractNodeInfoGrid extends GridLayout {

	private class VisibilityPanel extends Panel {
	    
	    private class ChangeVisibilityButton extends Button implements ClickListener {

		private class ChangeVisibilityWindow extends Window {

		    private class CloseButton extends Button implements ClickListener {

			private CloseButton(final String labelKey) {
			    super(getMessage(labelKey));
			    final ClickListener clickListener = this;
			    addListener(clickListener);
			}

			private CloseButton() {
			    this("label.close");
			}

			@Override
			public void buttonClick(final ClickEvent event) {
			    outerWindow.removeWindow(changeVisibilityWindow);
			}
		    }

		    private class SaveButton extends CloseButton implements ClickListener {

			private SaveButton() {
			    super("label.save");
			}

			@Override
			public void buttonClick(final ClickEvent event) {
			    selectedNode.setVisibility(visibilityGroups);
			    super.buttonClick(event);
			    abstractNodeInfoGrid.detach();
			    abstractNodeInfoGrid.attach();
			}
		    }

		    private class VisibilityGroupLayout extends HorizontalLayout implements ValueChangeListener {

			private class VisibilityOperationSelect extends NativeSelect {

			    private VisibilityOperationSelect() {
				addContainerProperty("description", String.class, null);
				for (final VisibilityOperation operation : VisibilityOperation.values()) {
				    final Item item = addItem(operation);
				    item.getItemProperty("description").setValue(operation.getDescription());
				}
				setNullSelectionAllowed(false);
				setValue(visibilityGroup.visibilityOperation);
				setImmediate(true);
				setItemCaptionMode(ITEM_CAPTION_MODE_PROPERTY);
				setItemCaptionPropertyId("description");
			    }

			}

			private class RemoveVisibilityGroup extends Button implements ClickListener {

			    private final VisibilityGroupLayout visibilityGroupLayout;
		    
			    private RemoveVisibilityGroup(final VisibilityGroupLayout visibilityGroupLayout) {
				this.visibilityGroupLayout = visibilityGroupLayout;
				setStyleName(BaseTheme.BUTTON_LINK);
				setIcon(new ThemeResource("../runo/icons/16/cancel.png"));
				final ClickListener clickListener = this;
				addListener(clickListener);
			    }

			    @Override
			    public void buttonClick(ClickEvent event) {
				visibilityGroups.remove(visibilityGroup);
				final VerticalLayout layout = (VerticalLayout) changeVisibilityWindow.getContent();
				layout.removeComponent(visibilityGroupLayout);
			    }
			}

			private final VisibilityGroup visibilityGroup;

			private VisibilityGroupLayout(final VisibilityGroup visibilityGroup) {
			    this.visibilityGroup = visibilityGroup;
			    setSpacing(true);
			}

			@Override
			public void attach() {
			    super.attach();

			    final VisibilityGroupLayout visibilityGroupLayout = this;

			    final RemoveVisibilityGroup removeVisibilityGroup = new RemoveVisibilityGroup(visibilityGroupLayout);
			    addComponent(removeVisibilityGroup);
			    setComponentAlignment(removeVisibilityGroup, Alignment.MIDDLE_CENTER);

			    final Label label = new Label(visibilityGroup.persistentGroup.getName());
			    addComponent(label);
			    setComponentAlignment(label, Alignment.MIDDLE_CENTER);

			    final VisibilityOperationSelect visibilityOperationSelect = new VisibilityOperationSelect();
			    visibilityOperationSelect.addListener(visibilityGroupLayout);
			    addComponent(visibilityOperationSelect);
			    setComponentAlignment(visibilityOperationSelect, Alignment.MIDDLE_CENTER);
			}

			@Override
			public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
			    final VisibilityOperation operation = (VisibilityOperation) event.getProperty().getValue();
			    visibilityGroup.setVisibilityOperation(operation);
			}
		    }

		    private class AddGroupButton extends Button implements ClickListener {

			private class AddGroupWindow extends Window {

			    private class AddGroupCloseButton extends Button implements ClickListener {

				private AddGroupCloseButton(final String labelKey) {
				    super(getMessage(labelKey));
				    final ClickListener clickListener = this;
				    addListener(clickListener);
				}

				private AddGroupCloseButton() {
				    this("label.close");
				}

				@Override
				public void buttonClick(final ClickEvent event) {
				    outerWindow.removeWindow(addGroupWindow);
				}
			    }

			    private class AddGroupSaveButton extends AddGroupCloseButton implements ClickListener {

				private AddGroupSaveButton() {
				    super("label.save");
				}

				@Override
				public void buttonClick(final ClickEvent event) {
				    if (hasPersistentGroup != null) {
					final VisibilityGroup visibilityGroup = new VisibilityGroup(hasPersistentGroup.getPersistentGroup(), VisibilityOperation.READ);
				    	visibilityGroups.add(visibilityGroup);
				    	final VerticalLayout layout = (VerticalLayout) changeVisibilityWindow.getContent();
				    	final int index = layout.getComponentIndex(addGroupButton);
				    	layout.addComponent(new VisibilityGroupLayout(visibilityGroup), index);
				    }
				    super.buttonClick(event);
				}
			    }

			    private class GroupComboBox extends ComboBox implements Property.ValueChangeListener {

				private GroupComboBox() {
				    setWidth(400, UNITS_PIXELS);
				    setNullSelectionAllowed(false);
				    setImmediate(true);
				    addContainerProperty("name", String.class, null);
				    setItemCaptionPropertyId("name");
				    setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
				    setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
				    final ValueChangeListener valueChangeListener = this;
				    addListener(valueChangeListener);
				}

				@Override
				public void attach() {
				    super.attach();

				    for (final PersistentGroup persistentGroup : MyOrg.getInstance().getSystemGroupsSet()) {
					final String externalId = persistentGroup.getExternalId();
					final String name = persistentGroup.getName();

					final Item comboItem = addItem(externalId);
					comboItem.getItemProperty("name").setValue(name);
				    }

				    for (final Party party : MyOrg.getInstance().getPartiesSet()) {
					if (party.isUnit() || (party.isPerson() && ((Person) party).hasUser())) {
					    final String externalId = party.getExternalId();
					    final String name = party.getPresentationName();

					    final Item comboItem = addItem(externalId);
					    comboItem.getItemProperty("name").setValue(name);
					}
				    }
				}

				@Override
				public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
				    groupSpecificLayout.removeAllComponents();

				    if (event.getProperty().getValue() != null) {
					final String externalId = (String) event.getProperty().getValue();
					final DomainObject domainObject = AbstractDomainObject.fromExternalId(externalId);

					select(externalId);

					if (domainObject instanceof PersistentGroup) {
					    final PersistentGroup persistentGroup = (PersistentGroup) domainObject;
					    hasPersistentGroup = new HasPersistentGroup() {
					        @Override
					        public PersistentGroup getPersistentGroup() {
					            return persistentGroup;
					        }

						@Override
						public void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout) {
						}
					    };
					} else if (domainObject instanceof Person) {
					    final Person person = (Person) domainObject;
					    final User user = person.getUser();
					    final SingleUserGroup singleUserGroup = SingleUserGroup.getOrCreateGroup(user);
					    hasPersistentGroup = new HasPersistentGroup() {
					        @Override
					        public PersistentGroup getPersistentGroup() {
					            return singleUserGroup;
					        }

						@Override
						public void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout) {
						}
					    };
					} else if (domainObject instanceof Unit) {
					    final Unit unit = (Unit) domainObject;
					    hasPersistentGroup = new HasPersistentGroup() {
						private Set<AccountabilityType> selectedAccountabilityTypes = new HashSet<AccountabilityType>();
						private boolean includeSubUnits = false;

					        @Override
					        public PersistentGroup getPersistentGroup() {
				        	    final AccountabilityType[] memberTypes = selectedAccountabilityTypes.toArray(new AccountabilityType[0]);
				        	    final AccountabilityType[] childTypes = includeSubUnits ?
				        		    new AccountabilityType[] { IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType() } : null;
				        	    return UnitGroup.getOrCreateGroup(unit, memberTypes, childTypes);
					        }

						@Override
						public void renderGroupSpecificLayout(final AbstractOrderedLayout layout) {
						    final OptionGroup yesNoOptionGroup = new YesNoOptionGroup("label.include.child.unit.members", "label.yes", "label.no");
						    layout.addComponent(yesNoOptionGroup);
						    yesNoOptionGroup.addListener(new ValueChangeListener() {
							@Override
							public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
							    includeSubUnits = !includeSubUnits;
							}
						    });

						    final OptionGroup accountabilityTypeOptions = new MemberAccountabilityTypeOptionGroup();
						    layout.addComponent(accountabilityTypeOptions);

						    accountabilityTypeOptions.addListener(new ValueChangeListener() {
							@Override
							public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
							    if (event.getProperty().getValue() != null) {
								selectedAccountabilityTypes.clear();
								for (Object itemId : (Collection) event.getProperty().getValue()) {
								    final String accountabilityTypeOid = (String) itemId;
								    final AccountabilityType accountabilityType = AbstractDomainObject.fromExternalId(accountabilityTypeOid);
								    selectedAccountabilityTypes.add(accountabilityType);
								}
							    }
							}
						    });
						}
					    };
					} else {
					    throw new Error("unregistered.type: " + domainObject);
					}
					if (hasPersistentGroup != null) {
					    hasPersistentGroup.renderGroupSpecificLayout(groupSpecificLayout);
					}
				    }
				}

			    }

			    private final AddGroupWindow addGroupWindow;
			    private HasPersistentGroup hasPersistentGroup;
			    private AbstractOrderedLayout groupSpecificLayout = new VerticalLayout();

			    private AddGroupWindow() {
				super(getMessage("label.add.group"));
				addGroupWindow = this;
				setModal(true);
				setWidth(50, UNITS_PERCENTAGE);
				setSpacing(true);
			    }

			    @Override
			    public void attach() {
			        super.attach();

			        addComponent(new GroupComboBox());

			        addComponent(groupSpecificLayout);

			        final HorizontalLayout layout = new HorizontalLayout();
			        addComponent(layout);
			        layout.addComponent(new AddGroupSaveButton());
			        layout.addComponent(new AddGroupCloseButton());
			    }

			}

			private AddGroupButton() {
			    super(getMessage("label.add"));
			    setStyleName(BaseTheme.BUTTON_LINK);
			    final ClickListener clickListener = this;
			    addListener(clickListener);
			}

			@Override
			public void buttonClick(final ClickEvent event) {
			    outerWindow.addWindow(new AddGroupWindow());
			}
			
		    }

		    private final ChangeVisibilityWindow changeVisibilityWindow;
		    private final VisibilityList visibilityGroups = selectedNode.getVisibilityGroups();
		    private final AddGroupButton addGroupButton = new AddGroupButton();

		    private ChangeVisibilityWindow() {
			super(getMessage("label.file.visibility") + " - " + getSelectedNodeName());
			changeVisibilityWindow = this;
			setModal(true);
			setWidth(50, UNITS_PERCENTAGE);

			final VerticalLayout layout = (VerticalLayout) getContent();
			layout.setMargin(true);
			layout.setSpacing(true);

			for (final VisibilityGroup visibilityGroup : visibilityGroups) {
			    layout.addComponent(new VisibilityGroupLayout(visibilityGroup));
			}

			layout.addComponent(addGroupButton);

			final HorizontalLayout horizontalLayout = new HorizontalLayout();
			layout.addComponent(horizontalLayout);
			horizontalLayout.addComponent(new SaveButton());
			horizontalLayout.addComponent(new CloseButton());
		    }

		}

		private ChangeVisibilityButton() {
		    super(getMessage("label.edit"));
		    setStyleName(BaseTheme.BUTTON_LINK);
		    final ClickListener clickListener = this;
		    addListener(clickListener);
		}

		@Override
		public void buttonClick(final ClickEvent event) {
		    final ChangeVisibilityWindow changeVisibilityWindow = new ChangeVisibilityWindow();
		    outerWindow.addWindow(changeVisibilityWindow);
		}
	    }

	    private VisibilityPanel() {
		super(getMessage("label.file.visibility"));
		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.setSpacing(true);
		final Collection<VisibilityGroup> visibilityGroups = selectedNode.getVisibilityGroups();
		for (final VisibilityGroup visibilityGroup : visibilityGroups) {
		    addComponent(new Label(visibilityGroup.getDescription()));
		}
		if (selectedNode.isWriteGroupMember()) {
		    addComponent(new ChangeVisibilityButton());
		}
	    }
	}

	AbstractNodeInfoGrid() {
	    super(1, 3);
	    setWidth(100, UNITS_PERCENTAGE);
	    setHeight(100, UNITS_PERCENTAGE);
	    //setMargin(true);
	    setMargin(true, false, false, true);
	    setSpacing(true);
	}

	@Override
	public void attach() {
	    super.attach();

	    final Panel fileHeaderPanel = new Panel(getSelectedNodeName());
	    fileHeaderPanel.setIcon(getThemeResource(selectedNode));
	    fileHeaderPanel.setStyleName(Reindeer.PANEL_LIGHT);
	    final VerticalLayout fileHeaderLayout = ((VerticalLayout) fileHeaderPanel.getContent());
	    fileHeaderLayout.setSpacing(true);
	    fileHeaderLayout.setMargin(false);
	    addComponent(fileHeaderPanel, 0, 0);
	    setComponentAlignment(fileHeaderPanel, Alignment.MIDDLE_CENTER);

	    final Panel visibilityPanel = new VisibilityPanel();
	    addComponent(visibilityPanel, 0, 1);
	    setComponentAlignment(visibilityPanel, Alignment.MIDDLE_CENTER);

	    final Panel directoryPanel = new Panel("Directories");
	    addComponent(directoryPanel, 0, 2);
	    setComponentAlignment(directoryPanel, Alignment.MIDDLE_CENTER);
	    directoryPanel.addComponent(new Label("TODO: work in progress."));
	}

	@Override
	public void detach() {
	    super.detach();
	    removeAllComponents();
	}
    }

    private final DocumentTable documentTable = new DocumentTable();
    private final DocumentMenu documentMenu = new DocumentMenu();
    private final SearchDocuments searchDocuments = new SearchDocuments();
    private final UploadFileArea uploadFileArea = new UploadFileArea();
    private final AbstractNodeInfoGrid abstractNodeInfoGrid = new AbstractNodeInfoGrid();

    private Window outerWindow;
    private DirNode dirNode = FileRepository.getOrCreateFileRepository(UserView.getCurrentUser());
    private AbstractFileNode selectedNode = dirNode;

    @Override
    public void setArguments(final String... arg0) {
    }

    @Override
    public void attach() {
	super.attach();

	outerWindow = getWindow();

	final AbstractLayout layout = new VerticalLayout();
	layout.setSizeFull();
	setCompositionRoot(layout);

	final HorizontalLayout horizontalLayoutMenu = new HorizontalLayout();
	horizontalLayoutMenu.setWidth(100, UNITS_PERCENTAGE);
	layout.addComponent(horizontalLayoutMenu);
	
	horizontalLayoutMenu.addComponent(new IconMenu());
	horizontalLayoutMenu.addComponent(uploadFileArea);
	horizontalLayoutMenu.setComponentAlignment(uploadFileArea, Alignment.MIDDLE_RIGHT);

	layout.addComponent(documentMenu);

	final HorizontalLayout horizontalLayout = new HorizontalLayout();
	horizontalLayout.setSizeFull();
	layout.addComponent(horizontalLayout);

	horizontalLayout.addComponent(documentTable);
	horizontalLayout.setExpandRatio(documentTable, 2.0f);

	horizontalLayout.addComponent(abstractNodeInfoGrid);
	horizontalLayout.setExpandRatio(abstractNodeInfoGrid, 1.0f);
    }

    private void changeSelectedNode(final AbstractFileNode abstractFileNode) {
	if (abstractFileNode != selectedNode) {
	    this.selectedNode = abstractFileNode;
	    abstractNodeInfoGrid.detach();
	    abstractNodeInfoGrid.attach();
	}
    }

    private void changeDir(final DirNode dirNode) {
	this.dirNode = dirNode;
	    
	documentTable.detach();
	documentTable.attach();
	    
	documentMenu.detach();
	documentMenu.attach();

	changeSelectedNode(dirNode);
    }

    private ThemeResource getThemeResource(final AbstractFileNode abstractFileNode) {
	final String iconFile = abstractFileNode.isDir() ? "folder1_16x16.gif" :
	    	getIconFile(((FileNode) abstractFileNode).getDocument().getLastVersionedFile().getFilename());
	return new ThemeResource("../../../images/fileManagement/" + iconFile);
    }

    private String getIconFile(final String filename) {
	final int lastDot = filename.lastIndexOf('.');
	final String fileSuffix = lastDot < 0 ? "file" : filename.substring(lastDot + 1);
	return "fileicons/" + fileSuffix + ".png";
    }

    public String getSelectedNodeName() {
	return getNodeName(selectedNode);
    }

    public String getNodeName(final AbstractFileNode node) {
	final User user = UserView.getCurrentUser();
	final DirNode fileRepository = user.getFileRepository();
	return fileRepository == node ? getMessage("label.menu.home") : node.getDisplayName();
    }

}
