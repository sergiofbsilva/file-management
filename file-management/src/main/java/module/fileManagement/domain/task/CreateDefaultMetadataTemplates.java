package module.fileManagement.domain.task;

import pt.ist.fenixWebFramework.services.Service;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.DateTimeMetadata;
import module.fileManagement.domain.metadata.IntegerMetadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.PersonMetadata;
import module.fileManagement.domain.metadata.SeqNumberMetadata;

public class CreateDefaultMetadataTemplates extends CreateDefaultMetadataTemplates_Base {

    public CreateDefaultMetadataTemplates() {
	super();
    }

    @Override
    public String getLocalizedName() {
	return FileManagementSystem.getBundle().getString("label.task.create.default.metadata.templates");
    }

    public void resetTemplatesAndKeys() {
	for (MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplatesSet()) {
	    for (MetadataKey key : template.getKeys()) {
		if (!key.hasAnyMetadata()) {
		    key.delete();
		}
	    }
	    template.delete();
	}
    }

    public void createDRHDeclarationTemplate() {
	final String name = "Declaração DRH";
	final MetadataTemplate metadataTemplate = FileManagementSystem.getInstance().getMetadataTemplate(name);
	if (metadataTemplate == null) {
	    MetadataTemplate template = new MetadataTemplate(name);
	    MetadataKey key = MetadataKey.getOrCreateInstance("Nº Declaração", SeqNumberMetadata.class);
	    template.addKey(key, 1, Boolean.TRUE, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Data", DateTimeMetadata.class), 2, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Destinatário", PersonMetadata.class), 3, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Nº Mecanográfico", IntegerMetadata.class), 4, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Remetente", PersonMetadata.class), 5, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Observações"), 6, Boolean.FALSE);
	}
    }

    public void createDocumentTemplate() {
	final String name = "Documento";
	final MetadataTemplate metadataTemplate = FileManagementSystem.getInstance().getMetadataTemplate(name);
	if (metadataTemplate == null) {
	    MetadataTemplate template = new MetadataTemplate(name);
	    template.addKey(MetadataKey.getOrCreateInstance("Identificador"), 1, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Título"), 2, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Data", DateTimeMetadata.class), 3, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Autor"), 4, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Categoria"), 5, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Departamento"), 6, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Descrição"), 7, Boolean.FALSE);
	    template.addKey(MetadataKey.getOrCreateInstance("Entidade"), 8, Boolean.FALSE);
	}
    }

    @Service
    public void executeTask() {
	createDocumentTemplate();
	createDRHDeclarationTemplate();
    }

}
