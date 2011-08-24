package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.presentationTier.component.AddDirWindow;
import module.fileManagement.presentationTier.component.DocumentFileBrowser;
import module.fileManagement.presentationTier.component.NodeDetails;
import pt.ist.bennu.ui.FlowLayout;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "DocumentBrowse" })
public class DocumentBrowse extends CustomComponent implements EmbeddedComponentContainer {
    
    NodeDetails fileDetails;
    DocumentFileBrowser browser;
    Layout mainLayout;
    GridLayout mainGrid;
    
    @Override
    public void setArguments(String... arg0) {
	// TODO Auto-generated method stub

    }
    
    public HorizontalLayout createButtons() {
	HorizontalLayout layout = new HorizontalLayout();
	layout.setSpacing(true);
	Button btNewFolder = new Button(getMessage("button.new.folder"));
	
	btNewFolder.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		if (browser.getDirNode().isWriteGroupMember()) {
		    final AddDirWindow addDirWindow = new AddDirWindow(browser);
		    getWindow().addWindow(addDirWindow);
		} else {
                    getWindow().showNotification(getMessage("message.dir.cannot.write"));
		}
	    }
	});
	layout.addComponent(btNewFolder);
	
	
	Button btUpload = new Button(getMessage("button.upload"));
	
	btUpload.addListener(new ClickListener() {
	    public void buttonClick(ClickEvent event) {
		final AbstractFileNode dirNode = browser.getDirNode();
		if (dirNode.isDir()) {
		    getWindow().open(new ExternalResource("#UploadPage-" + dirNode.getExternalId()));
		}
	    }
	});
	
	layout.addComponent(btUpload);
	return layout;
    }
    
    public GridLayout createGrid(Component browser) {
	GridLayout grid = new GridLayout(2,1);
	grid.setSizeFull();
	grid.setMargin(true,true,true,false);
	grid.setSpacing(true);
	browser.setSizeFull();
	grid.addComponent(browser, 0, 0);
//	grid.setColumnExpandRatio(0, 1f);
//	grid.addComponent(fileDetails,1,0);
//	grid.setColumnExpandRatio(1, 1f);
	return grid;
    }
    
    
    public FlowLayout createNavigationLink(String path) {
	FlowLayout layout = new FlowLayout();
	layout.setSizeFull();
	final String[] split = path.split("/");
	String link = new String();
	for(int i = 0 ; i < split.length - 1; i++) {
	    link += String.format("<a href='dir#%s'>%s</a> > ", split[i],split[i]);
	}
	link += split[split.length - 1];
	layout.addComponent(new Label(link,Label.CONTENT_XHTML));
	return layout;
    }
    
    public DocumentFileBrowser createBrowser() {
	DocumentFileBrowser browser = new DocumentFileBrowser();
//	VerticalLayout layout = new VerticalLayout();
//	layout.setSizeFull();
//	layout.addComponent(createNavigationLink("/home/sfbs/sparta/benfica/is/the/best/SLB"));
//	Table tbBrowser = new Table();
//	tbBrowser.setSizeFull();
//	layout.addComponent(tbBrowser);
//	return layout;
	return browser;
    }
    
    public VerticalLayout createPage() {
	VerticalLayout layout = new VerticalLayout();
	layout.setSizeFull();
	layout.setMargin(true,true,false,false);
	layout.setSpacing(true);
	layout.addComponent(createButtons());
	browser = createBrowser();
	mainGrid = createGrid(browser);
	setFileDetails((AbstractFileNode)browser.getDirNode());
	layout.addComponent(mainGrid);
	return layout;
    }
    
    public void setFileDetails(AbstractFileNode absFileNode) {
	mainGrid.removeComponent(fileDetails);
	fileDetails = NodeDetails.makeDetails(absFileNode);
	fileDetails.setDocumentBrowse(this);
	fileDetails.setSizeFull();
	mainGrid.addComponent(fileDetails,1,0);
    }
    
    public DocumentBrowse() {
	mainLayout = createPage();
	browser.addListener(new ValueChangeListener() {
	    @Override
	    public void valueChange(ValueChangeEvent event) {
		final AbstractFileNode absFileNode = (AbstractFileNode) event.getProperty().getValue();
		if (absFileNode != null) {
		    setFileDetails(absFileNode);
		}
	    }
	});
	setCompositionRoot(mainLayout);
    }
    
    public void refresh() {
	fileDetails.updateFilePanel();
    }
    
    public void removeNode(AbstractFileNode node) {
	browser.getDocumentTable().getContainerDataSource().removeItem(node);
    }

}
