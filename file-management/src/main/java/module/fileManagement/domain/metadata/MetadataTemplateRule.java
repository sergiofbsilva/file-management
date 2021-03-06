package module.fileManagement.domain.metadata;

import pt.ist.fenixframework.Atomic;

public class MetadataTemplateRule extends MetadataTemplateRule_Base implements Comparable<MetadataTemplateRule> {

    public MetadataTemplateRule(MetadataTemplate template, MetadataKey key, Integer position, Boolean required, Boolean readOnly) {
        super();
        setTemplate(template);
        setKey(key);
        setPosition(position);
        setRequired(required);
        setReadOnly(readOnly);
    }

    @Override
    public int compareTo(MetadataTemplateRule o) {
        return getPosition().compareTo(o.getPosition());
    }

    @Atomic
    public void delete() {
        setTemplate(null);
        setKey(null);
        deleteDomainObject();
    }
}
