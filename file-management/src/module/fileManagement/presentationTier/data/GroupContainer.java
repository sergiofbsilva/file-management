package module.fileManagement.presentationTier.data;

import java.util.Collection;

import module.fileManagement.presentationTier.component.groups.GroupCreatorRegistry;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroupCreator;
import myorg.domain.Presentable;
import pt.ist.vaadinframework.data.AbstractBufferedContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;

public class GroupContainer extends AbstractBufferedContainer<Presentable, Object, DomainItem<Presentable>> {
    public GroupContainer() {
	super(Presentable.class);
    }

    {
	// addContainerProperty("presentationName", String.class,
	// StringUtils.EMPTY);
    }

    private void addItems(Collection<Presentable> presentables) {
	for (Presentable presentable : presentables) {
	    addItem(presentable);
	}
    }

    public void search(String filterText) {
	removeAllItems();
	for (HasPersistentGroupCreator groupCreator : GroupCreatorRegistry.getPersistentGroupCreators()) {
	    addItems(groupCreator.getElements(filterText));
	}
    }

    @Override
    protected DomainItem<Presentable> makeItem(Presentable presentable) {
	return new DomainItem<Presentable>(presentable);
    }

    @Override
    protected DomainItem<Presentable> makeItem(Class<? extends Presentable> type) {
	return new DomainItem<Presentable>(type);
    }
}
