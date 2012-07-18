package module.fileManagement.tools;

import module.fileManagement.domain.DirNode;

public class FilenameTemplate extends StringTemplate {
    private DirNode targetDirNode;

    public FilenameTemplate() {
	super();
    }

    public FilenameTemplate(DirNode dirNode) {
	this();
	setDirNode(dirNode);
    }

    public void setDirNode(DirNode dirNode) {
	targetDirNode = dirNode;
	setTemplate(dirNode.getFilenameTemplate());
	addResolver(new SeqNumberAttributeResolver(targetDirNode));
	addResolver(new CurrentYearAttributeResolver());
    }
}
