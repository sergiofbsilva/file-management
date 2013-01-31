package module.fileManagement.tools;

import module.fileManagement.domain.DirNode;

public abstract class DirAttributeResolver implements AttributeResolver {

	private DirNode dirNode;

	public DirAttributeResolver(DirNode dirNode) {
		setDirNode(dirNode);
	}

	public DirNode getDirNode() {
		return dirNode;
	}

	public void setDirNode(final DirNode dirNode) {
		this.dirNode = dirNode;
	}
}
