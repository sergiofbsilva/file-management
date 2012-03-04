/*
 * @(#)ContextPath.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.fileManagement.domain.log.DirNodePath;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Sérgio Silva
 * 
 */
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
