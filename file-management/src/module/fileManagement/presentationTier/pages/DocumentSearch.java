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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pt.ist.vaadinframework.VaadinFrameworkLogger;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.presentationTier.component.DocumentFileBrowser;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.fileManagement.presentationTier.data.FMSFieldFactory;
import module.vaadin.ui.BennuTheme;

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
    private Component advSearch;
    private Component simpleSearch;

    private final TextField txtSearch;

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	return Boolean.TRUE;
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
	advSearch = createAdvancedSearchPanel();
	simpleSearch = createSearchPanel();
	gsl.setCell("header", 16, new Label("<h3>Procurar</h3>", Label.CONTENT_XHTML));
	gsl.setCell("search", 16, simpleSearch);
	gsl.setCell("result", 0, 10, 6, lblSearchResult);
	gsl.setCell("browser", 10, browser);
	gsl.setCell("details", 6);
	setCompositionRoot(gsl);
    }

    private void setFileDetails(DomainItem<AbstractFileNode> nodeItem) {
	details = NodeDetails.makeDetails(nodeItem);
	gsl.setCell("details", 6, details);
    }

    private void setNoResults() {
	final Object value = txtSearch.getValue();
	final String simpleSearch = "Não existem resultados a apresentar para a pesquisa por '%s's";
	final String advSearch = "Não existem resultados a apresentar";

	lblSearchResult.setValue(value != null ? String.format(simpleSearch, value) : advSearch);
	lblSearchResult.setVisible(Boolean.TRUE);
	browser.setVisible(Boolean.FALSE);
	if (details != null) {
	    details.setVisible(Boolean.FALSE);
	}
    }

    private void updateSearchResultLabel(final int size) {
	final Object value = txtSearch.getValue();
	final String simpleSearch = "A pesquisa por '%s' resultou em %d ocorrências";
	final String advSearch = "A pesquisa resultou em %d ocorrências";

	lblSearchResult.setValue(String.format(value == null ? advSearch : simpleSearch, value, size));
	lblSearchResult.setVisible(Boolean.TRUE);
    }

    private void updateContent(Set<AbstractFileNode> searchResult) {
	if (searchResult.isEmpty()) {
	    setNoResults();
	} else {
	    browser.setNodes(searchResult);
	    browser.setVisible(Boolean.TRUE);
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

    private Set<AbstractFileNode> advSearch(final Multimap<MetadataKey, Object> searchMap) {
	final Set<AbstractFileNode> resultFiles = Sets.newHashSet();
	Map<AbstractFileNode, Map<MetadataKey, Boolean>> resultMap = Maps.newHashMap();
	for (DirNode repository : DocumentHome.getAvailableRepositories()) {
	    resultMap.putAll(repository.doSearch(searchMap));
	}
	for (Entry<AbstractFileNode, Map<MetadataKey, Boolean>> entry : resultMap.entrySet()) {
	    for (Entry<MetadataKey, Boolean> entryKey : entry.getValue().entrySet()) {
		if (entryKey.getValue()) {
		    resultFiles.add(entry.getKey());
		}
	    }
	}
	return resultFiles;
    }

    private void doAdvSearch(final Multimap<MetadataKey, Object> searchMap) {
	if (searchMap.isEmpty()) {
	    setNoResults();
	}
	long start = System.currentTimeMillis();
	final Set<AbstractFileNode> searchResult = advSearch(searchMap);
	long end = System.currentTimeMillis();
	VaadinFrameworkLogger.getLogger().debug(
		String.format("[%s ms] > query : %s size : %d\n", new Long(end - start), searchMap, searchResult.size()));
	start = System.currentTimeMillis();
	updateContent(searchResult);
	end = System.currentTimeMillis();
	VaadinFrameworkLogger.getLogger().debug("render time : " + new Long(end - start) + "ms");
    }

    private void doSearch(String searchText) {
	if (searchText.isEmpty()) {
	    setNoResults();
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

    private static enum Operator {
	AND("&"), OR("|");

	private String operator;

	private Operator(String operator) {
	    this.operator = operator;
	}

	@Override
	public String toString() {
	    return this == AND ? "e" : "ou";
	}

	public String getOperator() {
	    return operator;
	}
    }

    private Component createAdvancedSearchPanel() {
	final VerticalLayout vlMain = new VerticalLayout();
	final VerticalLayout vlEntries = new VerticalLayout();

	final AdvancedSearchEntry.RemoveEntryListener removeListener = new AdvancedSearchEntry.RemoveEntryListener() {

	    @Override
	    public void removeEntry(AdvancedSearchEntry.RemoveEntryEvent event) {
		vlEntries.removeComponent(event.getEntry());
	    }
	};
	final Button btAddEntry = new Button("+");

	final Select cbTemplate = new Select();
	cbTemplate.setWidth(30, UNITS_EM);
	cbTemplate.setImmediate(Boolean.TRUE);
	cbTemplate.setNullSelectionAllowed(Boolean.FALSE);
	cbTemplate.setContainerDataSource((Container) new DomainItem(FileManagementSystem.getInstance())
		.getItemProperty("metadataTemplates"));
	cbTemplate.setItemCaptionPropertyId("name");
	final ValueChangeListener templateListener = new ValueChangeListener() {

	    @Override
	    public void valueChange(ValueChangeEvent event) {
		vlEntries.removeAllComponents();
		for (Object listener : btAddEntry.getListeners(ClickEvent.class)) {
		    ((ClickListener) listener).buttonClick(null);
		}
	    }
	};
	cbTemplate.addListener(templateListener);

	HorizontalLayout hl = new HorizontalLayout();
	hl.setSpacing(Boolean.TRUE);
	final Label lbl = new Label("Tipologia");
	lbl.setWidth(180, UNITS_PIXELS);
	hl.addComponent(lbl);
	hl.addComponent(cbTemplate);
	final Label lblSimpleHelp = new Label("Por favor seleccione uma tipologia");
	lblSimpleHelp.addStyleName(BennuTheme.LABEL_SMALL);
	hl.addComponent(lblSimpleHelp);

	vlMain.addComponent(hl);
	vlMain.addComponent(vlEntries);
	final ClickListener addEntryListener = new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		MetadataTemplate template = (MetadataTemplate) cbTemplate.getValue();
		DomainContainer<MetadataKey> keys = new DomainContainer<MetadataKey>(template.getKey(), MetadataKey.class);
		final AdvancedSearchEntry advSearch = new AdvancedSearchEntry(keys);
		advSearch.addListener(removeListener);
		vlEntries.addComponent(advSearch);
	    }
	};

	btAddEntry.addListener(addEntryListener);
	vlMain.addComponent(btAddEntry);

	Button btSimpleSearch = new Button("Pesquisa Simples", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		gsl.setCell("search", 0, 8, 8, simpleSearch);
	    }
	});

	btSimpleSearch.setStyleName(BennuTheme.BUTTON_LINK);
	final Button btSearch = new Button("Procurar", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		Multimap<MetadataKey, Object> searchMap = HashMultimap.create();
		Iterator<Component> iterator = vlEntries.getComponentIterator();
		final Set<AdvancedSearchEntry> entriesToRemove = Sets.newHashSet();
		while (iterator.hasNext()) {
		    AdvancedSearchEntry entry = (AdvancedSearchEntry) iterator.next();
		    if (entry.getFieldValue() != null) {
			searchMap.put(entry.getMetadataKey(), entry.getFieldValue());
		    } else {
			entriesToRemove.add(entry);
		    }
		}
		for (AdvancedSearchEntry entry : entriesToRemove) {
		    vlEntries.removeComponent(entry);
		}
		doAdvSearch(searchMap);
	    }
	});

	hl = new HorizontalLayout();
	hl.setSpacing(Boolean.TRUE);
	hl.addComponent(btSearch);
	hl.addComponent(btSimpleSearch);

	vlMain.addComponent(hl);
	vlMain.setSpacing(Boolean.TRUE);
	vlEntries.setSpacing(Boolean.TRUE);
	return vlMain;
    }

    private static class AdvancedSearchEntry extends CustomComponent {

	private static Method REMOVE_ENTRY_METHOD;
	private final Select cbMetadataKey;
	private final HorizontalLayout hl;
	private Field currentField;
	private ObjectProperty property;

	public class RemoveEntryEvent extends Event {

	    public RemoveEntryEvent(Component source) {
		super(source);
	    }

	    public Component getEntry() {
		return (Component) getSource();
	    }

	}

	public interface RemoveEntryListener extends Serializable {
	    public void removeEntry(RemoveEntryEvent event);
	}

	static {
	    try {
		REMOVE_ENTRY_METHOD = RemoveEntryListener.class.getDeclaredMethod("removeEntry",
			new Class[] { RemoveEntryEvent.class });
	    } catch (SecurityException e) {
		e.printStackTrace();
	    } catch (NoSuchMethodException e) {
		e.printStackTrace();
	    }
	}

	private void setCurrentField(final Field field) {
	    currentField = field;
	}

	private Field getCurrentField() {
	    return currentField;
	}

	public AdvancedSearchEntry(DomainContainer<MetadataKey> keys) {

	    hl = new HorizontalLayout();
	    hl.setSpacing(Boolean.TRUE);

	    final Field placeHolder = new TextField();
	    placeHolder.setVisible(Boolean.FALSE);
	    setCurrentField(placeHolder);

	    cbMetadataKey = new Select();
	    cbMetadataKey.setNullSelectionAllowed(Boolean.FALSE);
	    cbMetadataKey.setImmediate(Boolean.TRUE);
	    cbMetadataKey.setContainerDataSource(keys);
	    cbMetadataKey.setItemCaptionPropertyId("keyValue");
	    cbMetadataKey.addListener(new ValueChangeListener() {

		@Override
		public void valueChange(ValueChangeEvent event) {
		    MetadataKey key = (MetadataKey) event.getProperty().getValue();
		    Field newField = FMSFieldFactory.makeField(key);
		    if (newField instanceof AbstractTextField) {
			((AbstractTextField) newField).setNullRepresentation(StringUtils.EMPTY);
		    }
		    newField.setWidth(30, UNITS_EM);
		    property = new ObjectProperty(null, key.getKeyPrimitiveType());
		    newField.setPropertyDataSource(property);
		    hl.replaceComponent(getCurrentField(), newField);
		    setCurrentField(newField);
		}
	    });

	    Button btRemove = new Button();
	    btRemove.setIcon(new ThemeResource("../runo/icons/16/cancel.png"));
	    btRemove.addListener(new ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
		    fireEvent(new RemoveEntryEvent(AdvancedSearchEntry.this));
		}
	    });

	    Select cbOperator = new Select(null, Arrays.asList(Operator.values()));
	    cbOperator.setImmediate(Boolean.TRUE);
	    cbOperator.setNullSelectionAllowed(Boolean.FALSE);
	    cbOperator.select(Operator.OR);
	    cbOperator.setWidth(5, UNITS_EM);
	    // hl.addComponent(cbOperator);
	    hl.addComponent(cbMetadataKey);
	    hl.addComponent(placeHolder);
	    hl.addComponent(btRemove);
	    if (keys.size() > 0) {
		cbMetadataKey.select(keys.getIdByIndex(0));
	    }
	    setCompositionRoot(hl);
	}

	public void addListener(RemoveEntryListener listener) {
	    addListener(RemoveEntryEvent.class, listener, REMOVE_ENTRY_METHOD);
	}

	public MetadataKey getMetadataKey() {
	    return (MetadataKey) cbMetadataKey.getValue();
	}

	public Object getFieldValue() {
	    return property != null ? property.getValue() : null;
	}

    }

    private Component createSearchPanel() {
	HorizontalLayout hl = new HorizontalLayout();
	hl.setSpacing(true);
	txtSearch.setWidth(24, UNITS_EM);

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

	Button btAdvSearch = new Button("Pesquisa Avançada", new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		gsl.setCell("search", 0, 8, 8, advSearch);
	    }
	});
	btAdvSearch.setStyleName(BennuTheme.BUTTON_LINK);
	hl.addComponent(txtSearch);
	hl.addComponent(btSearch);
	hl.addComponent(btAdvSearch);
	return hl;
    }

}
