package module.fileManagement.presentationTier.pages;

import module.fileManagement.domain.DirNode;

import com.vaadin.ui.CustomComponent;

public class DocumentUpload extends CustomComponent {

    final DirNode targetDir;

    @Override
    public void attach() {
        super.attach();
    }

    public DocumentUpload(DirNode dir) {
        targetDir = dir;
    }
}
