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
	return user.hasFileRepository() ? user.getFileRepository() : createFileRepository(user);
    }

//    public static DirNode getOrCreateFileRepository(final Unit unit) {
//	return unit.hasFileRepository() ? unit.getFileRepository() : createFileRepository(unit);
//    }
//
//    @Service
//    private static DirNode createFileRepository(final Unit unit) {
//	return unit.hasFileRepository() ? unit.getFileRepository() : new DirNode(unit);
//    }

    @Service
    private static DirNode createFileRepository(final User user) {
	return user.hasFileRepository() ? user.getFileRepository() : new DirNode(user);
    }
    
//    public static DirNode getOrCreateTrash(final User user) {
//   	return user.hasTrash() ? user.getTrash() : createFileRepository(user);
//    }
    
}
