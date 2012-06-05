/*
 * @(#)CreateMetadataTemplate.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.presentationTier.pages;

import java.util.Map;
import java.util.Set;

import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.MetadataTemplateRule;
import module.fileManagement.domain.metadata.StringMetadata;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "CreateMetadataTemplate" }, args = { "template", "readOnly" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class CreateMetadataTemplate extends CustomComponent implements EmbeddedComponentContainer {

    private int keyIndex = 1;
    private MetadataTemplate template;
    private boolean readOnly = false;

    class KeyProp implements Comparable<KeyProp> {
	private Integer index;

	public KeyProp() {
	    this.index = keyIndex++;
	}

	public KeyProp(final Integer index) {
	    this.index = index;
	    keyIndex = index;
	}

	public Integer getIndex() {
	    return index;
	}

	public void setIndex(final Integer index) {
	    this.index = index;
	}

	@Override
	public int compareTo(KeyProp o) {
	    return getIndex().compareTo(o.getIndex());
	}
    }

    @Service
    private void editOrCreateTemplate(Form form) {
	final String templateName = (String) form.getItemProperty("name").getValue();
	MetadataTemplate currentTemplate;
	if (template != null) {
	    currentTemplate = template;
	    currentTemplate.setName(templateName);
	    currentTemplate.clear();
	} else {
	    currentTemplate = new MetadataTemplate(templateName);
	}

	for (Object propId : form.getItemPropertyIds()) {
	    if (propId instanceof KeyProp) {
		final KeyField itemProperty = (KeyField) form.getItemProperty(propId);
		String keyName = (String) itemProperty.getValue();
		Class<? extends Metadata> metadataClassType = itemProperty.getMetadataType();
		final MetadataKey key = new MetadataKey(keyName, Boolean.FALSE, metadataClassType);
		currentTemplate.addKey(key, ((KeyProp) propId).getIndex(), itemProperty.getRequired());
	    }
	}
    }

    public class KeyField extends CustomField {

	private final Form underlingForm;
	private final Object propId;
	private final TextField fieldKey;
	private final Select selectType;
	private final CheckBox requiredBox;

	public KeyField(Form form, Object id, Class<? extends Metadata> metadataType, Boolean required) {
	    this(form, id);
	    selectType.select(metadataType.getSimpleName());
	    requiredBox.setValue(required);
	}

	public KeyField(Form form, Object id) {
	    this.underlingForm = form;
	    this.propId = id;
	    fieldKey = new TextField();
	    requiredBox = new CheckBox("Required");

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

	    selectType = getMetadataTypeSelect();
	    hl.addComponent(selectType);
	    hl.addComponent(requiredBox);
	    hl.addComponent(btDelete);

	    setCompositionRoot(hl);
	}

	private Select getMetadataTypeSelect() {
	    final Select selectMetadataType = new Select();
	    selectMetadataType.setNullSelectionAllowed(false);
	    final Set<Class<? extends Metadata>> types = Metadata.getAvailableMetadataClasses();
	    BeanContainer<String, Class<? extends Metadata>> typeContainer = new BeanContainer<String, Class<? extends Metadata>>(
		    Class.class);
	    typeContainer.setBeanIdProperty("simpleName");
	    typeContainer.addAll(types);
	    selectMetadataType.setContainerDataSource(typeContainer);
	    selectMetadataType.setItemCaptionPropertyId("simpleName");
	    selectMetadataType.select(StringMetadata.class.getSimpleName());
	    return selectMetadataType;
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

	public Class<? extends Metadata> getMetadataType() {
	    final Object itemId = selectType.getValue();
	    return (Class<? extends Metadata>) ((BeanItem) selectType.getItem(itemId)).getBean();
	}

	public Boolean getRequired() {
	    return (Boolean) requiredBox.getValue();
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
	    for (MetadataTemplateRule rule : template.getPositionOrderedRules()) {
		KeyProp propId = new KeyProp(rule.getPosition());
		final MetadataKey key = rule.getKey();
		final KeyField keyField = new KeyField(form, propId, key.getMetadataValueType(), rule.getRequired());
		keyField.setValue(key.getKeyValue());
		form.addField(propId, keyField);
	    }
	}

	layout.addComponent(form);
	Button btAddField = new Button("Adicionar Chave");
	btAddField.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		final KeyProp propId = new KeyProp();
		form.addField(propId, new KeyField(form, propId));
	    }
	});

	Button btCreateTemplate = new Button((template == null ? "Criar" : "Editar ") + " Template");
	btCreateTemplate.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		form.commit();
		editOrCreateTemplate(form);
		Notification not = new Notification("Sucesso", "Template criado com sucesso!",
			Notification.TYPE_TRAY_NOTIFICATION);
		not.setDelayMsec(500);
		getWindow().showNotification(not);
		goToManageTemplates();
	    }
	});

	Button btCancel = new Button("Voltar", new ClickListener() {

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
    public void setArguments(Map<String, String> arguments) {
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
	EmbeddedApplication.open(getApplication(), ManageMetadataTemplates.class);
    }

    @Override
    public boolean isAllowedToOpen(Map<String, String> arg0) {
	return true;
    }
}
