/*
 * @(#)DocumentContainer.java
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
import pt.ist.vaadinframework.data.AbstractBufferedContainer;

/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentContainer extends AbstractBufferedContainer<Document, Object, DocumentItem> {
    public DocumentContainer() {
	super(Document.class);
    }

    {
	addContainerProperty("displayName", String.class, null);
    }

    @Override
    protected DocumentItem makeItem(Document itemId) {
	return new DocumentItem(itemId);
    }

    @Override
    protected DocumentItem makeItem(Class<? extends Document> type) {
	return new DocumentItem(type);
    }
}
