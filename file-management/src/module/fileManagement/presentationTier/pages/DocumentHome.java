/*
 * @(#)DocumentHome.java
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
package module.fileManagement.presentationTier.pages;

import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.Map;

import module.fileManagement.domain.FileNode;
import module.vaadin.ui.BennuTheme;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "DocumentHome" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentHome extends CustomComponent implements EmbeddedComponentContainer {

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	return true;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	// TODO Auto-generated method stub

    }

    public Panel createWelcomePanel() {
	Panel welcomePanel = new Panel();
	welcomePanel.setStyleName(BennuTheme.PANEL_LIGHT);
	welcomePanel.setCaption(getMessage("welcome.title"));
	welcomePanel.setScrollable(false);
	final Layout hlWelcomeContent = new VerticalLayout();
	Label lblWelcomeText = new Label(getMessage("welcome.content"), Label.CONTENT_XHTML);
	hlWelcomeContent.addComponent(lblWelcomeText);
	welcomePanel.setContent(hlWelcomeContent);
	return welcomePanel;
    }

    public GridLayout createGrid(Component recentlyShared, Component fileDetails) {
	GridLayout grid = new GridLayout(2, 1);
	grid.setSizeFull();
	grid.setMargin(true, true, true, false);
	grid.setSpacing(true);
	grid.addComponent(recentlyShared, 0, 0);
	grid.addComponent(fileDetails, 1, 0);
	return grid;
    }

    public Panel createRecentlyShared() {
	Panel recentlyPanel = new Panel(getMessage("label.recently.shared"));
	Table tbShared = new Table();
	tbShared.setSizeFull();
	recentlyPanel.addComponent(tbShared);
	return recentlyPanel;
    }

    public static Panel createFileDetails(FileNode fileNode) {
	Panel pDetails = new Panel(getMessage("label.file.details"));
	DomainItem item = new DomainItem(fileNode);
	return pDetails;
    }

    public static Panel createDirDetails() {
	Panel pDetails = new Panel(getMessage("label.file.details"));
	pDetails.addComponent(new Label("Name: document.doc", Label.CONTENT_TEXT));
	pDetails.addComponent(new Label("Size: 120kb", Label.CONTENT_TEXT));
	return pDetails;
    }

    public Component createPage() {
	// VerticalLayout mainLayout = new VerticalLayout();
	// mainLayout.setMargin(true);
	// mainLayout.setSizeFull();
	// mainLayout.setSpacing(true);
	// mainLayout.addComponent(createWelcomePanel());
	// //
	// mainLayout.addComponent(createGrid(createRecentlyShared(),createFileDetails()));
	// return mainLayout;
	GridSystemLayout gsl = new GridSystemLayout();
	gsl.setCell("welcome", 16, createWelcomePanel());
	// gsl.setCell("recentlyShared", 8, createRecentlyShared());
	// gsl.setCell("dirDetails", 8, createDirDetails());

	return gsl;
    }

    @Override
    public void attach() {
	setCompositionRoot(createPage());
    }

}
