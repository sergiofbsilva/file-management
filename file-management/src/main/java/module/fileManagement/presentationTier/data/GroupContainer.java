package module.fileManagement.presentationTier.data;


import module.fileManagement.presentationTier.component.groups.GroupCreatorRegistry;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroupCreator;
import pt.ist.bennu.core.domain.Presentable;

public class GroupContainer extends AbstractSearchContainer {
    public GroupContainer() {
	super(Presentable.class);
    }

    {
	// addContainerProperty("presentationName", String.class,
	// StringUtils.EMPTY);
    }

    @Override
    public void search(String filterText) {
	removeAllItems();
	for (HasPersistentGroupCreator groupCreator : GroupCreatorRegistry.getPersistentGroupCreators()) {
	    addItems(groupCreator.getElements(filterText));
	}
    }
}
