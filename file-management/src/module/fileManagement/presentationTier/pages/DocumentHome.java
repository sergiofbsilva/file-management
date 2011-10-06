package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.Map;

import module.fileManagement.domain.FileNode;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@EmbeddedComponent(path = { "DocumentHome" })
public class DocumentHome extends CustomComponent implements EmbeddedComponentContainer {

    @Override
    public void setArguments(Map<String, String> arguments) {
	// TODO Auto-generated method stub

    }

    public Panel createWelcomePanel() {
	Panel welcomePanel = new Panel();
	welcomePanel.setStyleName(Reindeer.PANEL_LIGHT);
	welcomePanel.setCaption(getMessage("welcome.title"));
	welcomePanel.setScrollable(false);
	final Layout hlWelcomeContent = new VerticalLayout();
	Label lblWelcomeText = new Label(getMessage("welcome.content"), Label.CONTENT_XHTML);
	hlWelcomeContent.addComponent(lblWelcomeText);
	welcomePanel.setContent(hlWelcomeContent);
	return welcomePanel;
    }

    public GridLayout createGrid(Component recentlyShared, Component fileDetails) {
	GridLayout grid = new GridLayout(2, 1);
	grid.setSizeFull();
	grid.setMargin(true, true, true, false);
	grid.setSpacing(true);
	grid.addComponent(recentlyShared, 0, 0);
	grid.addComponent(fileDetails, 1, 0);
	return grid;
    }

    public Panel createRecentlyShared() {
	Panel recentlyPanel = new Panel(getMessage("label.recently.shared"));
	Table tbShared = new Table();
	tbShared.setSizeFull();
	recentlyPanel.addComponent(tbShared);
	return recentlyPanel;
    }

    public static Panel createFileDetails(FileNode fileNode) {
	Panel pDetails = new Panel(getMessage("label.file.details"));
	DomainItem item = new DomainItem(fileNode);
	return pDetails;
    }

    public static Panel createDirDetails() {
	Panel pDetails = new Panel(getMessage("label.file.details"));
	pDetails.addComponent(new Label("Name: document.doc", Label.CONTENT_TEXT));
	pDetails.addComponent(new Label("Size: 120kb", Label.CONTENT_TEXT));
	return pDetails;
    }

    public Component createPage() {
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.setMargin(true);
	mainLayout.setSizeFull();
	mainLayout.setSpacing(true);
	mainLayout.addComponent(createWelcomePanel());
	// mainLayout.addComponent(createGrid(createRecentlyShared(),createFileDetails()));
	return mainLayout;
    }

    @Override
    public void attach() {
	setCompositionRoot(createPage());
    }

}
