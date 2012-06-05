package module.fileManagement.domain.metadata;

import pt.ist.fenixWebFramework.services.Service;

public class MetadataTemplateRule extends MetadataTemplateRule_Base implements Comparable<MetadataTemplateRule> {

    public MetadataTemplateRule(MetadataTemplate template, MetadataKey key, Integer position, Boolean required) {
	super();
	setTemplate(template);
	setKey(key);
	setPosition(position);
	setRequired(required);
    }

    @Override
    public int compareTo(MetadataTemplateRule o) {
	return getPosition().compareTo(o.getPosition());
    }

    @Service
    public void delete() {
	setTemplate(null);
	setKey(null);
	deleteDomainObject();
    }
}
