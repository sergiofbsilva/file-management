/*
 * @(#)FileNode.java
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import module.fileManagement.domain.log.FileLog;
import module.fileManagement.domain.log.RecoverFileLog;
import module.fileManagement.domain.log.UnshareFileLog;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class FileNode extends FileNode_Base {

    public FileNode(final DirNode dirNode, final File file, final String fileName) {
	super();
	if (dirNode == null) {
	    throw new DomainException("must.specify.a.dir.node.when.creating.a.file.node");
	}
	setDocument(new Document(file, fileName));
	setParent(dirNode);
    }

    public FileNode() {
	super();
    }

    @Override
    public boolean isFile() {
	return true;
    }

    @Override
    @Service
    public void delete() {
	final Document document = getDocument();
	document.delete();
	getParent().removeChild(this);
	deleteDomainObject();
    }

    void deleteFromDocument() {
	removeDocument();
	super.delete();
    }

    @Service
    public void deleteService() {
	delete();
    }

    @Override
    public PersistentGroup getReadGroup() {
	final PersistentGroup group = getDocument().getReadGroup();
	return group == null && hasParent() ? getParent().getReadGroup() : group;
    }

    @Override
    public PersistentGroup getWriteGroup() {
	final PersistentGroup group = getDocument().getWriteGroup();
	return group == null && hasParent() ? getParent().getWriteGroup() : group;
    }

    @Override
    public String getDisplayName() {
	final Document document = getDocument();
	return document.getDisplayName();
    }

    @Override
    public int compareTo(final AbstractFileNode node) {
	return node.isDir() ? 1 : super.compareTo(node);
    }

    @Override
    protected void setReadGroup(final PersistentGroup persistentGroup) {
	final Document document = getDocument();
	document.setReadGroup(persistentGroup);
    }

    @Override
    protected void setWriteGroup(PersistentGroup persistentGroup) {
	final Document document = getDocument();
	document.setWriteGroup(persistentGroup);
    }

    @Override
    public String getPresentationFilesize() {
	final Document document = getDocument();
	return document.getPresentationFilesize();
    }

    @Override
    public long getFilesize() {
	return Long.valueOf(getDocument().getFilesize());
    }

    @Override
    public Set<FileLog> getFileLogSet() {
	if (hasAnySharedFileNodes()) {
	    final HashSet<FileLog> logs = new HashSet<FileLog>(super.getFileLogSet());
	    for (SharedFileNode sharedNode : getSharedFileNodes()) {
		logs.addAll(sharedNode.getFileLogSet());
	    }
	    return logs;
	}
	return super.getFileLogSet();
    }

    @Override
    @Service
    public void unshare(VisibilityGroup group) {
	super.unshare(group);
	for (SharedFileNode sharedNode : getSharedFileNodes()) {
	    new UnshareFileLog(UserView.getCurrentUser(), sharedNode);
	    sharedNode.deleteLink(new ContextPath(getParent()));
	}
    }

    @Service
    public void recoverTo(DirNode targetDir) {
	new RecoverFileLog(UserView.getCurrentUser(), targetDir.getContextPath(), this);
	setParent(targetDir);
    }
}
