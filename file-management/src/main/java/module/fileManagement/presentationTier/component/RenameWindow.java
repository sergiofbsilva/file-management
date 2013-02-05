package module.fileManagement.presentationTier.component;

import static module.fileManagement.domain.FileManagementSystem.getMessage;
import module.fileManagement.domain.AbstractFileNode;
import module.fileManagement.domain.FileManagementSystem;
import pt.ist.vaadinframework.data.reflect.DomainItem;
import pt.ist.vaadinframework.ui.TransactionalForm;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class RenameWindow extends Window {

    public RenameWindow(final DomainItem<AbstractFileNode> nodeItem) {
        super(getMessage("label.rename"));

        setModal(true);
        setWidth(50, UNITS_PERCENTAGE);

        final VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        final TransactionalForm form = new TransactionalForm(FileManagementSystem.getBundleName());
        form.setWriteThrough(false);
        form.addButton(getMessage("label.save"), new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                form.commit();
                close();
            }

        });
        form.addButton(getMessage("label.close"), new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        });

        form.setItemDataSource(nodeItem);
        form.setVisibleItemProperties(new String[] { "displayName" });
        // form.getField("displayName").addValidator(new Validator() {
        //
        // @Override
        // public void validate(Object value) throws InvalidValueException {
        // if (!isValid(value)) {
        // throw new
        // InvalidValueException("Já existe um ficheiro com esse nome");
        // }
        // }
        //
        // @Override
        // public boolean isValid(Object value) {
        // AbstractFileNode node = nodeItem.getValue();
        // final DirNode dirNode = (DirNode) (node instanceof FileNode ?
        // ((FileNode) node).getParent() : node);
        // final String displayName = (String) value;
        // return node.getDisplayName().equals(displayName) ||
        // !dirNode.hasNode(displayName);
        // }
        //
        // });
        layout.addComponent(form);
    }
}