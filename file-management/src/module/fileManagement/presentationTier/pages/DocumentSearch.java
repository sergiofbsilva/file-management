/*
 * @(#)DocumentSearch.java
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.presentationTier.component.DocumentFileBrowser;
import module.fileManagement.presentationTier.component.NodeDetails;
import pt.ist.vaadinframework.VaadinFrameworkLogger;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@EmbeddedComponent(path = { "DocumentSearch" }, persistent = true)
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentSearch extends CustomComponent implements EmbeddedComponentContainer {

    private final Label lblSearchResult;

    private DocumentFileBrowser browser;
    private NodeDetails details;
    private GridSystemLayout gsl;

    private final TextField txtSearch;

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	return true;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	// TODO Auto-generated method stub

    }

    public DocumentSearch() {
	txtSearch = new TextField();
	lblSearchResult = new Label();
	gsl = new GridSystemLayout();
	browser = new DocumentFileBrowser();
	browser.getDocumentMenu().setVisible(false);
	browser.setVisible(false);
	final ValueChangeListener itemChanged = new ValueChangeListener() {
	    @Override
	    public void valueChange(ValueChangeEvent event) {
		Object absFileNode = event.getProperty().getValue();
		if (absFileNode != null) {
		    DomainItem<AbstractFileNode> nodeItem = browser.getContainer().getItem(absFileNode);
		    setFileDetails(nodeItem);
		}
	    }
	};
	browser.addListener(itemChanged);

	gsl.setCell("header", 16, new Label("<h3>Procurar</h3>", Label.CONTENT_XHTML));
	gsl.setCell("search", 0, 8, 8, createSearchPanel());
	gsl.setCell("result", 0, 10, 6, lblSearchResult);
	gsl.setCell("browser", 10, browser);
	setCompositionRoot(gsl);
    }

    private void setFileDetails(DomainItem<AbstractFileNode> nodeItem) {
	details = NodeDetails.makeDetails(nodeItem);
	gsl.setCell("details", 6, details);
    }

    private void setNoResults() {
	lblSearchResult.setValue(String.format("Não existem resultados a apresentar para a pesquisa por '%s'",
		txtSearch.getValue()));
	lblSearchResult.setVisible(true);
	browser.setVisible(false);
	details.setVisible(false);
    }

    private void updateSearchResultLabel(final int size) {
	lblSearchResult.setValue(String.format("A pesquisa por '%s' resultou em %d ocorrências", txtSearch.getValue(), size));
	lblSearchResult.setVisible(true);
    }

    private void updateContent(Set<AbstractFileNode> searchResult) {
	if (searchResult.isEmpty()) {
	    setNoResults();
	} else {
	    browser.setNodes(searchResult);
	    browser.setVisible(true);
	    updateSearchResultLabel(searchResult.size());
	    final AbstractFileNode selected = (AbstractFileNode) browser.getNodes().getIdByIndex(0);
	    browser.getDocumentTable().select(selected);
	}
    }

    private Set<AbstractFileNode> search(String searchText) {
	final Set<AbstractFileNode> resultSet = new HashSet<AbstractFileNode>();
	for (DirNode repository : DocumentHome.getAvailableRepositories()) {
	    resultSet.addAll(repository.doSearch(searchText));
	}
	return resultSet;
    }

    private void doSearch(String searchText) {
	if (searchText.isEmpty()) {
	    setNoResults();
	    lblSearchResult.setVisible(false);
	}
	long start = System.currentTimeMillis();
	final Set<AbstractFileNode> searchResult = search(searchText);
	long end = System.currentTimeMillis();
	VaadinFrameworkLogger.getLogger().debug(
		String.format("[%s ms] > query : %s size : %d\n", new Long(end - start), searchText, searchResult.size()));
	start = System.currentTimeMillis();
	updateContent(searchResult);
	end = System.currentTimeMillis();
	VaadinFrameworkLogger.getLogger().debug("render time : " + new Long(end - start) + "ms");
    }

    private Component createSearchPanel() {
	HorizontalLayout hl = new HorizontalLayout();
	hl.setSpacing(true);
	txtSearch.setImmediate(true);
	txtSearch.addShortcutListener(new ShortcutListener("search", KeyCode.ENTER, null) {

	    @Override
	    public void handleAction(Object sender, Object target) {
		doSearch((String) txtSearch.getValue());
	    }
	});

	Button btSearch = new Button("Pesquisar", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		doSearch((String) txtSearch.getValue());
	    }
	});
	hl.addComponent(txtSearch);
	hl.addComponent(btSearch);
	return hl;
    }

}
