/*
 * @(#)ManageMetadataTemplates.java
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

import java.util.Map;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.MetadataTemplate;

import org.vaadin.dialogs.ConfirmDialog;

import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.google.common.base.Joiner;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
@EmbeddedComponent(path = { "ManageMetadataTemplates" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class ManageMetadataTemplates extends CustomComponent implements EmbeddedComponentContainer {
	@Override
	public boolean isAllowedToOpen(Map<String, String> arguments) {
		return true;
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		// TODO Auto-generated method stub

	}

	private Label createTemplateDescription(final MetadataTemplate template) {
		final String templateName = template.getName();
		final Label label = new Label();
		if (template.hasAnyKey()) {
			final String keyNames = Joiner.on(",").join(template.getKeyNames());
			label.setValue(String.format("%s [%s]", templateName, keyNames));
		} else {
			label.setValue(templateName);
		}
		return label;
	}

	private void createTemplateEntry(final Layout container, final MetadataTemplate template) {
		final HorizontalLayout fl = new HorizontalLayout();
		fl.setWidth("60%");
		fl.setSpacing(true);
		fl.setMargin(false, false, true, true);

		final Label lblName = createTemplateDescription(template);
		lblName.setStyleName("lbl-template-name");
		fl.addComponent(lblName);

		Button btView = new Button("Ver");
		Button btEdit = new Button("Editar");
		Button btDelete = new Button("Apagar");
		btView.setStyleName(BaseTheme.BUTTON_LINK);
		btEdit.setStyleName(BaseTheme.BUTTON_LINK);
		btDelete.setStyleName(BaseTheme.BUTTON_LINK);

		btView.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				EmbeddedApplication.open(getApplication(), CreateMetadataTemplate.class, template.getExternalId(), "view");
			}
		});

		btEdit.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				EmbeddedApplication.open(getApplication(), CreateMetadataTemplate.class, template.getExternalId());
				// getWindow().open(new
				// ExternalResource("#CreateMetadataTemplate-" +
				// template.getExternalId() + "-"));
			}
		});

		btDelete.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				final String templateName = template.getName();
				ConfirmDialog.show(getWindow(), "Apagar Template " + templateName, "Deseja apagar o template " + templateName
						+ " ? ", "Sim", "Não", new ConfirmDialog.Listener() {

					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							template.delete();
							container.removeComponent(fl);
						}
					}
				});
			}
		});

		HorizontalLayout hlButtons = new HorizontalLayout();

		hlButtons.addComponent(btView);
		hlButtons.addComponent(btEdit);
		hlButtons.addComponent(btDelete);
		hlButtons.setSpacing(true);
		hlButtons.setSizeFull();

		fl.addComponent(lblName);
		fl.addComponent(hlButtons);
		fl.setExpandRatio(lblName, 2);
		fl.setExpandRatio(hlButtons, 1);

		container.addComponent(fl);
	}

	@Override
	public void attach() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.addComponent(new Label("<h2>Templates</h2>", Label.CONTENT_XHTML));

		Button btCreateTemplate = new Button("+ Criar Template");

		btCreateTemplate.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				EmbeddedApplication.open(getApplication(), CreateMetadataTemplate.class);
			}
		});
		vl.addComponent(btCreateTemplate);
		for (MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplates()) {
			createTemplateEntry(vl, template);
		}
		setCompositionRoot(vl);
	}

}
