package module.fileManagement.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.fileManagement.domain.log.DirNodePath;

import org.apache.commons.lang.StringUtils;

public class ContextPath {
    
    private final List<DirNode> contextPath;
    private final String contextPathString;
    
    public ContextPath(DirNode dirNode) {
	this(Collections.singletonList(dirNode));
    }
    
    public ContextPath(List<DirNode> dirNodes) {
	this.contextPath = dirNodes;
	this.contextPathString = getContextPath(this.contextPath);
    }
    
    public ContextPath(String contextPathString) {
	this.contextPath = getContextPath(contextPathString);
	this.contextPathString = contextPathString;
    }
    
    public ContextPath concat(DirNode dirNode) {
	List<DirNode> dirNodes = new ArrayList<DirNode>(contextPath);
	dirNodes.add(dirNode);
	return new ContextPath(dirNodes);
    }
    
    private static List<DirNode> getContextPath(String pathString) {
	final List<DirNode> dirNodes = new ArrayList<DirNode>();
	final String[] split = StringUtils.split(pathString,'/');
	for (String node : split) {
	    final DirNode dirNode = DirNode.fromExternalId(node);
	    dirNodes.add(dirNode);
	}
	return Collections.unmodifiableList(dirNodes);
    }
    
    private static String getContextPath(List<DirNode> dirNodes) {
	final List<String> oids = new ArrayList<String>();
	for (DirNode dirNode : dirNodes) {
	    oids.add(dirNode.getExternalId());
	}
	return StringUtils.join(oids,'/');
    }
    
    public List<DirNode> getDirNodes() {
	return contextPath;
    }
    
    public String getContextPathString() {
	return contextPathString;
    }
    
    public DirNodePath createDirNodePath() {
	return DirNodePath.createPath(this);
    }
    
    public DirNode getLastDirNode() {
	return getDirNodes().get(getDirNodes().size() - 1);
    }
    
    @Override
    public String toString() {
	return getContextPathString();
    }
    
    public boolean contains(DirNode dirNode) {
	return contextPath.contains(dirNode);
    }
    
}
