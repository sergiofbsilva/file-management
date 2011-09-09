package module.fileManagement.presentationTier.component.viewers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.VisibilityList;

import org.joda.time.DateTime;

import pt.ist.vaadinframework.ui.DefaultViewerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.Viewer;
import com.vaadin.ui.Component;

public class FMSViewerFactory extends DefaultViewerFactory {
    
    private static FMSViewerFactory instance;
    private Map<Class<?>,Class<? extends Viewer>> classViewerMap = new HashMap<Class<?>, Class<? extends Viewer>>();
    
    private FMSViewerFactory() {
	super(FileManagementSystem.getBundle());
    }
    
    {
	register(DateTime.class, DateTimeViewer.class);
   	register(URL.class, LinkViewer.class);
   	register(VisibilityList.class, VisibilityListViewer.class);
    }
    
    private void register(Class<?> type, Class<? extends Viewer> viewer) {
	classViewerMap.put(type, viewer);
    }
    
    private Viewer getNewViewer(Class<?> type) throws InstantiationException, IllegalAccessException {
	final Class<? extends Viewer> viewerClass = classViewerMap.get(type);
	return viewerClass == null ? null : viewerClass.newInstance();
    }
    
    
    public Class<?> getType(Class<Viewer> viewer) {
	for(Entry<Class<?>, Class<? extends Viewer>> entry : classViewerMap.entrySet()) {
	    if (entry.getValue().equals(viewer)) {
		return entry.getKey();
	    }
	}
	return null;
    }
    
    public static FMSViewerFactory getInstance() {
	if (instance == null) {
	    instance = new FMSViewerFactory();
	}
	return instance;
    }
    
    @Override
    protected Viewer makeViewer(Item item, Object propertyId, Component uiContext) {
	final Property itemProperty = item.getItemProperty(propertyId);
	final Viewer viewer;
	try {
	    viewer = getNewViewer(itemProperty.getType());
	} catch (InstantiationException e) {
	    throw new UnsupportedOperationException(e);
	} catch (IllegalAccessException e) {
	    throw new UnsupportedOperationException(e);
	}
	return viewer != null ? viewer : super.makeViewer(item, propertyId, uiContext);
    }
    
    @Override
    public String makeDescription(Item item, Object propertyId, Component uiContext) {
        return super.makeDescription(item, propertyId, uiContext);
    }
}
