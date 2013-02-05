/*
 * @(#)ShareFileLog.java
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
package module.fileManagement.domain.log;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import pt.ist.bennu.core.domain.User;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class ShareFileLog extends ShareFileLog_Base {

    public ShareFileLog() {
        super();
    }

    public ShareFileLog(User user, ContextPath contextPath, FileNode fileNode, DirNode targetSharedFolder) {
        super();
        super.init(user, contextPath, fileNode);
        setTargetDirNode(targetSharedFolder);
    }

    @Override
    public String getOperationString(String... args) {
        final DirNode sharedFolder = getCurrentUserFileRepository().getSharedFolder();
        final String sharedWith =
                sharedFolder == getTargetDirNode() ? "consigo" : " com " + getTargetDirNode().getOwner().getPresentationName();
        return super.getOperationString(sharedWith);
    }

    @Override
    public String toString() {
        return String.format("(%s) %s %s %s", getLogTime(), getUserName(), getOperationString(), getFileNode().getDisplayName());
    }
}
