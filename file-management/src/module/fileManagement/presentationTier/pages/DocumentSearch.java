package module.fileManagement.presentationTier.pages;

import java.util.Map;

import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.ui.CustomComponent;

@EmbeddedComponent(path = { "DocumentSearch" })
public class DocumentSearch extends CustomComponent implements EmbeddedComponentContainer {

    @Override
    public boolean isAllowedToOpen(Map<String, String> arguments) {
	return true;
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
	// TODO Auto-generated method stub

    }

}
