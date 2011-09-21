package module.fileManagement.presentationTier.component.viewers;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.log.AbstractLog;

import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

public abstract class AbstractLogViewer<T extends AbstractLog> extends Label {
    private final T log;
    
    public AbstractLogViewer(T log) {
	super();
	setSizeFull();
	setStyleName(BaseTheme.BUTTON_LINK);
	setContentMode(Label.CONTENT_XHTML);
	this.log = log;
    }
    
    public T getLog() {
	return log;
    }
    
    
    public String createTargetDirLink() {
	return createDirNodeLink(getLog().getContextPath());
    }

    public final String createDirNodeLink(final ContextPath contextPath) {
	final DirNode targetDir = contextPath.getLastDirNode();
	return targetDir.isAccessible() ? String.format("<a href=\"#DocumentBrowse-%s\">%s</a>", contextPath,
		targetDir.getDisplayName()) : targetDir.getDisplayName();
    }
    
    public abstract String createTargetNodeLink();
    
    @Override
    public void attach() {
        super.attach();
        setValue(createEntry());
    }
    
    private String createEntry() {
	final StringBuilder builder = new StringBuilder();
	builder.append(String.format("(%s) Em %s, ", log.getLogTime(), createTargetDirLink()));
	builder.append(String.format(" %s ", log.getUserName()));
	builder.append(String.format(" %s ", log.getOperationString()));
	builder.append(String.format(" %s ", createTargetNodeLink()));
	return builder.toString();
    }
}
