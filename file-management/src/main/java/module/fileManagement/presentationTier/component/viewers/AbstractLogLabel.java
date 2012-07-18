/*
 * @(#)AbstractLogLabel.java
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
package module.fileManagement.presentationTier.component.viewers;

import java.util.Collections;
import java.util.List;

import module.fileManagement.domain.ContextPath;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.log.AbstractLog;
import module.fileManagement.presentationTier.pages.DocumentBrowse;
import module.fileManagement.presentationTier.pages.LogPage;
import pt.ist.vaadinframework.fragment.FragmentQuery;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public abstract class AbstractLogLabel<T extends AbstractLog> extends Label implements AbstractLogViewer {
    private final T log;
    private LogPage parentContainer;

    public AbstractLogLabel(T log) {
	super();
	setSizeFull();
	setStyleName(BaseTheme.BUTTON_LINK);
	setContentMode(Label.CONTENT_XHTML);
	this.log = log;
    }

    public T getLog() {
	return log;
    }

    public String createTargetDirLink() {
	return createDirNodeLink(getLog().getContextPath());
    }

    public final String createDirNodeLink(final ContextPath contextPath) {
	final DirNode targetDir = contextPath.getLastDirNode();
	return targetDir.isAccessible() ? String.format("<a href=\"#%s\">%s</a>", new FragmentQuery(DocumentBrowse.class,
		contextPath.toString()).getQueryString(), targetDir.getDisplayName()) : targetDir.getDisplayName();
    }

    public abstract String createTargetNodeLink();

    @Override
    public void attach() {
	super.attach();
	setValue(createEntry());
    }

    private String createEntry() {
	final StringBuilder builder = new StringBuilder();
	builder.append(String.format("(%s) Em %s, ", log.getLogTime(), createTargetDirLink()));
	builder.append(String.format(" %s ", log.getUserName()));
	builder.append(String.format(" %s ", log.getOperationString()));
	builder.append(String.format(" %s ", createTargetNodeLink()));
	return builder.toString();
    }

    @Override
    public boolean hasOperations() {
	return false;
    }

    @Override
    public List<Component> getOperations() {
	return Collections.EMPTY_LIST;
    }

    public void setParentContainer(LogPage parent) {
	this.parentContainer = parent;
    }

    public LogPage getParentContainer() {
	return this.parentContainer;
    }
}
