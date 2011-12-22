package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.VisibilityGroup;
import module.fileManagement.domain.VisibilityGroup.VisibilityOperation;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.fileManagement.presentationTier.component.groups.GroupCreatorRegistry;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroup;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroupCreator;
import module.fileManagement.presentationTier.data.GroupContainer;
import module.vaadin.ui.BennuTheme;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.Presentable;
import myorg.domain.groups.SingleUserGroup;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import pt.ist.bennu.ui.TimeoutSelect;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@EmbeddedComponent(path = { "DocumentShare" }, args = { "fileNode", "contextPath" })
public class DocumentShare extends CustomComponent implements EmbeddedComponentContainer {

    private AbstractFileNode fileNode;
    private GroupContainer groupContainer;
    private VerticalLayout groupSpecificLayout;
    private HasPersistentGroup currentPersistentGroupHolder;
    private VisibilityOperation currentVisibilityOperation;
    private ContextPath contextPath;
    private Set<HasPersistentGroup> fileNodeGroups;
    private Table fileNodeGroupsTable;
    private BeanContainer<VisibilityGroup, VisibilityGroupBean> visibilityGroupContainer;

    public class VisibilityGroupBean extends BeanItem<VisibilityGroup> {

	public VisibilityGroupBean(VisibilityGroup bean) {
	    super(bean);
	}

	public String getPresentationName() {
	    return getBean().persistentGroup.getPresentationName();
	}

	public void setPresentationName() {

	}

	public String getVisibilityName() {
	    return getBean().visibilityOperation.getDescription();
	}

	public void setVisibilityName() {

	}
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	final String fileExternalId = arguments.get("fileNode");
	final String contextPath = arguments.get("contextPath");
	fileNode = AbstractFileNode.fromExternalId(fileExternalId);
	if (fileNode == null || !fileNode.isWriteGroupMember()) {
	    throw new InvalidOperationException("É necessário seleccionar um ficheiro");
	}
	this.contextPath = new ContextPath(contextPath);
	updateGroupTable();
    }

    private void updateGroupTable() {
	for (VisibilityGroup visibilityGroup : fileNode.getVisibilityGroups()) {
	    if (visibilityGroup.persistentGroup instanceof SingleUserGroup) {
		final SingleUserGroup singleUserGroup = (SingleUserGroup) visibilityGroup.persistentGroup;
		if (singleUserGroup.isMember(UserView.getCurrentUser())) {
		    continue;
		}
	    }
	    visibilityGroupContainer.addBean(new VisibilityGroupBean(visibilityGroup));
	}
    }

    private void unshare(VisibilityGroup group) {
	fileNode.unshare(group);
	fileNodeGroupsTable.removeItem(group);
    }

    public Component createHeaderPanel() {
	Panel welcomePanel = new Panel();
	welcomePanel.setStyleName(BennuTheme.PANEL_LIGHT);
	welcomePanel.setCaption(getMessage("document.share.title"));
	welcomePanel.setScrollable(false);
	final Layout hlWelcomeContent = new VerticalLayout();
	Label lblWelcomeText = new Label(getMessage("welcome.share.content"), Label.CONTENT_XHTML);
	hlWelcomeContent.addComponent(lblWelcomeText);
	welcomePanel.setContent(hlWelcomeContent);
	return welcomePanel;
    }

    private class VisibilityOperationSelect extends NativeSelect {

	private VisibilityOperationSelect() {
	    addContainerProperty("description", String.class, null);
	    for (final VisibilityOperation operation : VisibilityOperation.values()) {
		final Item item = addItem(operation);
		item.getItemProperty("description").setValue(operation.getDescription());
	    }
	    setNullSelectionAllowed(false);
	    setImmediate(true);
	    setItemCaptionMode(ITEM_CAPTION_MODE_PROPERTY);
	    setItemCaptionPropertyId("description");
	    select(VisibilityOperation.READ);
	}

    }

