package module.fileManagement.presentationTier.pages;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.presentationTier.component.viewers.AbstractLogViewer;
import module.fileManagement.presentationTier.component.viewers.LogViewerFactory;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "LogPage" })
public class LogPage extends CustomComponent implements EmbeddedComponentContainer {

    @Override
    public void setArguments(Map<String, String> arguments) {
	// TODO Auto-generated method stub

    }

    public LogPage() {
	final long start = System.currentTimeMillis();
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.addStyleName("mainLayout");
	mainLayout.setMargin(true);
	mainLayout.setSizeFull();
	mainLayout.setSpacing(true);
	createLogs(mainLayout, getLogs());
	setCompositionRoot(mainLayout);
	final long end = System.currentTimeMillis();
	final long delta = end - start;
	System.out.println("log time: " + delta + " ms");
    }

    private Set<AbstractLog> getLogs() {
	final User currentUser = UserView.getCurrentUser();
	final TreeSet<AbstractLog> sortedLogs = new TreeSet<AbstractLog>(AbstractLog.TIMESTAMP_COMPARATOR);
	sortedLogs.addAll(currentUser.getOperationLog());
	final DirNode rootDir = FileRepository.getOrCreateFileRepository(currentUser);
	sortedLogs.addAll(getLogs(rootDir));
	// sortedLogs.addAll(getLogs(currentUser.getTrash()));
	return sortedLogs;
    }

    private Set<AbstractLog> getLogs(final DirNode dirNode) {
	Set<AbstractLog> logs = new HashSet<AbstractLog>();
	logs.addAll(dirNode.getDirLogSet());
	logs.addAll(dirNode.getTargetLogSet());
	for (AbstractFileNode node : dirNode.getChild()) {
	    if (node.isFile()) {
		logs.addAll(((FileNode) node).getFileLogSet());
	    }
	    if (node.isDir()) {
		logs.addAll(getLogs((DirNode) node));
	    }
	}
	return logs;
    }

    private void createLogs(Layout layout, Set<AbstractLog> logs) {
	for (AbstractLog log : logs) {
	    HorizontalLayout hlLog = new HorizontalLayout();
	    hlLog.addStyleName("hl-log-entry");
	    hlLog.setWidth("100%");
	    final Component viewer = LogViewerFactory.createViewer(log);
	    viewer.setSizeUndefined();
	    hlLog.addComponent(viewer);

	    // hlLog.setExpandRatio(viewer, 0.7f);
	    if (viewer instanceof AbstractLogViewer) {
		final AbstractLogViewer abstractLogViewer = (AbstractLogViewer) viewer;
		if (abstractLogViewer.hasOperations()) {
		    hlLog.setSpacing(true);
		    final List<Component> operations = abstractLogViewer.getOperations();
		    for (Component operation : operations) {
			hlLog.addComponent(operation);
			hlLog.setExpandRatio(operation, 1);
			hlLog.setComponentAlignment(operation, Alignment.MIDDLE_LEFT);
		    }
		}
	    }
	    layout.addComponent(hlLog);
	}
    }
}
