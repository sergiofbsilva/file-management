/*
 * @(#)VisibilityGroup.java
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

import java.io.Serializable;

import pt.ist.bennu.core.domain.groups.PersistentGroup;

/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class VisibilityGroup implements Serializable {

    public static enum VisibilityOperation {
        READ("label.visibilityGroup.read"), WRITE("label.visibilityGroup.write");

        private final String key;

        private VisibilityOperation(final String key) {
            this.key = key;
        }

        public String getDescription() {
            return FileManagementSystem.getMessage(key);
        }

        public boolean isMember(final VisibilityOperation[] visibilityOperations) {
            for (final VisibilityOperation visibilityOperation : visibilityOperations) {
                if (visibilityOperation == this) {
                    return true;
                }
            }
            return false;
        }
    }

    public final PersistentGroup persistentGroup;
    public VisibilityOperation visibilityOperation;

    public VisibilityGroup(final PersistentGroup persistentGroup, final VisibilityOperation visibilityOperation) {
        this.persistentGroup = persistentGroup;
        this.visibilityOperation = visibilityOperation;
    }

    public String getDescription() {
        final StringBuilder groupString = new StringBuilder();
        groupString.append(persistentGroup.getName());
        groupString.append(" - ");
        groupString.append(visibilityOperation.getDescription());
        return groupString.toString();
    }

    public void setVisibilityOperation(final VisibilityOperation visibilityOperation) {
        this.visibilityOperation = visibilityOperation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VisibilityGroup) {
            final VisibilityGroup group = (VisibilityGroup) obj;
            return group.persistentGroup == persistentGroup;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return persistentGroup.getPresentationName().hashCode();
    }

}
