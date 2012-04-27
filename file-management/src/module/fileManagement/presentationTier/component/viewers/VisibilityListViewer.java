/*
 * @(#)VisibilityListViewer.java
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
package module.fileManagement.presentationTier.component.viewers;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.VisibilityGroup;
import module.fileManagement.domain.VisibilityList;
import myorg.domain.groups.AnyoneGroup;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.SingleUserGroup;

import com.vaadin.data.Property.ValueChangeListener;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class VisibilityListViewer extends com.vaadin.ui.Label implements ValueChangeListener {

    public enum VisibilityType {
	PRIVATE("label.visibility.private"), SHARED("label.visibility.shared"), PUBLIC("label.visibility.public");

	private final String typeKey;

	VisibilityType(String type) {
	    typeKey = type;
	}

	public String getTypeKey() {
	    return typeKey;
	}
    }

    public VisibilityListViewer() {
	addStyleName("visibility-viewer");
    }

    public VisibilityList getPropertyValue() {
	return (VisibilityList) getValue();
    }

    // public String getVisibility() {
    // final PersistentGroup readGroup = getReadGroup();
    // final String key = readGroup instanceof SingleUserGroup ?
    // "label.visibility.private"
    // : readGroup instanceof AnyoneGroup ? "label.visibility.public" :
    // "label.visibility.shared";
    // return FileManagementSystem.getMessage(key);
    // }

    public boolean isPublicAccess() {
	for (VisibilityGroup group : getPropertyValue()) {
	    if (group.persistentGroup instanceof AnyoneGroup) {
		return true;
	    }
	}
	return false;
    }

    protected VisibilityType getVisibilityType() {
	final VisibilityList list = getPropertyValue();
	if (isPublicAccess()) {
	    return VisibilityType.PUBLIC;
	} else {
	    if ((list.size() == 1 && list.contains(SingleUserGroup.class))
		    || (list.size() == 2 && list.contains(SingleUserGroup.class) && list.contains(EmptyGroup.class))) {
		return VisibilityType.PRIVATE;
	    } else {
		return VisibilityType.SHARED;
	    }
	}
    }

    @Override
    public String toString() {
	return FileManagementSystem.getMessage(getVisibilityType().getTypeKey());
    }

    @Override
    public String getDescription() {
	final StringBuilder htmlDescription = new StringBuilder();
	htmlDescription.append("<ul>");
	for (VisibilityGroup group : getPropertyValue()) {
	    htmlDescription.append(String.format("<li>%s</li>", group.getDescription()));
	}
	htmlDescription.append("</ul>");
	return htmlDescription.toString();
    }
}
