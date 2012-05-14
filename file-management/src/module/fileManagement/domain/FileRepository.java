/*
 * @(#)FileRepository.java
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

//import module.organization.domain.Unit;
import module.organization.domain.Party;
import myorg.domain.User;
import pt.ist.fenixWebFramework.services.Service;

/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class FileRepository {

    public static DirNode getOrCreateFileRepository(final User user) {
	return getOrCreateFileRepository(user.getPerson());
    }

    public static DirNode getOrCreateFileRepository(final Party party) {
	return party.hasFileRepository() ? party.getFileRepository() : createFileRepository(party);
    }

    public static DirNode getTrash(final User user) {
	final DirNode dirNode = getOrCreateFileRepository(user);
	return dirNode.getTrash();
    }

    @Service
    private static DirNode createFileRepository(final User user) {
	return createFileRepository(user.getPerson());
    }

    @Service
    private static DirNode createFileRepository(final Party party) {
	return party.hasFileRepository() ? party.getFileRepository() : new DirNode(party);
    }

}
