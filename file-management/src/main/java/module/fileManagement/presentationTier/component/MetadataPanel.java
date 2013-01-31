/*
 * @(#)MetadataPanel.java
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
package module.fileManagement.presentationTier.component;

import java.util.Collection;
import java.util.HashSet;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.presentationTier.data.DocumentContainer;
import module.fileManagement.presentationTier.data.FMSFieldFactory;
import module.fileManagement.presentationTier.data.TemplateItem;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class MetadataPanel extends Panel {

	private Select selectTemplate;
	protected TemplateItem metadataTemplateItem;
	private TransactionalForm metadataForm;
	private DocumentContainer documentContainer;
	private ObjectHintedProperty<Collection> selectedDocuments;

	private void setMetadataItem(MetadataTemplate template) {
		if (template == null) {
			metadataForm.setEnabled(false);
			metadataForm.setItemDataSource(null);
			metadataTemplateItem = null;
			return;
		}
		if (metadataTemplateItem == null) {
			selectTemplate.setEnabled(true);
			metadataTemplateItem = new TemplateItem(template, documentContainer, selectedDocuments);
			metadataForm.setFormFieldFactory(new FMSFieldFactory(FileManagementSystem.getBundleName(), template));
			metadataForm.setItemDataSource(metadataTemplateItem, metadataTemplateItem.getVisibleItemProperties());
			metadataForm.setImmediate(true);
			metadataForm.setWriteThrough(true);
			selectedDocuments.addListener(new ValueChangeListener() {

				@Override
				public void valueChange(ValueChangeEvent event) {
					metadataTemplateItem.discard();
				}
			});

		} else {
			metadataTemplateItem.setValue(template);
			metadataForm.setItemDataSource(metadataTemplateItem, metadataTemplateItem.getVisibleItemProperties());
		}
	}

	@SuppressWarnings("serial")
	public MetadataPanel(DocumentContainer documentContainer, ObjectHintedProperty<Collection> selectedDocuments) {
		super("Metadata");
		this.documentContainer = documentContainer;
		this.selectedDocuments = selectedDocuments;

		selectTemplate = new Select("Tipologia");
		selectTemplate.setImmediate(true);
		selectTemplate.setEnabled(false);
		selectTemplate.setContainerDataSource((Container) new DomainItem(FileManagementSystem.getInstance())
				.getItemProperty("metadataTemplates"));
		selectTemplate.setItemCaptionPropertyId("name");
		selectTemplate.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				final MetadataTemplate template = (MetadataTemplate) event.getProperty().getValue();
				setMetadataItem(template);
			}
		});
		addComponent(selectTemplate);
		metadataForm = new TransactionalForm(FileManagementSystem.getBundleName());
		metadataForm.setSizeFull();
		addComponent(metadataForm);
	}

	public void selectDocuments(Collection<Document> documents) {
		selectTemplate.setEnabled(true);
		if (selectTemplate.getValue() == null) {
			Collection<Document> selectedDocuments = documents;
			Collection<String> templateNames = new HashSet<String>();
			for (Document doc : selectedDocuments) {
				Metadata templateMetadata = doc.getMetadata(MetadataKey.getTemplateKey());
				if (templateMetadata != null) {
					templateNames.add((String) templateMetadata.getValue());
				}
			}
			final String templateName = templateNames.size() < 1 ? null : templateNames.iterator().next();
			selectTemplate.select(templateName == null ? null : MetadataTemplate.getMetadataTemplate(templateName));
		}
	}

	public boolean isValid() {
		try {
			metadataForm.validate();
			return true;
		} catch (InvalidValueException ive) {
			return false;
		}
	}

	public void commit() {
		metadataForm.commit();
	}

}
