package module.fileManagement.presentationTier.component;

import myorg.domain.groups.PersistentGroup;

public interface ChangeGroupProcedure {

    public void execute(final PersistentGroup group);

    public PersistentGroup getGroup();

}
