package module.fileManagement.presentationTier.component;

import pt.ist.bennu.core.domain.groups.legacy.PersistentGroup;

public interface ChangeGroupProcedure {

    public void execute(final PersistentGroup group);

    public PersistentGroup getGroup();

}
