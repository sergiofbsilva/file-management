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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.Party;
import module.organization.domain.Person;
import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
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

    @Service
    private static DirNode createFileRepository(Party party) {
	return new DirNode(party);
    }

    public static DirNode getTrash(final User user) {
	final DirNode dirNode = getOrCreateFileRepository(user);
	return dirNode.getTrash();
    }

    private static List<Party> getParentUnits(final Party party, List<Party> processed) {
	final List<Party> result = new ArrayList<Party>();
	if (party.hasFileRepository() && party.getFileRepository().isAccessible()) {
	    result.add(party);
	}
	for (Accountability accountability : party.getParentAccountabilities()) {
	    final Party parent = accountability.getParent();
	    if (!processed.contains(parent)) {
		processed.add(parent);
		result.addAll(getParentUnits(parent, processed));
	    }
	}
	return result;
    }

    private static List<Party> getAllParentUnits(final Person person) {
	final List<Party> processed = new ArrayList<Party>();
	final List<Party> result = new ArrayList<Party>();
	result.addAll(getParentUnits(person, processed));
	return result;
    }

    public static Set<DirNode> getAvailableRepositories() {
	return getAvailableRepositories(Authenticate.getCurrentUser());
    }

    public static Set<DirNode> getAvailableRepositories(final User user) {
	final Set<DirNode> reps = new HashSet<DirNode>();
	final Person person = user.getPerson();
	reps.add(getOrCreateFileRepository(person));
	for (Party party : getAllParentUnits(person)) {
	    reps.add(party.getFileRepository());
	}
	return reps;
    }
}
