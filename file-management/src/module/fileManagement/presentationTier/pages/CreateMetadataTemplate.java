package module.fileManagement.presentationTier.pages;

import java.util.Map;

import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "CreateMetadataTemplate" } , args = {"template", "readOnly"} )
public class CreateMetadataTemplate extends CustomComponent implements EmbeddedComponentContainer {

    private int keyIndex = 1;
    private MetadataTemplate template;
    private boolean readOnly = false;
    
    public String getNextKeyIndex() {
	return "key_" + keyIndex++;
    }
    
    @Service
    private void editOrCreateTemplate(Form form) {
	final String templateName = (String) form.getItemProperty("name").getValue();
	MetadataTemplate currentTemplate;
	if (template != null) {
	    currentTemplate = template;
	    currentTemplate.setName(templateName);
	    for(MetadataKey key : template.getKeys()) {
		currentTemplate.removeKeys(key);
	    }
	} else {
	    currentTemplate = new MetadataTemplate(templateName);
	}
	for (Object prop : form.getItemPropertyIds()) {
	    String propId = (String) prop;
	    if (propId.startsWith("key_")) {
		String keyName = (String) form.getItemProperty(propId).getValue();
		currentTemplate.addKeys(MetadataKey.getOrCreateInstance(keyName));
	    }
	}
    }

    public class KeyField extends CustomField {

	private final Form underlingForm;
	private final Object propId;
	private final TextField fieldKey;

	public KeyField(Form form, Object id) {
	    this.underlingForm = form;
	    this.propId = id;
	    fieldKey = new TextField();
	    
	    HorizontalLayout hl = new HorizontalLayout();
	    hl.setSpacing(true);
	    hl.addComponent(fieldKey);

	    Button btDelete = new Button("Apagar");
	    btDelete.setStyleName(BaseTheme.BUTTON_LINK);
	    btDelete.addListener(new ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
		    underlingForm.removeItemProperty(propId);
		}
	    });
	    hl.addComponent(btDelete);
	    setCompositionRoot(hl);
	}

	@Override
	protected void setInternalValue(Object newValue) {
	    super.setInternalValue(newValue);
	    fieldKey.setValue(newValue);
	}

	@Override
	public String getCaption() {
	    return "Chave";
	}
	
	@Override
	public Object getValue() {
	    return fieldKey.getValue();
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
	    super.commit();
	    fieldKey.commit();
	}

	@Override
	public void discard() throws SourceException {
	    super.discard();
	    fieldKey.discard();
	}

	@Override
	public Class<?> getType() {
	    return String.class;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
	    fieldKey.setReadOnly(readOnly);
	}

    }
    
    
    @Override
    public void attach() {
	VerticalLayout layout = new VerticalLayout();

	final Form form = new Form();
	form.setReadOnly(readOnly);
	
	final TextField txtName = new TextField("Nome");
	form.addItemProperty("name", txtName);
	
	if (template != null) {
	    txtName.setValue(template.getName());
	    for(MetadataKey key : template.getKeys()) {
		String propId = getNextKeyIndex();
		final KeyField keyField = new KeyField(form,propId);
		keyField.setValue(key.getKeyValue());
		form.addField(propId, keyField);
	    }
	}
	
	
	layout.addComponent(form);
	Button btAddField = new Button("Adicionar Chave");
	btAddField.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		final String propId = getNextKeyIndex();
		form.addField(propId, new KeyField(form, propId));
	    }
	});

	Button btCreateTemplate = new Button((template == null ? "Criar" : "Editar ") + " Template");
	btCreateTemplate.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		form.commit();
		editOrCreateTemplate(form);
		Notification not = new Notification("Sucesso", "Template criado com sucesso!",Notification.TYPE_TRAY_NOTIFICATION);
		not.setDelayMsec(500);
		getWindow().showNotification(not);
		goToManageTemplates();
	    }
	});

	Button btCancel = new Button ("Voltar", new ClickListener() {
	    
	    @Override
	    public void buttonClick(ClickEvent event) {
		goToManageTemplates();
	    }
	});
	
	HorizontalLayout hl = new HorizontalLayout();
	hl.setSpacing(true);
	if (!readOnly) {
	    hl.addComponent(btAddField);
	    hl.addComponent(btCreateTemplate);
	}
	hl.addComponent(btCancel);
	layout.addComponent(hl);
	setCompositionRoot(layout);
    }

    @Override
    public void setArguments(Map<String,String> arguments) {
	final String templateOid = arguments.get("template");
	final String readOnly = arguments.get("readOnly");
	setTemplate(templateOid);
	if (!StringUtils.isEmpty(readOnly)) {
	    this.readOnly = true;
	}
    }
    
    private void setTemplate(String templateOid) {
	template = MetadataTemplate.fromExternalId(templateOid);
    }
    
    private void goToManageTemplates() {
	getWindow().open(new ExternalResource("#ManageMetadataTemplates"));
    }
}
