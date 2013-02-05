/*
 * @(#)AbstractLog.java
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

import java.util.Comparator;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileRepository;

import org.joda.time.DateTime;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public abstract class AbstractLog extends AbstractLog_Base {

    public static final Comparator<AbstractLog> TIMESTAMP_COMPARATOR = new Comparator<AbstractLog>() {

        @Override
        public int compare(AbstractLog o1, AbstractLog o2) {
            final int result = o2.getTimestamp().compareTo(o1.getTimestamp());
            return result == 0 ? o2.getExternalId().compareTo(o1.getExternalId()) : result;
        }
    };

    public AbstractLog() {
        super();

    }

    public AbstractLog(User user, ContextPath contextPath) {
        this();
        init(user, contextPath);
    }

    public void init(User user, ContextPath contextPath) {
        setTimestamp(new DateTime());
        setUser(user);
        setTargetPath(contextPath.createDirNodePath());
        setTargetDirNode(contextPath.getLastDirNode());
    }

    public String getOperationString(String... args) {
        return FileManagementSystem.getMessage("label.log." + getClass().getSimpleName(), args);
    }

    public String getLogTime() {
        return getTimestamp().toString("dd/MM/yyyy HH:mm");
    }

    public String getTargetDirName() {
        return getTargetDirNode().getDisplayName();
    }

    public String getUserName() {
        return getUser() != null ? getUser().getUsername() : FileManagementSystem.getMessage("label.script");
    }

    public ContextPath getContextPath() {
        return getTargetPath().createContextPath();
    }

    public DirNode getCurrentUserFileRepository() {
        return FileRepository.getOrCreateFileRepository(Authenticate.getCurrentUser());
    }

}
