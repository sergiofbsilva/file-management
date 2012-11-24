/*
 * @(#)DocumentShare.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.presentationTier.pages;

import java.util.HashSet;
import java.util.Map;

import module.fileManagement.domain.FileManagementSystem;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.VisibilityGroup;
import module.fileManagement.domain.VisibilityGroup.VisibilityOperation;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.fileManagement.presentationTier.component.groups.GroupCreatorRegistry;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroup;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroupCreator;
import module.fileManagement.presentationTier.data.AbstractSearchContainer;
import module.fileManagement.presentationTier.data.GroupContainer;
import module.vaadin.ui.BennuTheme;

import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.Presentable;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;
import pt.ist.vaadinframework.ui.TimeoutSelect;

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
import com.vaadin.ui.themes.Reindeer;

@EmbeddedComponent(path = { "DocumentShare" }, args = { "fileNode", "contextPath" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentShare extends CustomComponent implements EmbeddedComponentContainer {

    private AbstractFileNode fileNode;
    private AbstractSearchContainer groupContainer;
    private VerticalLayout groupSpecificLayout;
    private HasPersistentGroup currentPersistentGroupHolder;
    private VisibilityOperation currentVisibilityOperation;
    private ContextPath contextPath;
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
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	final String fileExternalId = arguments.get("fileNode");
	final AbstractFileNode node = AbstractFileNode.fromExternalId(fileExternalId);
	if (node == null || node.isShared() || !node.isWriteGroupMember()) {
	    return false;
	}
	return true;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	final String fileExternalId = arguments.get("fileNode");
	final String contextPath = arguments.get("contextPath");
	fileNode = AbstractFileNode.fromExternalId(fileExternalId);
	this.contextPath = new ContextPath(contextPath);
	updateGroupTable();
    }

    private void updateGroupTable() {
	for (VisibilityGroup visibilityGroup : fileNode.getVisibilityGroups()) {
	    // if (visibilityGroup.persistentGroup instanceof SingleUserGroup) {
	    // final SingleUserGroup singleUserGroup = (SingleUserGroup)
	    // visibilityGroup.persistentGroup;
	    // if (singleUserGroup.isMember(UserView.getCurrentUser())) {
	    // continue;
	    // }
	    // }
	    visibilityGroupContainer.addBean(new VisibilityGroupBean(visibilityGroup));
	}
    }

    private void unshare(VisibilityGroup group) {
	fileNode.unshare(group);
	visibilityGroupContainer.removeItem(group);
    }

    public Component createHeaderPanel() {
	Panel welcomePanel = new Panel();
	welcomePanel.setStyleName(BennuTheme.PANEL_LIGHT);
	welcomePanel.setCaption(FileManagementSystem.getMessage("document.share.title"));
	welcomePanel.setScrollable(false);
	final Layout hlWelcomeContent = new VerticalLayout();
	Label lblWelcomeText = new Label(FileManagementSystem.getMessage("welcome.share.content"), Label.CONTENT_XHTML);
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
	final Panel pnlShare = new Panel(FileManagementSystem.getMessage("document.share.with"));
	pnlShare.setStyleName(Reindeer.PANEL_LIGHT);
	((VerticalLayout) pnlShare.getContent()).setSpacing(true);

	final HorizontalLayout hlVisibilityGroup = new HorizontalLayout();
	hlVisibilityGroup.setSizeFull();
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
		// if (fileNodeGroups.add(currentPersistentGroupHolder)) {
		// final VisibilityGroup group = new
		// VisibilityGroup(currentPersistentGroupHolder.getPersistentGroup(),
		// currentVisibilityOperation);
		// final VisibilityGroupBean groupBean = new
		// VisibilityGroupBean(group);
		// visibilityGroupContainer.addBean(groupBean);
		// updateFileVisibility(group);
		// } else {
		// getWindow().showNotification("Elemento já se encontra atribuído");
		// }
		// selectGroupToMake.setValue(StringUtils.EMPTY);
		// groupSpecificLayout.removeAllComponents();
		final VisibilityGroup group = new VisibilityGroup(currentPersistentGroupHolder.getPersistentGroup(),
			currentVisibilityOperation);
		if (!visibilityGroupContainer.containsId(group)) {
		    visibilityGroupContainer.addBean(new VisibilityGroupBean(group));
		    updateFileVisibility(group);
		} else {
		    getWindow().showNotification(
			    "Já está partilhado com o utilizador " + group.persistentGroup.getPresentationName());
		}
		selectGroupToMake.setValue(StringUtils.EMPTY);
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

	selectGroupToMake.setSizeFull();
	hlVisibilityGroup.setExpandRatio(selectGroupToMake, 0.7f);
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

    private GridLayout createGrid() {
	GridSystemLayout gsl = new GridSystemLayout();
	gsl.setSizeFull();
	gsl.setMargin(true, true, true, false);
	gsl.setSpacing(true);
	gsl.addComponent(10, createSharePanel());
	gsl.addComponent(6, createFileDetails());
	return gsl;
    }

    public Component createPage() {
	GridSystemLayout gsl = new GridSystemLayout();
	gsl.setMargin(true);
	gsl.setSpacing(true);
	gsl.addComponent(16, createHeaderPanel());
	gsl.addComponent(10, createSharePanel());
	gsl.addComponent(6, createFileDetails());
	gsl.addComponent(16, fileNodeGroupsTable);
	gsl.addComponent(6, createBackLink());

	return gsl;
    }

    private Component createBackLink() {
	final Button btBack = new Button("Concluir");
	btBack.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		EmbeddedApplication.back(getApplication());
	    }
	});
	return btBack;
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
			if (visibilityGroup.persistentGroup.isMember(UserView.getCurrentUser())) {
			    ConfirmDialog.show(getWindow(), "Atenção",
				    "Encontra-se neste grupo. Tem a certeza que o deseja remover ? ", "Sim", "Não",
				    new ConfirmDialog.Listener() {

					@Override
					public void onClose(ConfirmDialog dialog) {
					    if (dialog.isConfirmed()) {
						unshare(visibilityGroup);
					    }
					}
				    });
			} else {
			    unshare(visibilityGroup);
			}

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
