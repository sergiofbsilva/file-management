package module.fileManagement.domain.task;

import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.contents.Node;
import pt.ist.bennu.core.domain.groups.Role;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.bennu.vaadin.domain.contents.VaadinNode;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

public class AddWebdavNode extends WriteCustomTask {
    final String FILEMANAGEMENT_RESOURCES = "resources.FileManagementResources";

    @Override
    protected void doService() {
        addNode(FenixFramework.getDomainObject("395136991834"), FenixFramework.getDomainObject("6597069782265"));
        addNode(FenixFramework.getDomainObject("395136992236"), FenixFramework.getDomainObject("6597069783265"));
    }

    private void addNode(DomainObject virtualHost, DomainObject parentNode) {
        VaadinNode.createVaadinNode((VirtualHost) virtualHost, (Node) parentNode, FILEMANAGEMENT_RESOURCES, "label.link.webdav",
                "WebdavHome", Role.getRole(RoleType.MANAGER));
    }
}