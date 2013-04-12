package pt.ist.file.rest.utils.domain;

import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.fenixframework.Atomic;

public class FileRestUtilsSystem extends FileRestUtilsSystem_Base {

    private FileRestUtilsSystem() {
        super();
    }

    public static FileRestUtilsSystem getInstance() {
        if (MyOrg.getInstance().getFileRestUtilsSystem() == null) {
            init();
        }
        return MyOrg.getInstance().getFileRestUtilsSystem();
    }

    @Atomic
    private static void init() {
        MyOrg.getInstance().setFileRestUtilsSystem(new FileRestUtilsSystem());
    }

    @Deprecated
    public java.util.Set<pt.ist.file.rest.utils.domain.RemoteDocument> getDocument() {
        return getDocumentSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.file.rest.utils.domain.RemoteFolder> getFolder() {
        return getFolderSet();
    }

}
