package module.fileManagement.domain.task;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.DateTimeMetadata;
import module.fileManagement.domain.metadata.IntegerMetadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.SeqNumberMetadata;
import module.fileManagement.domain.metadata.StringMetadata;
import pt.ist.fenixWebFramework.services.Service;

public class CreateDefaultMetadataTemplates extends CreateDefaultMetadataTemplates_Base {

    public static final String[] KEYS = { "Título", "Autor", "Entidade", "Departamento", "Data", "Descrição", "Categoria",
	    "Identificador" };

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
	    template.addKey(MetadataKey.getOrCreateInstance("Nº Declaração", SeqNumberMetadata.class), 1, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Data", DateTimeMetadata.class), 2, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Destinatário", StringMetadata.class), 3, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Nº Mecanográfico", IntegerMetadata.class), 4, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Remetente", StringMetadata.class), 5, Boolean.TRUE);
	    template.addKey(MetadataKey.getOrCreateInstance("Observações", StringMetadata.class), 6, Boolean.FALSE);
	}
    }

    @Service
    public void executeTask() {
	resetTemplatesAndKeys();
	createDRHDeclarationTemplate();
    }

}
