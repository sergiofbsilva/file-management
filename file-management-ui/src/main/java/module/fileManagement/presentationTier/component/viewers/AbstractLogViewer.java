package module.fileManagement.presentationTier.component.viewers;

import java.util.List;

import com.vaadin.ui.Component;

public interface AbstractLogViewer extends Component {

    public boolean hasOperations();

    public List<Component> getOperations();
}
