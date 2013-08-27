/*
 * @(#)SharedDirNode.java
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

import jvstm.cps.ConsistencyPredicate;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.domain.log.DirLog;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.legacy.PersistentGroup;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class SharedDirNode extends SharedDirNode_Base implements SharedNode {

    public SharedDirNode() {
        super();
    }

    public SharedDirNode(DirNode dirNode) {
        this();
        setNode(dirNode);
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public Long getQuota() {
        return getNode().getQuota();
    }

    @Override
    public Long getSize() {
        return getNode().getSize();
    }

    @Override
    public FileNode createFile(File file, String fileName, long filesize, ContextPath currentPath) {
        return getNode().createFile(file, fileName, filesize, currentPath);
    }

    @Override
    public boolean isSharedFilesDirectory() {
        return getNode().isSharedFilesDirectory();
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
    public int getCountFiles() {
        return getNode().getCountFiles();
    }

    @Override
    public String getDisplayName() {
        final String name = super.getDisplayName();
        return name != null ? name : getNode().getDisplayName();
    }

    @Override
    public PersistentGroup getReadGroup() {
        return getNode() != null ? getNode().getReadGroup() : super.getReadGroup();
    }

    @Override
    public PersistentGroup getWriteGroup() {
        return getNode() != null ? getNode().getWriteGroup() : super.getWriteGroup();
    }

    @Override
    public String getName() {
        final String name = super.getName();
        return name != null ? name : getNode().getName();
    }

    @Override
    public boolean hasAvailableQuota(long length) {
        return getNode().hasAvailableQuota(length);
    }

    @Override
    public boolean hasAvailableQuota(long length, FileNode fileNode) {
        return getNode().hasAvailableQuota(length, fileNode);
    }

    @Override
    public String getPresentationFilesize() {
        return getNode().getPresentationFilesize();
    }

    @Override
    public String getPresentationTotalUsedSpace() {
        return getNode().getPresentationTotalUsedSpace();
    }

    @Override
    public String getPercentOfTotalUsedSpace() {
        return getNode().getPercentOfTotalUsedSpace();
    }

    @Override
    public long getFilesize() {
        return getNode().getFilesize();
    }

    @Override
    public User getOwner() {
        return super.getOwner();
    }

    @Override
    public Set<AbstractFileNode> getChildSet() {
        return getNode().getChildSet();
    }

    @Atomic
    public void deleteLink() {
        super.setParent(null);
        super.setNode(null);
        deleteDomainObject();
    }

    @Atomic
    public void deleteLink(ContextPath currentPath) {
        setParent(getTrash());
    }

    @Override
    public void trash(ContextPath currentPath) {
        deleteLink(currentPath);
    }

    @Override
    public Set<DirLog> getDirLogSet() {
        final Set<DirLog> dirLog = new HashSet<DirLog>(super.getDirLogSet());
        dirLog.addAll(getNode().getDirLogSet());
        return dirLog;
    }

    @Override
    public Set<AbstractLog> getTargetLogSet() {
        final Set<AbstractLog> fileLog = new HashSet<AbstractLog>(super.getTargetLogSet());
        fileLog.addAll(getNode().getTargetLogSet());
        return fileLog;
    }

    @Override
    @ConsistencyPredicate
    public boolean checkParent() {
        return true;
    }

}
