/*
 * @(#)DocumentExtendedInfo.java
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.Document;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.Metadata;
import module.fileManagement.presentationTier.DownloadUtil;
import module.fileManagement.presentationTier.component.MetadataPanel;
import module.fileManagement.presentationTier.component.NodeDetails;
import module.fileManagement.presentationTier.component.TabularViewer;
import module.fileManagement.presentationTier.component.viewers.FMSViewerFactory;
import module.fileManagement.presentationTier.data.DocumentContainer;
import module.vaadin.data.util.ObjectHintedProperty;
import module.vaadin.ui.BennuTheme;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@EmbeddedComponent(path = { "DocumentExtendedInfo" }, args = { "node" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentExtendedInfo extends CustomComponent implements EmbeddedComponentContainer {
    private final GridLayout mainGrid;
    private FileNode fileNode;
    private Component metadataInfoView;
    private Component editMetadataView;
    private Layout metadataInfoPanel;

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	return true;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	fileNode = Document.fromExternalId(arguments.get("node"));
    }

    public void update() {
	mainGrid.removeAllComponents();
	mainGrid.addComponent(createExtendedInfo(), 0, 0);
	mainGrid.addComponent(createDocumentOperations(), 1, 0);
    }

    public DocumentExtendedInfo() {
	final VerticalLayout vl = new VerticalLayout();
	vl.setSizeFull();
	vl.setSpacing(true);
	vl.setMargin(false, false, false, true);
	mainGrid = new GridLayout(2, 1);
	mainGrid.setSizeFull();
	mainGrid.setSpacing(true);
	vl.addComponent(new Label("<h3>Documento</h3>", Label.CONTENT_XHTML));
	vl.addComponent(mainGrid);
	setCompositionRoot(vl);
    }

    @Override
    public void attach() {
	super.attach();
	update();
    }

    private Component createDocumentOperations() {
	return NodeDetails.makeDetails(new DomainItem<AbstractFileNode>(fileNode), true, false);
    }

    private Component createExtendedInfo() {
	final DomainItem<FileNode> item = new DomainItem<FileNode>(fileNode);
	final VerticalLayout vlayout = new VerticalLayout();
	vlayout.setSpacing(true);
	vlayout.addComponent(createInfoPanel(item));
	metadataInfoPanel = createMetadataInfoPanel(item);
	vlayout.addComponent(metadataInfoPanel);
	return vlayout;
    }

    private List<Component> getVersionsEntries(DomainItem<FileNode> item) {
	final List<Component> versions = new ArrayList<Component>();
	final Property recentMetadata = item.getItemProperty("document.versions");
	final Collection<Metadata> itemIds = (Collection<Metadata>) recentMetadata.getValue();
	int index = itemIds.size();
	for (Metadata metadata : itemIds) {
	    final Label lblValue = new Label();
	    lblValue.setCaption(String.format("Versão (%d) info", index--));
	    lblValue.setValue(String.format("%s em %s", metadata.getValue(),
		    metadata.getTimestamp().toString("dd/MM/yyyy HH:mm:ss")));
	    versions.add(lblValue);
	}
	return versions;
    }

    private Component editMetadataPanel() {
	final Document document = fileNode.getDocument();
	final ObjectHintedProperty<Collection> selectedDocuments = new ObjectHintedProperty<Collection>(new HashSet<Document>(),
		Collection.class);
	final DocumentContainer documentContainer = new DocumentContainer();
	documentContainer.addItem(document);
	final Collection docs = selectedDocuments.getValue();
	docs.add(document);
	selectedDocuments.setValue(docs);

	final Button btSubmit = new Button("Guardar");
	btSubmit.addListener(new ClickListener() {

	    @Override
	    public void buttonClick(ClickEvent event) {
		documentContainer.commit();
		// metadataInfoPanel.replaceComponent(editMetadataView,
		// metadataInfoView);
		update();
	    }
	});

	VerticalLayout vlEditMetadata = new VerticalLayout();
	vlEditMetadata.setSizeFull();
	vlEditMetadata.setSpacing(true);
	final MetadataPanel metadataPanel = new MetadataPanel(documentContainer, selectedDocuments);
	metadataPanel.setCaption("");
	metadataPanel.selectDocuments(selectedDocuments.getValue());
	vlEditMetadata.addComponent(metadataPanel);
	vlEditMetadata.addComponent(btSubmit);
	return vlEditMetadata;
    }

    private Layout createMetadataInfoPanel(DomainItem<FileNode> item) {
	final VerticalLayout vl = new VerticalLayout();
	vl.setSpacing(true);

	final HorizontalLayout hl = new HorizontalLayout();
	final Label lbl = new Label(
		String.format("Metadata Versão %s", item.getItemProperty("document.versionNumber").toString()));
	lbl.addStyleName(BennuTheme.LABEL_H2);
	hl.addComponent(lbl);

	if (fileNode.isWriteGroupMember()) {
	    final Button btEditMetadata = new Button("editar");
	    btEditMetadata.setStyleName(BaseTheme.BUTTON_LINK);
	    btEditMetadata.addListener(new ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
		    editMetadataView = editMetadataPanel();
		    vl.replaceComponent(metadataInfoView, editMetadataView);
		}
	    });
	    hl.addComponent(btEditMetadata);
	}

	vl.addComponent(hl);

	final Property recentMetadata = item.getItemProperty("document.recentMetadata");
	final DomainContainer<Metadata> metadataContainer = new DomainContainer<Metadata>(
		(Collection<Metadata>) recentMetadata.getValue(), Metadata.class);
	if (metadataContainer.size() > 0) {
	    final Table table = new Table();
	    metadataContainer.setContainerProperties("keyValue", "value");
	    table.setContainerDataSource(metadataContainer);
	    table.setVisibleColumns(new Object[] { "keyValue", "value" });
	    table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
	    metadataInfoView = table;
	} else {
	    metadataInfoView = new Label("Documento sem metadata");
	}
	vl.addComponent(metadataInfoView);
	return vl;
    }

    private Component createInfoPanel(DomainItem<FileNode> item) {
	TabularViewer viewer = new TabularViewer(FMSViewerFactory.getInstance());
	List<String> propertyIds = new ArrayList<String>();
	item.addItemProperty("url", new ObjectProperty<URL>(
		DownloadUtil.getDownloadUrl(getApplication(), fileNode.getDocument()), URL.class));
	propertyIds.addAll(Arrays.asList(new String[] { "displayName", "url", "document.versionNumber", "presentationFilesize",
		"document.lastModifiedDate", "parent.displayName" }));
	viewer.setItemDataSource(item, propertyIds);
	for (Component versionEntry : getVersionsEntries(item)) {
	    viewer.addLine(versionEntry);
	}
	return viewer;
    }

}
