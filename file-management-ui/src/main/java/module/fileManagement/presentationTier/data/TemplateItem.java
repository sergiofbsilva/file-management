/*
 * @(#)TemplateItem.java
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
package module.fileManagement.presentationTier.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import module.fileManagement.domain.Document;
import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.vaadin.data.util.ObjectHintedProperty;
import pt.ist.vaadinframework.data.AbstractBufferedItem;
import pt.ist.vaadinframework.data.BufferedProperty;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.AbstractProperty;

/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class TemplateItem extends AbstractBufferedItem<MetadataKey, MetadataTemplate> implements ValueChangeListener {
    public class StuffProperty extends AbstractProperty {
        private final MetadataKey key;

        public StuffProperty(MetadataKey key) {
            this.key = key;
        }

        @Override
        public Object getValue() {
            final Set<Object> allValues = new HashSet<Object>();
            for (Object item : selectedDocuments.getValue()) {
                final Document document = (Document) item;
                final DocumentItem documentItem = container.getItem(document);
                final Property itemProperty = documentItem.getItemProperty(key);
                allValues.add(itemProperty == null ? null : itemProperty.getValue());
            }
            return allValues.size() == 1 ? allValues.iterator().next() : null;
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
            for (Object item : selectedDocuments.getValue()) {
                Document document = (Document) item;
                final DocumentItem documentItem = container.getItem(document);
                final Property itemProperty = documentItem.getItemProperty(key);
                itemProperty.setValue(newValue);
            }
        }

        @Override
        public Class<?> getType() {
            return Metadata.getMetadataType(key.getMetadataValueType());
        }

    }

    DocumentContainer container;
    ObjectHintedProperty<Collection> selectedDocuments;

    public TemplateItem(MetadataTemplate value, DocumentContainer container,
            ObjectHintedProperty<Collection> selectedDocumentsProperty) {
        super(MetadataTemplate.class);
        setWriteThrough(true);
        selectedDocuments = selectedDocumentsProperty;
        setDocumentContainer(container);
        addListener(this); // first null then listener then value
        setValue(value);
    }

    public void setDocumentContainer(DocumentContainer container) {
        this.container = container;
    }

    @Override
    protected Property makeProperty(MetadataKey propertyId) {
        BufferedProperty<String> property = new BufferedProperty<String>(new StuffProperty(propertyId));
        property.setWriteThrough(true);
        addItemProperty(propertyId, property);
        return property;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        final MetadataTemplate newTemplate = (MetadataTemplate) event.getProperty().getValue();
        final MetadataKey templateKey = MetadataKey.getTemplateKey();
        final Property itemProperty = getItemProperty(templateKey);
        itemProperty.setValue(getValue().getName());

        Collection<MetadataKey> oldPropertyIds = new HashSet<MetadataKey>(getItemPropertyIds());
        for (MetadataKey oldKey : oldPropertyIds) {
            if (!oldKey.equals(templateKey)) {
                removeItemProperty(oldKey);
            }
        }

        for (MetadataKey key : newTemplate.getPositionOrderedKeys()) {
            getItemProperty(key);
        }
    }

    public Collection<?> getVisibleItemProperties() {
        if (getValue() != null) {
            return getValue().getPositionOrderedKeys();
        }
        return Collections.EMPTY_LIST;
    }
}
