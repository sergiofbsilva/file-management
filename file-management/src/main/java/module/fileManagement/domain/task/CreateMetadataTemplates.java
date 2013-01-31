package module.fileManagement.domain.task;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.metadata.DateTimeMetadata;
import module.fileManagement.domain.metadata.IntegerMetadata;
import module.fileManagement.domain.metadata.MetadataKey;
import module.fileManagement.domain.metadata.MetadataTemplate;
import module.fileManagement.domain.metadata.PersonMetadata;
import module.fileManagement.domain.metadata.SeqNumberMetadata;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

public class CreateMetadataTemplates extends WriteCustomTask {

	public void createDRHDeclarationTemplate() {
		final String name = "Declaração DRH";
		final MetadataTemplate metadataTemplate = FileManagementSystem.getInstance().getMetadataTemplate(name);
		if (metadataTemplate == null) {
			MetadataTemplate template = new MetadataTemplate(name);
			template.addKey(MetadataKey.getOrCreateInstance("Nº Declaração", SeqNumberMetadata.class), 1, Boolean.TRUE,
					Boolean.TRUE);
			template.addKey(MetadataKey.getOrCreateInstance("Data", DateTimeMetadata.class), 2, Boolean.TRUE);
			template.addKey(MetadataKey.getOrCreateInstance("Destinatário", PersonMetadata.class), 3, Boolean.FALSE);
			template.addKey(MetadataKey.getOrCreateInstance("Nº Mecanográfico", IntegerMetadata.class), 4, Boolean.FALSE);
			template.addKey(MetadataKey.getOrCreateInstance("Remetente", PersonMetadata.class), 5, Boolean.TRUE);
			template.addKey(MetadataKey.getOrCreateInstance("Observações"), 6, Boolean.FALSE);
		} else {
			out.printf("template already created %s\n", name);
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
		} else {
			out.printf("template already created %s\n", name);
		}
	}

	@Override
	protected void doService() {
		createDocumentTemplate();
		createDRHDeclarationTemplate();
	}
}
