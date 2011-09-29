package module.fileManagement.presentationTier.pages;

import java.util.Map;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;

import org.vaadin.dialogs.ConfirmDialog;

import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "ManageMetadataTemplates" })
public class ManageMetadataTemplates extends CustomComponent implements EmbeddedComponentContainer {

    
    @Override
    public void setArguments(Map<String,String> arguments) {
	// TODO Auto-generated method stub
	
    }
    
    private Label createTemplateDescription(final MetadataTemplate template) {
	
	String description = template.getName() + " ["; 
	
	if (template.hasAnyKeys()) {
	    
	    for(MetadataKey key : template.getKeys()) {
		description += key.getKeyValue() + ",";
	    }
	    
	    description = description.substring(0, description.length() - 1);
	}
	
	description += "]";
	
	return new Label(description);
	
    }
    
    private void createTemplateEntry(final Layout container, final MetadataTemplate template) {
	final HorizontalLayout fl = new HorizontalLayout();
	fl.setWidth("60%");
	fl.setSpacing(true);
	fl.setMargin(false, false, true, true);
	
	final Label lblName = createTemplateDescription(template);
	lblName.setStyleName("lbl-template-name");
	fl.addComponent(lblName);
	
	Button btView = new Button("Ver");
	Button btEdit = new Button("Editar");
	Button btDelete = new Button("Apagar");
	btView.setStyleName(BaseTheme.BUTTON_LINK);
	btEdit.setStyleName(BaseTheme.BUTTON_LINK);
	btDelete.setStyleName(BaseTheme.BUTTON_LINK);
	
	
	btView.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		getWindow().open(new ExternalResource("#CreateMetadataTemplate-" + template.getExternalId() + "-view"));
	    }
	});
	
	btEdit.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		getWindow().open(new ExternalResource("#CreateMetadataTemplate-" + template.getExternalId() + "-"));
	    }
	});
	
	btDelete.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		final String templateName = template.getName();
		ConfirmDialog.show(getWindow(), "Apagar Template " + templateName, "Deseja apagar o template " + templateName + " ? ", "Sim", "Não", new ConfirmDialog.Listener() {
		    
		    @Override
		    public void onClose(ConfirmDialog dialog) {
			if (dialog.isConfirmed()) {
			    template.delete();
			    container.removeComponent(fl);
			}
		    }
		});
	    }
	});
	
	HorizontalLayout hlButtons = new HorizontalLayout();
	
	hlButtons.addComponent(btView);
	hlButtons.addComponent(btEdit);
	hlButtons.addComponent(btDelete);
	hlButtons.setSpacing(true);
	hlButtons.setSizeFull();
	
	fl.addComponent(lblName);
	fl.addComponent(hlButtons);
	fl.setExpandRatio(lblName, 2);
	fl.setExpandRatio(hlButtons, 1);
	
	container.addComponent(fl);
    }
    
    @Override
    public void attach() {
	VerticalLayout vl = new VerticalLayout();
	vl.setSpacing(true);
	vl.addComponent(new Label("<h2>Templates</h2>",Label.CONTENT_XHTML));
	
	Button btCreateTemplate = new Button("+ Criar Template");
	
	btCreateTemplate.addListener(new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		getWindow().open(new ExternalResource("#CreateMetadataTemplate-"));
	    }
	});
	vl.addComponent(btCreateTemplate);
	for(MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplates()) {
	    createTemplateEntry(vl, template);
	}
	setCompositionRoot(vl);
    }
    
}
