package module.fileManagement.presentationTier.component.groups;

import java.util.Collection;

import myorg.domain.Presentable;

public interface HasPersistentGroupCreator {

    public HasPersistentGroup createGroupFor(final Object itemId);

    public Collection<Presentable> getElements(String filterText);

}
