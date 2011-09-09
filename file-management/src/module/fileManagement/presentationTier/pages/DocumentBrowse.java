package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.presentationTier.component.DocumentFileBrowser;
import module.fileManagement.presentationTier.component.NewDirWindow;
import module.fileManagement.presentationTier.component.NodeDetails;
import pt.ist.bennu.ui.FlowLayout;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Property;
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

@EmbeddedComponent(path = { "DocumentBrowse-(.*)" })
public class DocumentBrowse extends CustomComponent implements EmbeddedComponentContainer {
    
    NodeDetails fileDetails;
    DocumentFileBrowser browser;
    Layout mainLayout;
    GridLayout mainGrid;
    Button btUpload;
    
    @Override
    public void setArguments(String... arg0) {
	String dirExternalId = arg0[1];
	if (!dirExternalId.trim().isEmpty()) {
	    DirNode dirNode = DirNode.fromExternalId(dirExternalId);
	    if (dirNode.isAccessible()) {
		browser.setValue(dirNode);
	    }
	}
    }
    
    public HorizontalLayout createButtons() {
	HorizontalLayout layout = new HorizontalLayout();
	layout.setSpacing(true);
	Button btNewFolder = new Button(getMessage("button.new.folder"));
	
	btNewFolder.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		if (browser.getDirNode().isWriteGroupMember()) {
		    final NewDirWindow addDirWindow = new NewDirWindow(browser.getNodeItem());
		    getWindow().addWindow(addDirWindow);
		} else {
                    getWindow().showNotification(getMessage("message.dir.cannot.write"));
		}
	    }
	});
	layout.addComponent(btNewFolder);
	
	
	btUpload = new Button(getMessage("button.upload"));
	
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
//	browser.addDirChangedListener(new ValueChangeListener() {
//	    
//	    @Override
//	    public void valueChange(ValueChangeEvent event) {
//		final DirNode dirNode = (DirNode)event.getProperty().getValue();
//		btUpload.setEnabled(dirNode.isWriteGroupMember());
//	    }
//	});
	return browser;
    }
    
    public VerticalLayout createPage() {
	VerticalLayout layout = new VerticalLayout();
	layout.setSizeFull();
	layout.setMargin(true,true,false,false);
	layout.setSpacing(true);
	browser = createBrowser();
	layout.addComponent(createButtons());
	layout.addComponent(new QuotaLabel(browser.getNodeItem()));
	
	mainGrid = createGrid(browser);
	setFileDetails(browser.getNodeItem());
	layout.addComponent(mainGrid);
	return layout;
    }
    
    public void setFileDetails(DomainItem<AbstractFileNode> nodeItem) {
	mainGrid.removeComponent(fileDetails);
	fileDetails = NodeDetails.makeDetails(nodeItem);
	fileDetails.setDocumentBrowse(this);
	fileDetails.setSizeFull();
	mainGrid.addComponent(fileDetails,1,0);
    }
    
    public DocumentBrowse() {
	mainLayout = createPage();
	browser.addListener(new ValueChangeListener() {
	    @Override
	    public void valueChange(ValueChangeEvent event) {
		Object absFileNode = event.getProperty().getValue();
		if (absFileNode != null) {
		    DomainItem<AbstractFileNode> nodeItem = browser.getContainer().getItem(absFileNode);
		    setFileDetails(nodeItem);
		}
	    }
	});
	setCompositionRoot(mainLayout);
    }
    
    
    @SuppressWarnings("unused")
    private class QuotaLabel extends Label {
	
	private String quotaContent = null;
	
	public QuotaLabel(DomainItem<AbstractFileNode> item) {
	    setPropertyDataSource(item);
	    setContentMode(Label.CONTENT_XHTML);
	    setContent();
	    Property.ValueChangeListener listener = new Property.ValueChangeListener() {
		    
		    @SuppressWarnings("unchecked")
		    @Override
		    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			setContent();
		    }

		};
	    getItem().addListener(listener);
	}
	
	@SuppressWarnings("unchecked")
	private DomainItem<AbstractFileNode> getItem() {
	    return (DomainItem<AbstractFileNode>) getPropertyDataSource();
	}
	
	
	private void setContent() {
	    DomainItem<AbstractFileNode> dirNode = getItem();
	    final String percentTotalUsedSpace = dirNode.getItemProperty("percentOfTotalUsedSpace").toString();
	    final String totalUsedSpace = dirNode.getItemProperty("presentationTotalUsedSpace").toString();
	    final String quota = dirNode.getItemProperty("presentationQuota").toString();
	    quotaContent = String.format("%s espaco utilizado</br>A utilizar %s dos seus %s", percentTotalUsedSpace, totalUsedSpace , quota);
	}
	
	@Override
	public String toString() {
	    return quotaContent;
	}
    }
    
    
    
    public void refresh() {
	fileDetails.updateFilePanel();
    }
    
    public void removeNode(DomainItem<AbstractFileNode> nodeItem) {
	browser.removeNodeAndSelectNext(nodeItem.getValue());
    }

}