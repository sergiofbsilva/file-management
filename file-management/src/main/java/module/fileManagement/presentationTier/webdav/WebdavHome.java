package module.fileManagement.presentationTier.webdav;

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
import static module.fileManagement.domain.FileManagementSystem.getMessage;

import java.util.Map;

import module.fileManagement.domain.webdav.WebdavAuthentication;
import module.vaadin.ui.BennuTheme;
import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.User;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "WebdavHome" })
/**
 * 
 * @author Sérgio Silva
 * 
 */
public class WebdavHome extends CustomComponent implements EmbeddedComponentContainer {

    private final GridSystemLayout gsl;

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
        final User currentUser = Authenticate.getCurrentUser();
        return currentUser != null && currentUser.hasRoleType(RoleType.MANAGER);
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
        // TODO Auto-generated method stub
    }

    private void createWebdavLink(final Layout content) {
        final User currentUser = Authenticate.getCurrentUser();
        final boolean hasWebDavAuth = currentUser.getWebdavAuthentication() != null;
        if (hasWebDavAuth) {
            Label lblInstructions =
                    new Label(getMessage("webdav.message.instructions", currentUser.getUsername(), currentUser
                            .getWebdavAuthentication().getToken()), Label.CONTENT_XHTML);
            content.addComponent(lblInstructions);
        }
        final String btCaption = String.format("webdav.button.%s.token", hasWebDavAuth ? "generate" : "create");
        final Button btCreateWebdavToken = new Button(getMessage(btCaption), new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (hasWebDavAuth) {
                    currentUser.getWebdavAuthentication().generateNewToken();
                } else {
                    WebdavAuthentication.getOrCreateWebdavAuthentication(currentUser);
                }
                content.removeAllComponents();
                createWebdavLink(content);
            }
        });
        btCreateWebdavToken.setStyleName(BennuTheme.BUTTON_LINK);
        content.addComponent(btCreateWebdavToken);
    }

    public Panel createWebdavPanel() {
        final Panel webDavPanel = new Panel();
        final VerticalLayout vlWebdavContent = new VerticalLayout();
        vlWebdavContent.setSpacing(Boolean.TRUE);
        webDavPanel.setContent(vlWebdavContent);

        webDavPanel.setStyleName(BennuTheme.PANEL_LIGHT);
        webDavPanel.setCaption(getMessage("webdav.title"));

        vlWebdavContent.addComponent(new Label(getMessage("webdav.message"), Label.CONTENT_XHTML));
        createWebdavLink(vlWebdavContent);

        return webDavPanel;
    }

    public void createPage() {
        gsl.setCell("webdav", 16, createWebdavPanel());
    }

    public WebdavHome() {
        gsl = new GridSystemLayout();
        setCompositionRoot(gsl);
    }

    @Override
    public void attach() {
        super.attach();
        createPage();
    }

}