    private Component createSharePanel() {
	final Panel pnlShare = new Panel(getMessage("document.share.with"));
	final HorizontalLayout hlVisibilityGroup = new HorizontalLayout();
	hlVisibilityGroup.setSpacing(true);

	final TimeoutSelect selectGroupToMake = new TimeoutSelect();
	selectGroupToMake.setContainerDataSource(groupContainer);
	selectGroupToMake.setItemCaptionPropertyId("presentationName");
	selectGroupToMake.addListener(new TextChangeListener() {

	    @Override
	    public void textChange(TextChangeEvent event) {
		groupContainer.search(event.getText());
	    }
	});

	final VisibilityOperationSelect operationSelect = new VisibilityOperationSelect();
	operationSelect.addListener(new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		final VisibilityOperation operation = (VisibilityOperation) event.getProperty().getValue();
		currentVisibilityOperation = operation;
	    }
	});

	final Button btShare = new Button("Partilhar", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		if (fileNodeGroups.add(currentPersistentGroupHolder)) {
		    final VisibilityGroup group = new VisibilityGroup(currentPersistentGroupHolder.getPersistentGroup(),
			    currentVisibilityOperation);
		    final VisibilityGroupBean groupBean = new VisibilityGroupBean(group);
		    visibilityGroupContainer.addBean(groupBean);
		    updateFileVisibility(group);
		} else {
		    getWindow().showNotification("Elemento já se encontra atribuído");
		}
		currentVisibilityOperation = VisibilityOperation.READ;
		selectGroupToMake.discard();
		groupSpecificLayout.removeAllComponents();
	    }
	});

	selectGroupToMake.addListener(new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		groupSpecificLayout.removeAllComponents();
		Presentable presentable = (Presentable) event.getProperty().getValue();

		for (final HasPersistentGroupCreator hasPersistentGroupCreator : GroupCreatorRegistry
			.getPersistentGroupCreators()) {
		    final HasPersistentGroup groupHolder = hasPersistentGroupCreator.createGroupFor(presentable);
		    if (groupHolder != null) {
			setCurrentPersistentGroupHolder(groupHolder);
			groupHolder.renderGroupSpecificLayout(groupSpecificLayout);
			groupSpecificLayout.addComponent(btShare);
		    }
		}

	    }
	});

	hlVisibilityGroup.addComponent(selectGroupToMake);
	hlVisibilityGroup.addComponent(operationSelect);

	pnlShare.addComponent(hlVisibilityGroup);
	pnlShare.addComponent(groupSpecificLayout);
	return pnlShare;
    }

    protected void updateFileVisibility(VisibilityGroup group) {
	fileNode.share(currentPersistentGroupHolder.getUser(), group, contextPath);
    }

    protected void setCurrentPersistentGroupHolder(HasPersistentGroup groupHolder) {
	this.currentPersistentGroupHolder = groupHolder;
    }

    private Panel createFileDetails() {
	return NodeDetails.makeDetails(new DomainItem<AbstractFileNode>(fileNode), false, true);
    }

    public GridLayout createGrid() {
	GridLayout grid = new GridLayout(2, 1);
	grid.setSizeFull();
	grid.setMargin(true, true, true, false);
	grid.addComponent(createSharePanel(), 0, 0);
	grid.addComponent(createFileDetails(), 1, 0);
	return grid;
    }

    public Component createPage() {
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.setSizeFull();
	mainLayout.setMargin(true);
	mainLayout.setSpacing(true);
	mainLayout.addComponent(createHeaderPanel());
	mainLayout.addComponent(createGrid());
	mainLayout.addComponent(fileNodeGroupsTable);
	return mainLayout;
    }

    public DocumentShare() {
	fileNode = null;
	groupContainer = new GroupContainer();
	groupSpecificLayout = new VerticalLayout();
	groupSpecificLayout.setSpacing(true);
	visibilityGroupContainer = new BeanContainer<VisibilityGroup, VisibilityGroupBean>(VisibilityGroupBean.class);
	visibilityGroupContainer.setBeanIdProperty("bean");

	currentVisibilityOperation = VisibilityOperation.READ;

	fileNodeGroupsTable = new Table();
	fileNodeGroupsTable.addStyleName("shareTable");
	fileNodeGroupsTable.setVisible(false);
	fileNodeGroupsTable.setImmediate(true);
	fileNodeGroupsTable.setWidth("100%");
	fileNodeGroupsTable.setPageLength(0);
	fileNodeGroupsTable.setContainerDataSource(visibilityGroupContainer);
	fileNodeGroups = new HashSet<HasPersistentGroup>();
	fileNodeGroupsTable.addListener(new ItemSetChangeListener() {

	    @Override
	    public void containerItemSetChange(ItemSetChangeEvent event) {
		fileNodeGroupsTable.setVisible(event.getContainer().size() > 0);
	    }
	});
	fileNodeGroupsTable.addGeneratedColumn("Nome", new ColumnGenerator() {

	    @Override
	    public Component generateCell(Table source, Object itemId, Object columnId) {
		final VisibilityGroup visibilityGroup = (VisibilityGroup) itemId;
		final Label lblName = new Label(visibilityGroup.persistentGroup.getPresentationName());
		lblName.setHeight("30px");
		return lblName;
	    }

	});

	fileNodeGroupsTable.addGeneratedColumn("Remover", new ColumnGenerator() {

	    @Override
	    public Component generateCell(Table source, Object itemId, Object columnId) {
		final VisibilityGroup visibilityGroup = (VisibilityGroup) itemId;
		Button btRemoveGroup = new Button("Remover", new ClickListener() {

		    @Override
		    public void buttonClick(ClickEvent event) {
			unshare(visibilityGroup);
		    }
		});
		btRemoveGroup.setStyleName(BaseTheme.BUTTON_LINK);
		return btRemoveGroup;
	    }

	});

	fileNodeGroupsTable.setVisibleColumns(new String[] { "Nome", "visibilityName", "Remover" });
	fileNodeGroupsTable.setColumnHeaders(new String[] { "Nome", "Visibilidade", "Acções" });
	fileNodeGroupsTable.setColumnWidth("Nome", 670);
	fileNodeGroupsTable.setColumnReorderingAllowed(false);
	fileNodeGroupsTable.setColumnCollapsingAllowed(false);
	fileNodeGroupsTable.setSortDisabled(true);
    }

    @Override
    public void attach() {
	super.attach();
	setCompositionRoot(createPage());
    }
}
