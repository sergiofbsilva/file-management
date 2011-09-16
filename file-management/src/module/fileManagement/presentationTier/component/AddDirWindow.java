package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.DirNode;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddDirWindow extends Window {
    private DocumentFileBrowser browser;
    private final AddDirWindow addDirWindow;
    private final TextField editor = new TextField();

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
	    // getWindow().removeWindow(addDirWindow);
	    addDirWindow.close();
	}
    }

    private class SaveButton extends CloseButton implements ClickListener {

	SaveButton() {
	    super("label.save");
	}

	@Override
	public void buttonClick(final ClickEvent event) {
	    final DirNode newDirNode = browser.getDirNode().createDir((String) editor.getValue(), browser.getContextPath());
	    browser.getContainer().addItem(newDirNode);
	    super.buttonClick(event);
	}
    }

    public AddDirWindow(DocumentFileBrowser browser) {
	super(getMessage("label.file.repository.add.dir"));
	addDirWindow = this;
	this.browser = browser;
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