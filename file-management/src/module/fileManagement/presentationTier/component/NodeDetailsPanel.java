package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.FileManagementSystem;
import pt.ist.bennu.ui.viewers.ViewerFactory;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.DefaultViewerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class NodeDetailsPanel extends Panel {
    
    private AbstractFileNode absFileNode;
    
    public NodeDetailsPanel() {
	super(getMessage("label.file.details"));
    }
    
    private Component updateDirDetails() {
	final ResourceBundle bundle = FileManagementSystem.getBundle();
	final ViewerFactory defaultViewerFactory = new DefaultViewerFactory(bundle);
	TabularViewer viewer = new TabularViewer(defaultViewerFactory);
	
	List<String> propertyIds = new ArrayList<String>();
	propertyIds.addAll(Arrays.asList(new String[] { "displayName" , "presentationFilesize"}));
	propertyIds.addAll(Arrays.asList(new String[] { "countFiles" }));
	viewer.setItemDataSource(new DomainItem(absFileNode),propertyIds);
	return viewer;
    }

    public void setFileNode(final AbstractFileNode absFileNode) {
	this.absFileNode = absFileNode;
	updateFilePanel();
    }
    
    public void updateFilePanel() {
	if (absFileNode == null) {
	    return;
	}
	removeAllComponents();
	addComponent(NodeDetails.makeDetails(absFileNode));
    }

    private TabularViewer updateFileDetails() {
	final ResourceBundle bundle = FileManagementSystem.getBundle();
	final ViewerFactory defaultViewerFactory = new DefaultViewerFactory(bundle);
	TabularViewer viewer = new TabularViewer(defaultViewerFactory);
	
	List<String> propertyIds = new ArrayList<String>();
	propertyIds.addAll(Arrays.asList(new String[] { "displayName" , "presentationFilesize"}));
	if (absFileNode.isFile()) {
	    propertyIds.addAll(Arrays.asList(new String[] { "document.versionNumber" }));
	} else if (absFileNode.isDir()) {
	    propertyIds.addAll(Arrays.asList(new String[] { "countFiles" }));
	}
	viewer.setItemDataSource(new DomainItem(absFileNode),propertyIds);
	return viewer;
    }
    
    
    
}
