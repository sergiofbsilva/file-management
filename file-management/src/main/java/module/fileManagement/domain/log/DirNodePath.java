package module.fileManagement.domain.log;

import java.util.ArrayList;
import java.util.List;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import pt.ist.fenixWebFramework.services.Service;

public class DirNodePath extends DirNodePath_Base {

    public DirNodePath() {
        super();
    }

    public DirNodePath(DirNode dirNode) {
        super();
        setNode(dirNode);
    }

    @Service
    public static DirNodePath createPath(ContextPath contextPath) {
        DirNodePath prev = null;
        DirNodePath start = null;
        for (DirNode dirNode : contextPath.getDirNodes()) {
            if (prev == null) {
                prev = start = new DirNodePath(dirNode);
                continue;
            } else {
                DirNodePath curr = new DirNodePath(dirNode);
                prev.setNextPath(curr);
                prev = curr;
            }
        }
        return start;
    }

    public ContextPath createContextPath() {
        DirNodePath curr = this;
        final List<DirNode> dirNodes = new ArrayList<DirNode>();
        while (curr != null) {
            dirNodes.add((DirNode) curr.getNode());
            curr = curr.getNextPath();
        }
        return new ContextPath(dirNodes);
    }

    @Override
    public String toString() {
        final List<DirNode> paths = new ArrayList<DirNode>();
        DirNodePath path = getNextPath();
        while (path != null) {
            paths.add((DirNode) path.getNode());
            path = path.getNextPath();
        }
        return "/" + new ContextPath(paths);
    }

}
