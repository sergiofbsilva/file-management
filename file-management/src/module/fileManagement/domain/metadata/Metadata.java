package module.fileManagement.domain.metadata;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.FenixFramework;
import dml.DomainClass;
import dml.DomainModel;

public class Metadata extends Metadata_Base implements Comparable<Metadata> {

    public static final Comparator<Metadata> TIMESTAMP_DESC_COMPARATOR = new Comparator<Metadata>() {

	@Override
	public int compare(Metadata o1, Metadata o2) {
	    final int compareTo = o2.getTimestamp().compareTo(o1.getTimestamp());
	    return compareTo == 0 ? o2.getExternalId().compareTo(o1.getExternalId()) : compareTo;
	}
    };

    public static final Comparator<Metadata> KEYVALUE_COMPARATOR = new Comparator<Metadata>() {

	@Override
	public int compare(Metadata arg0, Metadata arg1) {
	    return arg0.getMetadataKey().compareTo(arg1.getMetadataKey());
	}

    };

    public Metadata() {
	super();
	changedNow();
    }

    public Metadata(final String keyValue, final String value) {
	this(MetadataKey.getOrCreateInstance(keyValue), value);
    }

    public Metadata(final MetadataKey key, final String value) {
	this();
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
	return new Metadata(getKeyValue(), getValue());
    }

    public void changedNow() {
	setTimestamp(new DateTime());
    }

    public Object getRealValue() {
	return getValue();
    }

    public void setRealValue(Object value) {
	setValue((String) value);
	changedNow();
    }

    public String getPresentationValue() {
	return getValue();
    }

    @Override
    public int compareTo(Metadata o) {
	final DateTime timestamp = getTimestamp();
	return timestamp == null ? -1 : timestamp.compareTo(o.getTimestamp());
    }

    private static boolean isMetadataClass(DomainClass clazz) {
	final DomainModel domainModel = FenixFramework.getDomainModel();
	final DomainClass metadataClass = domainModel.findClass(Metadata.class.getCanonicalName());
	if (clazz == null) {
	    return false;
	}
	if (clazz.equals(metadataClass)) {
	    return true;
	}
	if (clazz.hasSuperclass()) {
	    return isMetadataClass((DomainClass) clazz.getSuperclass());
	}
	return false;
    }

    public static Set<Class<? extends Metadata>> getAvailableMetadataClasses() {
	final Set<Class<? extends Metadata>> availableTypes = new HashSet();
	final DomainModel domainModel = FenixFramework.getDomainModel();
	for (DomainClass clazz : domainModel.getDomainClasses()) {
	    if (isMetadataClass(clazz)) {
		try {
		    availableTypes.add((Class<? extends Metadata>) (Class.forName(clazz.getFullName())));
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		}
	    }
	}
	return availableTypes;
    }

    public static Set<Class> getAvailableMetadataTypes() {
	final Set<Class> availableTypes = new HashSet<Class>();
	for (Class<? extends Metadata> clazz : getAvailableMetadataClasses()) {
	    availableTypes.add(getMetadataType(clazz));
	}
	return availableTypes;
    }

    public static Class getMetadataType(Class<? extends Metadata> entryValueClass) {
	Method method;
	try {
	    method = entryValueClass.getMethod("getRealValue");
	    return method.getReturnType();
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
