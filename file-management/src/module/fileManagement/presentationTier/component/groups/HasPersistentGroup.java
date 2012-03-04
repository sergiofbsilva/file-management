/*
 * @(#)HasPersistentGroup.java
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
package module.fileManagement.presentationTier.component.groups;

import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import com.vaadin.ui.AbstractOrderedLayout;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public abstract class HasPersistentGroup {

    public abstract PersistentGroup getPersistentGroup();

    public abstract void renderGroupSpecificLayout(final AbstractOrderedLayout abstractOrderedLayout);
    
    @Override
    public boolean equals(Object obj) {
	if (obj instanceof HasPersistentGroup) {
	    return getPersistentGroup().getPresentationName().equals(((HasPersistentGroup) obj).getPersistentGroup().getPresentationName());
	}
	return false;
    }
    
    @Override
    public int hashCode() {
	return getPersistentGroup().getPresentationName().hashCode();
    }
    
    public boolean shouldCreateLink() {
	return false;
    }
    
    public User getUser() {
	throw new InvalidOperationException("Not implemented.");
    }
}
