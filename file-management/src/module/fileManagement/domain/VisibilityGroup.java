package module.fileManagement.domain;

import java.io.Serializable;

import myorg.domain.groups.PersistentGroup;

public class VisibilityGroup implements Serializable {

    public static enum VisibilityOperation {
	READ("label.visibilityGroup.read"), WRITE("label.visibilityGroup.write");

	private final String key;

	private VisibilityOperation(final String key) {
	    this.key = key;
	}

	public String getDescription() {
	    return FileManagementSystem.getMessage(key);
	}

	public boolean isMember(final VisibilityOperation[] visibilityOperations) {
	    for (final VisibilityOperation visibilityOperation : visibilityOperations) {
		if (visibilityOperation == this) {
		    return true;
		}
	    }
	    return false;
	}
    }

    public final PersistentGroup persistentGroup;
    public VisibilityOperation visibilityOperation;

    public VisibilityGroup(final PersistentGroup persistentGroup, final VisibilityOperation visibilityOperation) {
	this.persistentGroup = persistentGroup;
	this.visibilityOperation = visibilityOperation;
    }

    public String getDescription() {
	final StringBuilder groupString = new StringBuilder();
	groupString.append(persistentGroup.getName());
	groupString.append(" - ");
	groupString.append(visibilityOperation.getDescription());
	return groupString.toString();
    }

    public void setVisibilityOperation(final VisibilityOperation visibilityOperation) {
        this.visibilityOperation = visibilityOperation;
    }
    
    @Override
    public boolean equals(Object obj) {
	if (obj instanceof VisibilityGroup) {
	    final VisibilityGroup group = (VisibilityGroup) obj;
	    return group.persistentGroup == persistentGroup;
	}
	return false;
    }

}
