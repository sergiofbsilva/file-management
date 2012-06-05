/*
 * @(#)DocumentItem.java
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

import module.fileManagement.domain.Document;
import module.fileManagement.domain.metadata.Metadata;
import module.fileManagement.domain.metadata.MetadataKey;
import pt.ist.vaadinframework.data.BufferedProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentItem extends DomainItem<Document> {
    public class MetadataKeyProperty extends AbstractProperty {
	private final MetadataKey key;

	public MetadataKeyProperty(MetadataKey key) {
	    this.key = key;
	}

	@Override
	public Object getValue() {
	    Document document = DocumentItem.this.getValue();
	    if (document != null) {
		Metadata metadata = document.getMetadataRecentlyChanged(key);
		return metadata != null ? metadata.getRealValue() : null;
	    }
	    return null;
	}

	@Override
	public void setValue(Object newValue) {
	    Document document = DocumentItem.this.getValue();
	    document.addMetadata(key.createMetadata(newValue));
	}

	@Override
	public Class<?> getType() {
	    return String.class;
	}
    }

    public DocumentItem(Document document) {
	super(document);
    }

    public DocumentItem(Class<? extends Document> type) {
	super(type);
    }

    {
	setWriteThrough(false);
    }

    @Override
    protected Property makeProperty(Object propertyId) {
	if (propertyId instanceof MetadataKey) {
	    BufferedProperty<String> property = new BufferedProperty<String>(new MetadataKeyProperty((MetadataKey) propertyId));
	    addItemProperty(propertyId, property);
	    return property;
	}
	return super.makeProperty(propertyId);
    }
}
