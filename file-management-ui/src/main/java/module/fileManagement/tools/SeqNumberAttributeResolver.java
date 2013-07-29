package module.fileManagement.tools;

import module.fileManagement.domain.DirNode;

public class SeqNumberAttributeResolver extends DirAttributeResolver {

    public SeqNumberAttributeResolver(DirNode dirNode) {
        super(dirNode);
    }

    @Override
    public String get() {
        return "seq";
    }

    @Override
    public String resolve() {
        return getDirNode().getSequenceNumber().toString();
    }
}
