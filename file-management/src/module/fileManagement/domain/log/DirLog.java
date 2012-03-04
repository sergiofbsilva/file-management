/*
 * @(#)DirLog.java
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
import myorg.domain.User;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public abstract class DirLog extends DirLog_Base {
    
    public  DirLog() {
        super();
    }
    
    public void init(User user, ContextPath contextPath, DirNode dirNode) {
        super.init(user, contextPath);
        setDirNode(dirNode);
    }
    
    public DirLog(User user, ContextPath contextPath, DirNode dirNode) {
	super();
	init(user, contextPath,dirNode);
    }
    
    @Override
    public String toString() {
	final String contextDirName = getTargetDirNode().getDisplayName();
	final String userName = getUser().getPresentationName();
	final String dirName = getDirNode().getDisplayName();
	return String.format("(%s) Em %s, %s %s %s", getLogTime(), contextDirName, userName, getOperationString(), dirName);
    }
    
    
}
