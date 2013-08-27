/*
 * @(#)ConfirmDeleteFile.java
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
package module.fileManagement.presentationTier.component;

import module.fileManagement.domain.FileNode;
import pt.ist.bennu.core.i18n.BundleUtil;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class ConfirmDeleteFile extends Window {

    protected static String getBundle() {
        return "resources.FileManagementResources";
    }

    protected static String getMessage(final String key, final String... args) {
        return BundleUtil.getString(getBundle(), key, args);
    }

    public ConfirmDeleteFile(final FileNode fileNode, final Table fileTable) {
        super(getMessage("label.file.delete"));
        setWidth(50, UNITS_PERCENTAGE);

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        final Window window = this;

        final Label label = new Label(getMessage("label.file.delete.confirm"));
        layout.addComponent(label);

        final HorizontalLayout horizontalLayout = new HorizontalLayout();
        layout.addComponent(horizontalLayout);
        layout.setComponentAlignment(horizontalLayout, Alignment.BOTTOM_RIGHT);

        final Button delete = new Button(getMessage("label.file.delete"), new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                fileTable.removeItem(fileNode.getExternalId());
                fileNode.deleteService();
                (getParent()).removeWindow(window);
            }
        });
        horizontalLayout.addComponent(delete);
        final Button close = new Button(getMessage("label.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                (getParent()).removeWindow(window);
            }
        });
        horizontalLayout.addComponent(close);
    }

}
