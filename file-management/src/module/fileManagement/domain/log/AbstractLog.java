package module.fileManagement.domain.log;

import java.util.Comparator;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.FileManagementSystem;
import myorg.domain.User;

import org.joda.time.DateTime;

public abstract class AbstractLog extends AbstractLog_Base {
    
    public static final Comparator<AbstractLog> TIMESTAMP_COMPARATOR = new Comparator<AbstractLog>() {
        
        @Override
        public int compare(AbstractLog o1, AbstractLog o2) {
            final int result = o2.getTimestamp().compareTo(o1.getTimestamp());
            return result == 0 ? o2.getExternalId().compareTo(o1.getExternalId()) : result;
        }
    };
    
    
    public  AbstractLog() {
        super();
        setOjbConcreteClass(getClass().getName());
        
    }
    
    public AbstractLog(User user, ContextPath contextPath) {
	this();
	init(user, contextPath);
    }

    public void init(User user, ContextPath contextPath) {
	setTimestamp(new DateTime());
	setUser(user);
	setTargetPath(contextPath.createDirNodePath());
	setTargetDirNode(contextPath.getLastDirNode());
    }
    
    public String getOperationString(String... args) {
	return FileManagementSystem.getMessage("label.log." + getClass().getSimpleName(), args);
    }
    
    public String getLogTime() {
	return getTimestamp().toString("dd/MM/yyyy HH:mm");
    }
    
    public String getTargetDirName() {
	return getTargetDirNode().getDisplayName();
    }
    
    public String getUserName() {
	return getUser().getPresentationName();
    }
    
    public ContextPath getContextPath() {
   	return getTargetPath().createContextPath();
    }
}
