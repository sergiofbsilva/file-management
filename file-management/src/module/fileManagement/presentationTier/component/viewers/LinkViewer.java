package module.fileManagement.presentationTier.component.viewers;

import java.net.URL;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.data.Property.Viewer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;

public class LinkViewer extends CustomComponent implements Viewer, ValueChangeListener {
    private Property source;

    protected String urlText;

    private String targetName;

    public LinkViewer() {
	setCompositionRoot(new Label());
    }

    public LinkViewer(Property newDataSource) {
	this();
	setPropertyDataSource(newDataSource);
    }

    public LinkViewer(String urlText, Property newDataSource) {
	this(newDataSource);
	this.urlText = urlText;
    }

    public LinkViewer(String urlText, String targetName, Property newDataSource) {
	this(urlText, newDataSource);
	this.targetName = targetName;
    }

    public LinkViewer(String urlText, String targetName) {
	this(urlText, targetName, null);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
	if (source != null) {
	    if (source instanceof ValueChangeNotifier) {
		((ValueChangeNotifier) source).removeListener(this);
	    }
	}
	this.source = newDataSource;
	if (source instanceof ValueChangeNotifier) {
	    ((ValueChangeNotifier) source).addListener(this);
	}
	updateContent();
    }

    @Override
    public Property getPropertyDataSource() {
	return source;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
	updateContent();
    }

    protected void updateContent() {
	URL url = source != null ? (URL) source.getValue() : null;
	if (url != null) {
	    Link link = new Link(urlText != null ? urlText : url.toString(), new ExternalResource(url));
	    if (targetName != null) {
		link.setTargetName(targetName);
	    }
	    setCompositionRoot(link);
	} else {
	    setCompositionRoot(new Label());
	}
    }
}
