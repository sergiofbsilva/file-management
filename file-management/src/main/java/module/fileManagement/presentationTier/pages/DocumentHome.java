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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;
import module.fileManagement.presentationTier.data.AbstractSearchContainer;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.vaadin.ui.BennuTheme;

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.Presentable;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.User;
import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.BuildingState;
import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.DSLState;
import pt.ist.vaadinframework.EmbeddedApplication;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.GridSystemLayout;
import pt.ist.vaadinframework.ui.TimeoutSelect;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@EmbeddedComponent(path = { "DocumentHome" })
/**
 * 
 * @author Pedro Santos
 * @author Sérgio Silva
 * 
 */
public class DocumentHome extends CustomComponent implements EmbeddedComponentContainer {

	private GridSystemLayout gsl;
	private Component webdav;

	@Override
	public boolean isAllowedToOpen(Map<String, String> arguments) {
		return Authenticate.getCurrentUser() != null;
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		// TODO Auto-generated method stub
	}

	private static class UnitContainer extends DomainContainer<Unit> {

		public UnitContainer() {
			super(Unit.class);
		}

		public UnitContainer(Collection<Unit> elements, Class<? extends Unit> elementType,
				pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
			super(elements, elementType, hints);
			// TODO Auto-generated constructor stub
		}

		public UnitContainer(Property wrapped, Class<? extends Unit> elementType,
				pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
			super(wrapped, elementType, hints);
			// TODO Auto-generated constructor stub
		}

		public UnitContainer(Class<? extends Unit> elementType, pt.ist.vaadinframework.data.HintedProperty.Hint[] hints) {
			super(elementType, hints);
		}

		@Override
		protected DSLState createFilterExpression(String filterText) {
			filterText = StringNormalizer.normalize(filterText);
			if (!StringUtils.isEmpty(filterText)) {
				final String[] split = filterText.trim().split("\\s+");
				BuildingState expr = new BuildingState();
				for (int i = 0; i < split.length; i++) {
					final String normalizedSplit = StringNormalizer.normalize(split[i]);
					if (i == split.length - 1) {
						return expr.matches(Unit.IndexableFields.UNIT_NAME, normalizedSplit).or()
								.matches(Unit.IndexableFields.UNIT_ACRONYM, normalizedSplit);
					}
					expr =
							expr.matches(Unit.IndexableFields.UNIT_NAME, normalizedSplit).or()
									.matches(Unit.IndexableFields.UNIT_ACRONYM, normalizedSplit).or();
				}
			}
			return new BuildingState();
		}
	}

	private static class UnitSearchContainer extends AbstractSearchContainer {

		public UnitSearchContainer(Class elementType, pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
			super(elementType, hints);
		}

		public UnitSearchContainer(List elements, Class elementType, pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
			super(elements, elementType, hints);
		}

		public UnitSearchContainer(Property wrapped, Class elementType, pt.ist.vaadinframework.data.HintedProperty.Hint... hints) {
			super(wrapped, elementType, hints);
		}

