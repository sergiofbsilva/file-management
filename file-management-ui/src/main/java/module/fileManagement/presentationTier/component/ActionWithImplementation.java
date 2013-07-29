package module.fileManagement.presentationTier.component;

import com.vaadin.event.Action;
import com.vaadin.terminal.Resource;

public abstract class ActionWithImplementation extends Action {

    public ActionWithImplementation(String caption) {
        super(caption);
    }

    public ActionWithImplementation(final String caption, final Resource resource) {
        super(caption, resource);
    }

    public abstract void perform(final Object target);

}
