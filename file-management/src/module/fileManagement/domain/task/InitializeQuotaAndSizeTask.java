/*
 * @(#)InitializeQuotaAndSizeTask.java
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
package module.fileManagement.domain.task;

import myorg.domain.MyOrg;
import myorg.domain.contents.Node;
import myorg.domain.scheduler.WriteCustomTask;
import pt.ist.bennu.vaadin.domain.contents.VaadinNode;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class InitializeQuotaAndSizeTask extends WriteCustomTask {
    protected void doService() {
	for (Node node : MyOrg.getInstance().getNodes()) {
	    if (node instanceof VaadinNode) {
		final VaadinNode vaadinNode = (VaadinNode) node;
		final String argument = vaadinNode.getArgument();
		String replace = "";
		if (argument.startsWith("PageView")) {
		    final String[] split = argument.split("-");
		    replace = "PageView?page=" + split[1];
		} else {
		    if (argument.endsWith("-")) {
			replace = argument.substring(0, argument.length() - 1);
		    }
		}
		if (!replace.isEmpty()) {
		    // vaadinNode.setArgument(replace);
		    out.printf("arg %s replaced by %s\n", argument, replace);
		}
	    }
	}
    }
}
