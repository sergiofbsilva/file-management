package module.fileManagement.presentationTier.component.groups;

import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

import com.vaadin.ui.AbstractOrderedLayout;

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
