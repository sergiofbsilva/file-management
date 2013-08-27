/*
 * @(#)DocumentBrowse.java
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

import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.ContextPath;
import module.fileManagement.presentationTier.component.AddDirWindow;
import module.fileManagement.presentationTier.component.DocumentFileBrowser;
import module.fileManagement.presentationTier.component.NodeDetails;
import pt.ist.bennu.core.security.Authenticate;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.FlowLayout;
import pt.ist.vaadinframework.ui.GridSystemLayout;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

@EmbeddedComponent(path = { "DocumentBrowse" }, args = { "contextPath" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentBrowse extends CustomComponent implements EmbeddedComponentContainer {

    NodeDetails fileDetails;
    DocumentFileBrowser browser;
    Layout mainLayout;
    GridSystemLayout gsl;
    Button btUpload;

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
        return Authenticate.getUser() != null;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
        final String pathString = arguments.get("contextPath");
        if (pathString != null) {
            browser.setContextPath(pathString);
        } else {
            browser.setContextPath(new ContextPath(Authenticate.getUser().getFileRepository()));
        }
        setFileDetails(browser.getNodeItem());
    }

    public HorizontalLayout createButtons() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        Button btNewFolder = new Button(getMessage("button.new.folder"));

        btNewFolder.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (browser.getDirNode().isWriteGroupMember()) {
                    final AddDirWindow addDirWindow = new AddDirWindow(browser);
                    getWindow().addWindow(addDirWindow);
                } else {
                    getWindow().showNotification(getMessage("message.dir.cannot.write"));
                }
            }
        });
        layout.addComponent(btNewFolder);

        btUpload = new Button(getMessage("button.upload"));

        btUpload.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                final String contextPath = browser.getContextPath().toString();
                EmbeddedApplication.open(getApplication(), UploadPage.class, contextPath);
            }
        });

        layout.addComponent(btUpload);
        return layout;
    }

    public GridLayout createGrid(Component browser) {
        GridLayout grid = new GridLayout(2, 1);
        grid.setSizeFull();
        grid.setMargin(true, true, true, false);
        grid.setSpacing(true);
        browser.setSizeFull();
        grid.addComponent(browser, 0, 0);
        return grid;
    }

    public FlowLayout createNavigationLink(String path) {
        FlowLayout layout = new FlowLayout();
        layout.setSizeFull();
        final String[] split = path.split("/");
        String link = new String();
        for (int i = 0; i < split.length - 1; i++) {
            link += String.format("<a href='dir#%s'>%s</a> > ", split[i], split[i]);
        }
        link += split[split.length - 1];
        layout.addComponent(new Label(link, Label.CONTENT_XHTML));
        return layout;
    }

    public DocumentFileBrowser createBrowser() {
        DocumentFileBrowser browser = new DocumentFileBrowser();
        return browser;
    }

    public Layout createPage() {
        browser = createBrowser();
        gsl = new GridSystemLayout();
        gsl.setCell("quota", 16, new QuotaLabel(browser.getNodeItem()));
        gsl.setCell("buttons", 16, createButtons());
        gsl.setCell("browser", 10, browser);
        // setFileDetails(browser.getNodeItem());
        return gsl;
    }

    public void setFileDetails(DomainItem<AbstractFileNode> nodeItem) {
        fileDetails = NodeDetails.makeDetails(nodeItem);
        fileDetails.setDocumentBrowse(this);
        gsl.setCell("info", 6, fileDetails);
    }

    public DocumentBrowse() {
        mainLayout = createPage();
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
        browser.addDirChangedListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                final DomainItem<AbstractFileNode> domainItem = (DomainItem<AbstractFileNode>) event.getProperty();
                setFileDetails(domainItem);
            }
        });
        setCompositionRoot(mainLayout);
    }

    @SuppressWarnings("unused")
    private class QuotaLabel extends Label {

        private String quotaContent = null;

        public QuotaLabel(DomainItem<AbstractFileNode> item) {
            setPropertyDataSource(item);
            setContentMode(Label.CONTENT_XHTML);
            setContent();
            Property.ValueChangeListener listener = new Property.ValueChangeListener() {

                @SuppressWarnings("unchecked")
                @Override
                public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                    setContent();
                }

            };
            getItem().addListener(listener);
        }

        @SuppressWarnings("unchecked")
        private DomainItem<AbstractFileNode> getItem() {
            return (DomainItem<AbstractFileNode>) getPropertyDataSource();
        }

        private void setContent() {
            DomainItem<AbstractFileNode> dirNode = getItem();
            final String percentTotalUsedSpace = dirNode.getItemProperty("percentOfTotalUsedSpace").toString();
            final String totalUsedSpace = dirNode.getItemProperty("presentationTotalUsedSpace").toString();
            final String quota = dirNode.getItemProperty("presentationQuota").toString();
            quotaContent =
                    String.format("%s espaco utilizado</br>A utilizar %s dos seus %s", percentTotalUsedSpace, totalUsedSpace,
                            quota);
        }

        @Override
        public String toString() {
            return quotaContent;
        }
    }

    public void refresh() {
        fileDetails.updateFilePanel();
    }

    public void removeNode(DomainItem<AbstractFileNode> nodeItem) {
        browser.removeNodeAndSelectNext(nodeItem.getValue());
    }

    public ContextPath getContextPath() {
        return browser.getContextPath();
    }

    public DocumentFileBrowser getBrowser() {
        return browser;
    }

}
