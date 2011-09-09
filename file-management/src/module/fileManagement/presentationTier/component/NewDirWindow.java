package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import myorg.domain.exceptions.DomainException;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NewDirWindow extends Window {
    
    public NewDirWindow(final DomainItem<AbstractFileNode> parentDir) {
	super(getMessage("label.file.repository.add.dir"));
	setModal(true);
	setWidth(50, UNITS_PERCENTAGE);
	
	final HorizontalLayout hlError = new HorizontalLayout();
	final VerticalLayout layout = (VerticalLayout) getContent();
	layout.setMargin(true);
	layout.setSpacing(true);
	
	final TextField editor = new TextField();
	
	Button btSave = new Button(getMessage("label.save"), new Button.ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		final DirNode node = (DirNode) parentDir.getValue();
		try {
		    node.createDir((String) editor.getValue());
		    parentDir.setValue(node);
		    close();
		}catch(DomainException de) {
		    final Label label = new Label(de.getLocalizedMessage());
		    hlError.addComponent(label);
		}
		
	    }
	});
	
	Button btClose = new Button(getMessage("label.close"), new Button.ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		close();
	    }
	});
	layout.addComponent(hlError);
	layout.addComponent(editor);
	final HorizontalLayout hl = new HorizontalLayout();
	hl.addComponent(btSave);
	hl.addComponent(btClose);
	layout.addComponent(hl);
	
    }

}