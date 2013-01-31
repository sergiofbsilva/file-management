package module.fileManagement.presentationTier.pages;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.domain.log.FileLog;
import module.fileManagement.presentationTier.component.viewers.AbstractLogLabel;
import module.fileManagement.presentationTier.component.viewers.AbstractLogViewer;
import module.fileManagement.presentationTier.component.viewers.LogViewerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
import pt.ist.vaadinframework.VaadinFrameworkLogger;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "LogPage" })
public class LogPage extends CustomComponent implements EmbeddedComponentContainer {

	private Date selectionDate;
	private Layout logsLayout;

	@Override
	public boolean isAllowedToOpen(Map<String, String> arguments) {
		return true;
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		// TODO Auto-generated method stub

	}

	public class LogDatePredicate implements Predicate {

		private final DateTime datetime;

		public LogDatePredicate(Date date) {
			this.datetime = new DateTime(date);
		}

		@Override
		public boolean evaluate(Object arg0) {
			AbstractLog log = (AbstractLog) arg0;
			return log.getTimestamp().toDateMidnight().compareTo(datetime.toDateMidnight()) <= 0;
		}

	}

	public LogPage() {
		final long start = System.currentTimeMillis();
		VerticalLayout mainLayout = new VerticalLayout();
		selectionDate = new Date();
		logsLayout = new VerticalLayout();
		mainLayout.addStyleName("mainLayout");
		mainLayout.setMargin(true);
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		mainLayout.addComponent(createDatePickerLayout());
		mainLayout.addComponent(logsLayout);
		createLogs(logsLayout, getLogs(selectionDate));
		setCompositionRoot(mainLayout);
		final long end = System.currentTimeMillis();
		final long delta = end - start;
		VaadinFrameworkLogger.getLogger().debug("logs render time : " + delta + "ms");
	}

	public void reloadContent() {
		createLogs(logsLayout, getLogs(selectionDate));
	}

	private Component createDatePickerLayout() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		final DateField dateField = new DateField();
		dateField.setValue(selectionDate);
		dateField.setDateFormat("dd-MM-yyyy");
		dateField.setImmediate(true);
		dateField.setWriteThrough(true);

		final Button btSubmit = new Button("Ver");
		btSubmit.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				final Date dateValue = (Date) dateField.getValue();
				selectionDate = dateValue;
				createLogs(logsLayout, getLogs(selectionDate));
			}
		});

		hl.addComponent(dateField);
		hl.addComponent(btSubmit);
		return hl;
	}

	private AbstractLog getLogAtPos(TreeSet<AbstractLog> logs, int n) {
		int i = 1;

		if (logs.size() < n) {
			return logs.last();
		}

		for (AbstractLog log : logs) {
			if (i++ == n) {
				return log;
			}
		}

		return logs.first();
	}

	private Set<AbstractLog> getLogs(Date selectionDate) {
		final User currentUser = Authenticate.getCurrentUser();

		final TreeSet<AbstractLog> sortedLogs = new TreeSet<AbstractLog>(AbstractLog.TIMESTAMP_COMPARATOR);
		final List<AbstractLog> operationLogs = currentUser.getOperationLog();
		final DirNode rootDir = FileRepository.getOrCreateFileRepository(currentUser);

		sortedLogs.addAll(operationLogs);

		CollectionUtils.filter(sortedLogs, new LogDatePredicate(selectionDate));

		final TreeSet<AbstractLog> dirLogs = getLogs(rootDir, selectionDate);
		sortedLogs.addAll(dirLogs);

		if (sortedLogs.size() > 50) {
			final SortedSet<AbstractLog> subSet = sortedLogs.subSet(sortedLogs.first(), getLogAtPos(sortedLogs, 50));
			VaadinFrameworkLogger.getLogger().debug("return only subset since logs are bigger than 50 : " + subSet.toString());
			return subSet;
		}

		return sortedLogs;
	}

	private TreeSet<AbstractLog> getLogs(final DirNode dirNode, Date selectionDate) {
		TreeSet<AbstractLog> logs = new TreeSet<AbstractLog>(AbstractLog.TIMESTAMP_COMPARATOR);
		logs.addAll(dirNode.getDirLogSet());
		logs.addAll(dirNode.getTargetLogSet());
		for (AbstractFileNode node : dirNode.getChild()) {
			if (node.isFile()) {
				final Set<FileLog> fileLogSet = ((FileNode) node).getFileLogSet();
				logs.addAll(fileLogSet);
			}
			if (node.isDir()) {
				logs.addAll(getLogs((DirNode) node, selectionDate));
			}
		}
		CollectionUtils.filter(logs, new LogDatePredicate(selectionDate));
		return logs;
	}

	private void createLogs(Layout layout, Set<AbstractLog> logs) {
		layout.removeAllComponents();
		for (AbstractLog log : logs) {
			HorizontalLayout hlLog = new HorizontalLayout();
			hlLog.addStyleName("hl-log-entry");
			hlLog.setWidth("100%");
			final Component viewer = LogViewerFactory.createViewer(log);
			if (viewer instanceof AbstractLogLabel) {
				((AbstractLogLabel) viewer).setParentContainer(this);
			}
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
