package module.fileManagement.presentationTier.pages;

import java.util.List;
import java.util.Map;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.organization.domain.Unit;
import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.ItemConstructor;
import pt.ist.vaadinframework.data.hints.Options;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@EmbeddedComponent(path = { "ManageDirProperties" }, args = { "dirNode" })
public class ManageDirProperties extends CustomComponent implements EmbeddedComponentContainer {

	public final class DirNodePartyConstructor implements ItemConstructor<Object> {

		@Override
		public Object[] getOrderedArguments() {
			return new Object[] { "unit" };
		}

		public DirNode createDir(final Unit unit) {
			return new DirNode(unit);
		}
	}

	private DomainItem<DirNode> dirNodeItem;
	private VerticalLayout mainLayout;

	@Override
	public boolean isAllowedToOpen(Map<String, String> args) {
		return Authenticate.getCurrentUser().hasRoleType(RoleType.MANAGER);
	}

	@Override
	public void setArguments(Map<String, String> args) {
		String dirNodeOid = args.get("dirNode");
		setDirNode((DirNode) AbstractDomainObject.fromExternalId(dirNodeOid));
	}

	private void setDirNode(DirNode dirNode) {
		dirNodeItem.setValue(dirNode);
		updateContent();
	}

	public ManageDirProperties() {
		dirNodeItem = new DomainItem<DirNode>(DirNode.class);
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		setCompositionRoot(mainLayout);
	}

	private void updateContent() {
		mainLayout.removeAllComponents();
		mainLayout.addComponent(new Label("<h3>Opções Directoria</h3>", Label.CONTENT_XHTML));
		setTemplateSelect();
		final TransactionalForm form = new TransactionalForm(FileManagementSystem.BUNDLE);
		form.setItemDataSource(dirNodeItem);
		form.setVisibleItemProperties(new String[] { "sequenceNumber", "filenameTemplate", "defaultTemplate" });
		form.addButton("Submeter", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				form.commit();
				getWindow().showNotification("Alterações efectuadas");
			}
		});
		form.addClearButton();
		form.addCancelButton();
		mainLayout.addComponent(form);

		// Button btCalcSize = new Button("recalcular tamanho", new
		// ClickListener() {
		//
		// @Override
		// public void buttonClick(ClickEvent event) {
		//
		// }
		// });
		// mainLayout.addComponent(btCalcSize);
	}

	private void setTemplateSelect() {
		final List<MetadataTemplate> metadataTemplates = FileManagementSystem.getInstance().getMetadataTemplates();
		final DomainItem<MetadataTemplate> defaultTemplate =
				(DomainItem<MetadataTemplate>) dirNodeItem.getItemProperty("defaultTemplate");
		final DomainContainer<MetadataTemplate> domainContainer =
				new DomainContainer<MetadataTemplate>(metadataTemplates, MetadataTemplate.class);
		final Options options = new Options(domainContainer);
		options.captionPropertyId("name");
		defaultTemplate.addHint(options);
	}
}
