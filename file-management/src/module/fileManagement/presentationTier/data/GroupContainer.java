package module.fileManagement.presentationTier.data;

import java.util.Collection;

import module.fileManagement.presentationTier.component.groups.GroupCreatorRegistry;
import module.fileManagement.presentationTier.component.groups.HasPersistentGroupCreator;
import myorg.domain.Presentable;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.data.BufferedContainer;
import pt.ist.vaadinframework.data.HintedProperty;
import pt.ist.vaadinframework.data.VBoxProperty;
import pt.ist.vaadinframework.data.reflect.DomainItem;

public class GroupContainer extends BufferedContainer<Presentable, String, DomainItem<AbstractDomainObject>> {

    
    public GroupContainer() {
   	super(new VBoxProperty(Collection.class), Presentable.class);
   	setWriteThrough(false);
    }
    
    {
//	addContainerProperty("presentationName", String.class, StringUtils.EMPTY);
    }

    @Override
    protected DomainItem<AbstractDomainObject> makeItem(HintedProperty itemId) {
	return new DomainItem<AbstractDomainObject>(itemId);
    }
    
    private void addItems(Collection<Presentable> presentables) {
	for(Presentable presentable : presentables) {
	    addItem(presentable);
	}
    }
    
    public void search(String filterText) {
	removeAllItems();
	for(HasPersistentGroupCreator groupCreator : GroupCreatorRegistry.getPersistentGroupCreators()) {
	    addItems(groupCreator.getElements(filterText));
	}
    }
    
}
