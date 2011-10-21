package module.fileManagement.presentationTier.component.viewers;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.VisibilityGroup;
import module.fileManagement.domain.VisibilityList;
import myorg.domain.groups.AnyoneGroup;
import myorg.domain.groups.EmptyGroup;
import myorg.domain.groups.SingleUserGroup;

import com.vaadin.data.Property.ValueChangeListener;

public class VisibilityListViewer extends com.vaadin.ui.Label implements ValueChangeListener {

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

    @Override
    public String toString() {
	String key;
	final VisibilityList list = getPropertyValue();
	if (isPublicAccess()) {
	    key = "label.visibility.public";
	} else {
	    if ((list.size() == 1 && list.contains(SingleUserGroup.class))
		    || (list.size() == 2 && list.contains(SingleUserGroup.class) && list.contains(EmptyGroup.class))) {
		key = "label.visibility.private";
	    } else {
		key = "label.visibility.shared";
	    }
	}
	return FileManagementSystem.getMessage(key);
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
