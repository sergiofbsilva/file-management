package module.fileManagement.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

public class MetadataTemplate extends MetadataTemplate_Base {
    
    public  MetadataTemplate() {
        super();
        FileManagementSystem.getInstance().addMetadataTemplates(this);
    }
    
    public MetadataTemplate(String name) {
	this();
	setName(name);
    }
    
    public static MetadataTemplate getMetadataTemplate(String name) {
	for(MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplates()) {
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
    public void setKeysStrings(Set<String> keys) {
	for(String key : keys) {
	    addKeys(MetadataKey.getOrCreateInstance(key));
	}
    }
    
    public Set<String> getKeysStrings() {
	Set<String> keys = new HashSet<String>();
	for(MetadataKey key : getKeys()) {
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
