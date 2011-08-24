package module.fileManagement.domain;

import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;

public class Metadata extends Metadata_Base implements Comparable<Metadata> {
    
    public Metadata(final String keyValue, final String value) {
	this(MetadataKey.getOrCreateInstance(keyValue), value);
    }
    
    public Metadata(final MetadataKey key, final String value) {
	setMetadataKey(key);
	setValue(value);
    }

    public void delete() {
	removeDocument();
	deleteDomainObject();
    }
    
    public String getKeyValue() {
	return getMetadataKey().getKeyValue();
    }
    
    @Service
    public Metadata getCopy() {
	return new Metadata(getKeyValue(),getValue());
    }
    
    public void changedNow() {
	setTimestamp(new DateTime());
    }
    
    @Override
    public void setValue(String value) {
	super.setValue(value);
	changedNow();
    }

    @Override
    public int compareTo(Metadata o) {
	final DateTime timestamp = getTimestamp();
	return timestamp == null ? -1 : timestamp.compareTo(o.getTimestamp());
    }
    
    @Override
    public String toString() {
	return String.format("[%s] [%s=>%s]", getTimestamp(), getKeyValue(), getValue());
    }
    
}
