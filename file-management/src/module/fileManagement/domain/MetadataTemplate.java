/*
 * @(#)MetadataTemplate.java
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
package module.fileManagement.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class MetadataTemplate extends MetadataTemplate_Base {

    public MetadataTemplate() {
	super();
	FileManagementSystem.getInstance().addMetadataTemplates(this);
    }

    public MetadataTemplate(String name) {
	this();
	setName(name);
    }

    public MetadataTemplate(String name, Set<String> keys) {
	this();
	setName(name);
	addKeysStrings(keys);
    }

    public static MetadataTemplate getMetadataTemplate(String name) {
	for (MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplates()) {
	    if (template.getName().equals(name)) {
		return template;
	    }
	}
	return null;
    }

    @Service
    public void delete() {
	for (MetadataKey key : getKeys()) {
	    removeKeys(key);
	}
	for (MetadataTemplate child : getChilds()) {
	    removeChilds(child);
	}
	FileManagementSystem.getInstance().removeMetadataTemplates(this);
	deleteDomainObject();
    }
    
    @Service
    public static MetadataTemplate getOrCreateInstance(String name) {
	MetadataTemplate instance = getMetadataTemplate(name);
	if (instance == null)
	    instance = new MetadataTemplate(name);
	return instance;
    }

    /**
     * 
     * @param the
     *            keys of the MetadataKey to be added to this template. If they
     *            the corresponding MetadataKey object doesn't exist, it is
     *            created
     */
    @Service
    public void addKeysStrings(Set<String> keys) {
	for (String key : keys) {
	    addKeys(MetadataKey.getOrCreateInstance(key));
	}
    }

    public Set<String> getKeysStrings() {
	Set<String> keys = new HashSet<String>();
	for (MetadataKey key : getKeys()) {
	    keys.add(key.getKeyValue());
	}
	return keys;
    }

    @SuppressWarnings("unchecked")
    public static MetadataTemplate fromExternalId(String templateOid) {
	if (templateOid == null || templateOid.isEmpty()) {
	    return null;
	} else {
	    return AbstractDomainObject.fromExternalId(templateOid);
	}
    }

    public TreeSet<MetadataKey> getAlfabeticallyOrderedKeys() {
	return new TreeSet<MetadataKey>(getKeys());
    }

}
