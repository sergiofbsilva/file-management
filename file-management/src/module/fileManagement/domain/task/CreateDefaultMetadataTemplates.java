package module.fileManagement.domain.task;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.MetadataKey;
import module.fileManagement.domain.MetadataTemplate;
import pt.ist.fenixWebFramework.services.Service;

public class CreateDefaultMetadataTemplates extends CreateDefaultMetadataTemplates_Base {
    
    private static final String[] KEYS = { "Título", "Autor" , "Data" , "Entidade" , "Departamento" , "Data", "Descrição" , "Categoria", "Identificador" } ;
    
    public  CreateDefaultMetadataTemplates() {
        super();
    }

    @Override
    public String getLocalizedName() {
	return FileManagementSystem.getBundle().getString("label.task.create.default.metadata.templates");
    }
    
    @Service
    public void resetTemplatesAndKeys() {
	for (MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplatesSet()) {
	    for(MetadataKey key : template.getKeys()) {
		key.delete();
	    }
	    template.delete();
	}
    }
    
    @Service
    public void executeTask() {
	resetTemplatesAndKeys();
	MetadataTemplate template = new MetadataTemplate("Documento");
	for (String key : KEYS) {
	    template.addKeys(MetadataKey.getOrCreateInstance(key));
	}
    }
    
}
