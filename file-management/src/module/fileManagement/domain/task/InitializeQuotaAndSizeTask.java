package module.fileManagement.domain.task;

import myorg.domain.MyOrg;
import myorg.domain.contents.Node;
import myorg.domain.scheduler.WriteCustomTask;
import pt.ist.bennu.vaadin.domain.contents.VaadinNode;

public class InitializeQuotaAndSizeTask extends WriteCustomTask {
    protected void doService() {
	for (Node node : MyOrg.getInstance().getNodes()) {
	    if (node instanceof VaadinNode) {
		final VaadinNode vaadinNode = (VaadinNode) node;
		final String argument = vaadinNode.getArgument();
		String replace = "";
		if (argument.startsWith("PageView")) {
		    final String[] split = argument.split("-");
		    replace = "PageView?page=" + split[1];
		} else {
		    if (argument.endsWith("-")) {
			replace = argument.substring(0, argument.length() - 1);
		    }
		}
		if (!replace.isEmpty()) {
		    // vaadinNode.setArgument(replace);
		    out.printf("arg %s replaced by %s\n", argument, replace);
		}
	    }
	}
    }
}
