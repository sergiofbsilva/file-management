package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.FileManagementSystem;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class RenameDirWindow extends Window {
    
    private DomainItem<AbstractFileNode> dirNodeItem;
    private final RenameDirWindow renameDirWindow;


    public RenameDirWindow(DomainItem<AbstractFileNode> nodeItem) {
	super(getMessage("label.rename"));
	renameDirWindow = this;
	dirNodeItem = nodeItem;
	
	setModal(true);
	setWidth(50, UNITS_PERCENTAGE);

	final VerticalLayout layout = (VerticalLayout) getContent();
	layout.setMargin(true);
	layout.setSpacing(true);
	
	final TransactionalForm form = new TransactionalForm(FileManagementSystem.getBundle());
	form.setWriteThrough(false);
	form.addButton(getMessage("label.save"), new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		form.commit();
		renameDirWindow.close();
	    }
	});
	form.addButton(getMessage("label.close"), new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		renameDirWindow.close();
	    }
	});
	
	form.setItemDataSource(dirNodeItem);
	form.setVisibleItemProperties(new String[] {"displayName"});
	layout.addComponent(form);
    }

}