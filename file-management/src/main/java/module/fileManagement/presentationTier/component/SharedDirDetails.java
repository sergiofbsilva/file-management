/*
 * @(#)SharedDirDetails.java
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

import module.fileManagement.domain.AbstractFileNode;
import pt.ist.vaadinframework.data.reflect.DomainItem;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class SharedDirDetails extends DirDetails {

	public SharedDirDetails(DomainItem<AbstractFileNode> nodeItem, boolean operationsVisible, boolean infoVisible) {
		super(nodeItem, operationsVisible, infoVisible);
	}

	public SharedDirDetails(DomainItem<AbstractFileNode> nodeItem) {
		this(nodeItem, true, true);
	}

}
