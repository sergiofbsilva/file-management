package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.HashSet;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.organization.domain.Person;
import myorg.domain.User;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.DomainLuceneContainer;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.fields.LuceneSelect;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@EmbeddedComponent(path = { "DocumentShare-(.*)" })
public class DocumentShare extends CustomComponent implements EmbeddedComponentContainer {
    
    private AbstractFileNode fileNode;
    private DomainContainer<Person> personsToShareWith; 
    
    @Override
    public void setArguments(String... args) {
	String fileExternalId = args[1];
	fileNode = AbstractFileNode.fromExternalId(fileExternalId);
    }

    public Component createHeaderPanel() {
	Panel welcomePanel = new Panel();
	welcomePanel.setStyleName(Reindeer.PANEL_LIGHT);
	welcomePanel.setCaption(getMessage("document.share.title"));
	welcomePanel.setScrollable(false);
	final Layout hlWelcomeContent = new VerticalLayout();
	Label lblWelcomeText = new Label(getMessage("welcome.content"), Label.CONTENT_TEXT);
	hlWelcomeContent.addComponent(lblWelcomeText);
	welcomePanel.setContent(hlWelcomeContent);
	return welcomePanel;
    }
    
    private Component createSharePanel() {
	final Panel pnlSelectShare = new Panel(getMessage("document.share.with"));
	
	pnlSelectShare.setSizeFull();
	((VerticalLayout)pnlSelectShare.getContent()).setSpacing(true);
	
	final DomainLuceneContainer<Person> luceneContainer = new DomainLuceneContainer<Person>(Person.class);
//	final LuceneSelect selectPerson = new LuceneSelect();
//	selectPerson.setFilteringMode(Select.FILTERINGMODE_CONTAINS);
//	selectPerson.setImmediate(true);
	final LuceneSelect selectPerson = new LuceneSelect();
	selectPerson.setSizeFull();
	selectPerson.setNullSelectionAllowed(true);
//	selectPerson.setNullSelectionItemId(null);
	selectPerson.setItemCaptionPropertyId("presentationString");
	selectPerson.addListener(new ValueChangeListener() {
	    
	    @Override
	    public void valueChange(ValueChangeEvent event) {
//		final Person person = (Person) selectPerson.getValue();
		final Person person = (Person) event.getProperty().getValue();
		if (person != null) {
		    personsToShareWith.addItem(person);
		}
		selectPerson.select(null);
	    }
	});
	selectPerson.setContainerDataSource(luceneContainer);
	pnlSelectShare.addComponent(selectPerson);
	pnlSelectShare.addComponent(createSharedWithTable());
	pnlSelectShare.addComponent(createShareButtonLink());
	return pnlSelectShare;
    }
    
    
    
    private Component createShareButtonLink() {
	return new Button(getMessage("document.share.title"), new Button.ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		for(Object itemId : personsToShareWith.getItemIds()) {
		    DomainItem<Person> person = personsToShareWith.getItem(itemId);
		    DomainItem<User> user = (DomainItem<User>) person.getItemProperty("user");
		    fileNode.share(user.getValue());
		}
	    }
	});
    }

    private Component createSharedWithTable() {
	Table tb = new Table();
	tb.setSizeFull();
	tb.setContainerDataSource(personsToShareWith);
	tb.setVisibleColumns(new String[] { "presentationString"});
	tb.setColumnHeaders(new String[] { "Utilizador"} );
	tb.addGeneratedColumn("remove", new ColumnGenerator() {
	    
	    @Override
	    public Component generateCell(final Table source, final Object itemId, Object columnId) {
		    Button btRemovePerson = new Button("",  new Button.ClickListener() {
		        
		        @Override
		        public void buttonClick(ClickEvent event) {
		            source.getContainerDataSource().removeItem(itemId);
		        }
		    });
		    btRemovePerson.addStyleName(BaseTheme.BUTTON_LINK);
		    btRemovePerson.setIcon(new ThemeResource("../default/icons/delete.png"));
		    return btRemovePerson;
		}
	});
	return tb;
    }

    private Panel createFileDetails() {
	return NodeDetails.makeDetails(fileNode, false);
    }
    
    public GridLayout createGrid() {
	GridLayout grid = new GridLayout(2,1);
	grid.setSizeFull();
	grid.setMargin(true,true,true,false);
	grid.setSpacing(true);
	grid.addComponent(createSharePanel(), 0, 0);
	grid.addComponent(createFileDetails(), 1, 0);
	return grid;
    }
    
    public Component createPage() {
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.setMargin(true);
	mainLayout.setSizeFull();
	mainLayout.setSpacing(true);
	mainLayout.addComponent(createHeaderPanel());
	mainLayout.addComponent(createGrid());
	return mainLayout;
    }
    
    public DocumentShare() {
	fileNode = null;
	personsToShareWith = new DomainContainer(new HashSet<Person>(), Person.class);
	personsToShareWith.setContainerProperties("presentationString");
    }
    
    @Override
    public void attach() {
        super.attach();
        setCompositionRoot(createPage());
    }
}
