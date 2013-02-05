package module.fileManagement.presentationTier.component;

import pt.ist.bennu.core.domain.groups.PersistentGroup;

public interface ChangeGroupProcedure {

    public void execute(final PersistentGroup group);

    public PersistentGroup getGroup();

}
