/*
 * @(#)SharedFileNode.java
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import module.fileManagement.domain.log.FileLog;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;


/**
 * 
 * @author Sérgio Silva
 * 
 */
public class SharedFileNode extends SharedFileNode_Base implements SharedNode {
    
    public SharedFileNode(FileNode fileNode) {
	super();
	setNode(fileNode);
    }
    
    @Override
    public FileNode getNode() {
        return (FileNode) super.getNode();
    }
    
    @Override
    public boolean isShared() {
	return true;
    }
    
    
    @Override
    public void deleteService() {
	getNode().deleteService();
    }

    @Override
    public PersistentGroup getReadGroup() {
	return getNode().getReadGroup();
    }

    @Override
    public PersistentGroup getWriteGroup() {
	return getNode().getWriteGroup();
    }

    @Override
    public String getDisplayName() {
	return getNode().getDisplayName();
    }

    @Override
    public boolean isAccessible() {
	return getNode().isAccessible();
    }

    @Override
    public int compareTo(AbstractFileNode node) {
	return getNode().compareTo(node);
    }

    @Override
    public String getVisibility() {
	return getNode().getVisibility();
    }

    @Override
    public String getPresentationFilesize() {
	return getNode().getPresentationFilesize();
    }

    @Override
    public VisibilityList getVisibilityGroups() {
	return getNode().getVisibilityGroups();
    }
    
    @Override
    public long getFilesize() {
        return getNode().getFilesize();
    }

    @Override
    public void setVisibility(VisibilityList visibilityList) {
	getNode().setVisibility(visibilityList);
    }

    @Override
    public int getSharedFileNodesCount() {
	return getNode().getSharedFileNodesCount();
    }

    @Override
    public boolean hasAnySharedFileNodes() {
	return getNode() == null ? super.hasAnySharedFileNodes() : getNode().hasAnySharedFileNodes();
    }

    @Override
    public boolean hasSharedFileNodes(SharedFileNode sharedFileNodes) {
	return getNode().hasSharedFileNodes(sharedFileNodes);
    }

    @Override
    public Set<SharedFileNode> getSharedFileNodesSet() {
	return getNode().getSharedFileNodesSet();
    }

        @Override
    public void addSharedFileNodes(SharedFileNode sharedFileNodes) {
	getNode().addSharedFileNodes(sharedFileNodes);
    }

    @Override
    public void share(User user, VisibilityGroup group, ContextPath contextPath) {
	getNode().share(user, group, contextPath);
    }
    
    
    @Override
    public void removeSharedFileNodes(SharedFileNode sharedFileNodes) {
	getNode().removeSharedFileNodes(sharedFileNodes);
    }

    
    @Override
    public List<SharedFileNode> getSharedFileNodes() {
	return getNode().getSharedFileNodes();
    }

    @Override
    public Iterator<SharedFileNode> getSharedFileNodesIterator() {
	return getNode().getSharedFileNodesIterator();
    }

    @Override
    public Document getDocument() {
	return getNode().getDocument();
    }

    @Override
    public User getOwner() {
	return super.getOwner();
    }

    @Override
    public void setDocument(Document document) {
	getNode().setDocument(document);
    }

    @Override
    public boolean hasDocument() {
	return getNode() == null ? super.hasDocument() : getNode().hasDocument();
    }

    @Override
    public boolean isDeleted() {
	return getNode().isDeleted();
    }

    @Override
    public boolean isReadGroupMember() {
	return getNode().isReadGroupMember();
    }

    @Override
    public boolean isWriteGroupMember() {
	return getNode().isWriteGroupMember();
    }

    @Override
    public void removeDocument() {
	getNode().removeDocument();
    }

    @Override
    public String toString() {
	return getNode().toString();
    }

    @Override
    protected void setReadGroup(PersistentGroup persistentGroup) {
        getNode().setReadGroup(persistentGroup);
    }
    
    @Override
    protected void setWriteGroup(PersistentGroup persistentGroup) {
	getNode().setWriteGroup(persistentGroup);
    }
    
    //TODO: check if this makes sense, never moves target file to this user target dir
    @Override
    public void setParent(DirNode parent) {
        super.setParent(parent);
    }
    
    @Override
    public DirNode getParent() {
	return super.getParent();
    }
    
    @Override
    public boolean hasParent() {
	return super.hasParent();
    }
    
    @Override
    public void removeParent() {
       super.removeParent();
    }
    
    @Service
    public void deleteLink(ContextPath contextPath) {
	if (!isInTrash()) {
	    final DirNode trash = super.getParent().getTrash();
	    super.setParent(trash);    
	}
    }
    
    @Override
    public void trash(ContextPath contextPath) {
        deleteLink(contextPath);
    }
    
    @Override
    public Set<FileLog> getFileLogSet() {
	final Set<FileLog> fileLog = new HashSet<FileLog>(super.getFileLog());
	fileLog.addAll(getNode().getFileLog());
	return fileLog;
    }
}
