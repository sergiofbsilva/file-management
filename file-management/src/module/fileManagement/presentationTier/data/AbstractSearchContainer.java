package module.fileManagement.presentationTier.data;

import java.util.Collection;
import java.util.List;

import com.vaadin.data.Property;

import myorg.domain.Presentable;
import pt.ist.vaadinframework.data.AbstractBufferedContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;

public abstract class AbstractSearchContainer extends AbstractBufferedContainer<Presentable, Object, DomainItem<Presentable>> {

    public AbstractSearchContainer(Class elementType, Hint... hints) {
	super(elementType, hints);
    }

    public AbstractSearchContainer(Property wrapped, Class elementType, Hint... hints) {
	super(wrapped, elementType, hints);
    }

    public AbstractSearchContainer(List elements, Class elementType, Hint... hints) {
	super(elements, elementType, hints);
    }

    public abstract void search(String filterText);

    protected void addItems(Collection<Presentable> presentables) {
        for (Presentable presentable : presentables) {
            addItem(presentable);
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