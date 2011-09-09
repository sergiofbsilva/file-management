package module.fileManagement.presentationTier.component.groups;

import java.util.HashSet;
import java.util.Set;

public class GroupCreatorRegistry {
    
    private static final Set<HasPersistentGroupCreator> persistentGroupCreators = new HashSet<HasPersistentGroupCreator>();
    
    public static synchronized void registerPersistentGroupCreator(final HasPersistentGroupCreator hasPersistentGroupCreator) {
	persistentGroupCreators.add(hasPersistentGroupCreator);
    }
    
    public static Set<HasPersistentGroupCreator> getPersistentGroupCreators() {
	return persistentGroupCreators;
    }
    
    static {
	registerPersistentGroupCreator(new PersistentGroupCreator());
    }
}
