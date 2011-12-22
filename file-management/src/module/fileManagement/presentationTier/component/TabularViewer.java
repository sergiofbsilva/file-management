package module.fileManagement.presentationTier.component;

import java.util.Arrays;
import java.util.Collection;

import module.vaadin.ui.BennuTheme;
import pt.ist.bennu.ui.viewers.ViewerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Item.PropertySetChangeEvent;
import com.vaadin.data.Item.PropertySetChangeListener;
import com.vaadin.data.Item.PropertySetChangeNotifier;
import com.vaadin.data.Item.Viewer;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class TabularViewer extends VerticalLayout implements Viewer, PropertySetChangeListener {
    private Item itemDatasource;
    private ViewerFactory viewerFactory;
    Collection<?> propertyIds;

    private static final String CAPTION_CSS = "v-tabularviewer-caption";

    public TabularViewer() {
	addStyleName(BennuTheme.PROPERTIES);
	propertyIds = null;
    }

    public TabularViewer(ViewerFactory viewerFactory) {
	this();
	setViewerFactory(viewerFactory);
    }

    public TabularViewer(Item newDataSource, ViewerFactory viewerFactory) {
	this();
	setItemDataSource(newDataSource);
	setViewerFactory(viewerFactory);
    }

    public void setViewerFactory(ViewerFactory viewerFactory) {
	this.viewerFactory = viewerFactory;
    }

    public ViewerFactory getViewerFactory() {
	return viewerFactory;
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
	setItemDataSource(newDataSource, newDataSource != null ? newDataSource.getItemPropertyIds() : null);
    }

    public void setItemDataSource(Item newDataSource, Collection<?> propertyIds) {

	if (itemDatasource != null) {
	    if (itemDatasource instanceof PropertySetChangeNotifier) {
		((PropertySetChangeNotifier) itemDatasource).removeListener(this);
	    }
	}

	this.itemDatasource = newDataSource;
	this.propertyIds = propertyIds;
	updateContent();

	if (itemDatasource instanceof PropertySetChangeNotifier) {
	    ((PropertySetChangeNotifier) itemDatasource).addListener(this);
	}

    }

    public void setVisibleItemProperties(Collection<?> visibleProperties) {
	setItemDataSource(itemDatasource, visibleProperties);
    }

    public void setVisibleItemProperties(Object[] visibleProperties) {
	setItemDataSource(itemDatasource, Arrays.asList(visibleProperties));
    }

    @Override
    public Item getItemDataSource() {
	return itemDatasource;
    }

    @Override
    public void itemPropertySetChange(PropertySetChangeEvent event) {
	updateContent();
    }

    // private Label makeCaption(Component valueComponent) {
    // final Label label = new Label(valueComponent.getCaption());
    // label.addStyleName(CAPTION_CSS);
    // label.setSizeFull();
    //
    // valueComponent.setCaption(null);
    // valueComponent.setSizeUndefined();
    // return label;
    // }
    //
    // public HorizontalLayout addLine(Component valueComponent) {
    // final HorizontalLayout line = new HorizontalLayout();
    // line.setSpacing(true);
    // line.addStyleName(BennuTheme.PROPERTIES);
    // line.setWidth("100%");
    //
    // final Label label = makeCaption(valueComponent);
    //
    // line.addComponent(label);
    // line.addComponent(valueComponent);
    // line.setExpandRatio(valueComponent, 1f);
    // addComponent(line);
    // return line;
    // }

    public HorizontalLayout addLine(Component valueComponent) {
	final HorizontalLayout line = new HorizontalLayout();
	line.setSpacing(true);
	line.addStyleName(BennuTheme.PROPERTIES);
	line.setWidth("100%");

	final Label label = new Label(valueComponent.getCaption());
	label.addStyleName(CAPTION_CSS);
	label.setSizeUndefined();

	valueComponent.setCaption(null);
	valueComponent.setSizeUndefined();

	line.addComponent(label);
	line.addComponent(valueComponent);
	line.setExpandRatio(valueComponent, 1f);
	addComponent(line);
	return line;
    }

    private void updateContent() {
	removeAllComponents();
	HorizontalLayout line = null;
	// GridLayout grid = new GridLayout(2,propertyIds.size());
	// grid.setSizeFull();
	// grid.setSpacing(true);
	// grid.setMargin(true);
	// int i = 0;
	for (Object propertyId : propertyIds) {
	    Property.Viewer value = viewerFactory.createViewer(itemDatasource, propertyId, this);
	    final Property itemProperty = itemDatasource.getItemProperty(propertyId);
	    value.setPropertyDataSource(itemProperty);
	    AbstractComponent valueComponent = (AbstractComponent) value;
	    line = addLine(valueComponent);
	    // grid.addComponent(makeCaption(valueComponent), 0, i);
	    // grid.addComponent(valueComponent, 1, i++);
	}

	if (line != null) {
	    line.addStyleName(BennuTheme.PROPERTIES_LAST);
	}
	// addComponent(grid);
    }
}
