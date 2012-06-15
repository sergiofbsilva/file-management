package module.fileManagement.presentationTier.component.dialog;

import java.util.Set;

import pt.ist.vaadinframework.data.reflect.DomainContainer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.presentationTier.pages.DocumentHome;

public class SelectDestinationDialog extends Window {

    private DirNode dirNode;
    private DirNode selectedDirNode;

    private DomainContainer<DirNode> childs;
    private ContainerHierarchicalWrapper wrapper;
    private Tree tree;

    public SelectDestinationDialog() {
	super();
	setModal(true);
	setResizable(false);

	setCaption("Seleccionar Directoria");

	Set<DirNode> availableRepositories = DocumentHome.getAvailableRepositories();

	childs = new DomainContainer<DirNode>(DirNode.class);
	childs.setContainerProperties("displayName", "icon");
	wrapper = new ContainerHierarchicalWrapper(childs);

	for (DirNode dirNode : availableRepositories) {
	    wrapper.addItem(dirNode);
	    createChildContainer(dirNode);
	}

	wrapper.updateHierarchicalWrapper();
	buildDefaultLayout();
    }

    public DirNode getSelectedDirNode() {
	return selectedDirNode;
    }

    public void setSelectedDirNode(DirNode selectedDirNode) {
	this.selectedDirNode = selectedDirNode;
    }

    private void createChildContainer(DirNode root) {
	boolean hasDirChilds = false;
	for (AbstractFileNode child : root.getChild()) {
	    if (child.isDir() && child.isWriteGroupMember()) {
		hasDirChilds = true;
		final DirNode childDirNode = (DirNode) child;
		wrapper.addItem(childDirNode);
		wrapper.setParent(childDirNode, root);
		createChildContainer(childDirNode);
	    }
	}
	wrapper.setChildrenAllowed(root, hasDirChilds);
    }

    public void buildDefaultLayout() {
	Panel pnlDialog = new Panel("Por favor seleccione uma directoria");
	pnlDialog.setScrollable(true);

	pnlDialog.setWidth("400px");
	pnlDialog.setHeight("400px");

	tree = new Tree();
	tree.setImmediate(true);
	tree.setContainerDataSource(wrapper);
	tree.setItemIconPropertyId("icon");
	tree.setItemCaptionPropertyId("displayName");
	expandAll();

	HorizontalLayout hlButtons = new HorizontalLayout();
	hlButtons.setSizeUndefined();
	hlButtons.setSpacing(true);

	final Button btOk = new Button("Seleccionar", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		selectedDirNode = (DirNode) tree.getValue();
		close();
	    }

	});

	btOk.setEnabled(false);

	final Button btCancel = new Button("Cancelar");
	btCancel.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		close();
	    }
	});

	hlButtons.addComponent(btOk);
	hlButtons.addComponent(btCancel);

	tree.addListener(new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		btOk.setEnabled(event.getProperty().getValue() != null);
	    }

	});

	pnlDialog.addComponent(tree);

	// collapse , expand buttons

	HorizontalLayout hlTreeOps = new HorizontalLayout();
	hlTreeOps.setSpacing(true);
	hlTreeOps.setMargin(false, false, true, false);

	final Button btCollapseAll = new Button("Colapsar Tudo");
	btCollapseAll.setStyleName(BaseTheme.BUTTON_LINK);
	btCollapseAll.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		collapseAll();
	    }
	});

	final Button btExpandAll = new Button("Expandir Tudo");
	btExpandAll.setStyleName(BaseTheme.BUTTON_LINK);
	btExpandAll.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		expandAll();
	    }
	});

	hlTreeOps.addComponent(btCollapseAll);
	hlTreeOps.addComponent(btExpandAll);

	addComponent(pnlDialog);
	addComponent(hlTreeOps);
	addComponent(hlButtons);
	getContent().setSizeUndefined();
    }

    private void expandAll() {
	for (Object item : wrapper.rootItemIds()) {
	    tree.expandItemsRecursively(item);
	}
    }

    private void collapseAll() {
	for (Object item : wrapper.rootItemIds()) {
	    tree.collapseItemsRecursively(item);
	}
    }

}