		@Override
		public void search(String filterText) {
			removeAllItems();
			final UnitContainer unitContainer = new UnitContainer();
			unitContainer.addContainerFilter(new Filter() {

				@Override
				public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
					final Unit unit = (Unit) itemId;
					return unit.hasFileRepository()
							&& (Authenticate.getCurrentUser().hasRoleType(RoleType.MANAGER) || unit.getFileRepository()
									.isAccessible());
				}

				@Override
				public boolean appliesToProperty(Object propertyId) {
					return true;
				}
			});
			unitContainer.search(filterText);
			addItems((Collection<Presentable>) unitContainer.getItemIds());
		}
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
		/*
		 * DomainItem item = new DomainItem(fileNode);
		 */return pDetails;
	}

	public static Panel createDirDetails() {
		Panel pDetails = new Panel(getMessage("label.file.details"));
		pDetails.addComponent(new Label("Name: document.doc", Label.CONTENT_TEXT));
		pDetails.addComponent(new Label("Size: 120kb", Label.CONTENT_TEXT));
		return pDetails;
	}

	public Component createRepositorySelect() {
		final UnitSearchContainer unitSearchContainer = new UnitSearchContainer(Unit.class);
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(Boolean.TRUE);
		hl.setSizeFull();

		final TimeoutSelect select = new TimeoutSelect();
		select.setSizeFull();
		select.setImmediate(true);
		select.setContainerDataSource(unitSearchContainer);
		select.setItemCaptionPropertyId("presentationName");
		select.addListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {
				unitSearchContainer.search(event.getText());
			}

		});

		Button btNavigate = new Button("Navegar", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				final Party party = (Party) select.getValue();
				if (party != null) {
					final DirNode rootDir = party.getFileRepository();
					EmbeddedApplication.open(getApplication(), DocumentBrowse.class, rootDir.getContextPath().toString());
				}
			}
		});
		// btNavigate.addStyleName(BennuTheme.BUTTON_LINK);
		btNavigate.setSizeFull();

		hl.addComponent(select);
		hl.addComponent(btNavigate);
		hl.setExpandRatio(select, 0.7f);
		hl.setExpandRatio(btNavigate, 0.3f);

		final Panel panel = new Panel("Procurar Repositório");
		panel.setContent(hl);
		panel.setStyleName(BennuTheme.PANEL_LIGHT);
		return panel;
	}

	private Component createRepositoryDashboard() {
		final Panel panel = new Panel("Repositórios Disponíveis");

		panel.setStyleName(BennuTheme.PANEL_LIGHT);
		((VerticalLayout) panel.getContent()).setSpacing(Boolean.TRUE);

		final User currentUser = Authenticate.getCurrentUser();
		final Person person = currentUser.getPerson();

		final DirNode sharedFolder = person.getFileRepository().getSharedFolder();
		panel.addComponent(getDirNodeLink(sharedFolder.getDisplayName(), sharedFolder));

		for (final DirNode repository : FileRepository.getAvailableRepositories(currentUser)) {
			panel.addComponent(addRepositoryButton(repository));
		}
		return panel;
	}

	private Button addRepositoryButton(final DirNode repository) {
		return getDirNodeLink(repository.getParty().getPresentationName(), repository);
	}

	private Button getDirNodeLink(final String linkName, final DirNode dirNode) {
		final Button btRepository = new Button(linkName, new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				EmbeddedApplication.open(getApplication(), DocumentBrowse.class, dirNode.getContextPath().toString());
			}
		});
		btRepository.addStyleName(BaseTheme.BUTTON_LINK);
		return btRepository;
	}

	public void createPage() {
		gsl.setCell("welcome", 16, createWelcomePanel());
		gsl.setCell("available_repositories", 16, createRepositoryDashboard());
		gsl.setCell("select_repository", 0, 9, 7, createRepositorySelect());
		// gsl.setCell("testjersey", 16, createTestJersey());
	}

	// private Component createTestJersey() {
	// VerticalLayout vl = new VerticalLayout();
	// final Label lbl = new Label();
	// lbl.setCaption("Request:");
	// Button bt = new Button("do request", new ClickListener() {
	//
	// @Override
	// public void buttonClick(ClickEvent event) {
	//
	// Map<String, String> args = new HashMap<String, String>();
	// args.put("__username__", "1");
	// args.put("__password__", "2");
	// final String host =
	// "http://ashtray.ist.utl.pt:8080/ciapl/jersey/services/";
	// lbl.setValue(new
	// JerseyClient().connect(host).method("helloworld").arg("__username__",
	// "1")
	// .arg("__password__", "3").get());
	// }
	// });
	//
	// vl.addComponent(lbl);
	// vl.addComponent(bt);
	// return vl;
	// }

	public DocumentHome() {
		gsl = new GridSystemLayout();
		setCompositionRoot(gsl);
	}

	@Override
	public void attach() {
		super.attach();
		createPage();
	}

}
