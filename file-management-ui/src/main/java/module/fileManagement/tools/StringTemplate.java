package module.fileManagement.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/*
 * This is class is used to preform strings substitutions based on attributes
 * resolvers. Attributes are defined as $<attributeName>$ in the template
 * string.
 */

public class StringTemplate {
    private final Function PARAMETER_TO_ATTRIBUTE = new Function<String, String>() {

        @Override
        public String apply(String arg0) {
            return arg0.replace("$", "");
        }
    };

    private final Pattern ATTRIBUTE_REGEXP = Pattern.compile("\\$.*?\\$");
    private String template;
    private final Map<String, AttributeResolver> resolvers = new HashMap<String, AttributeResolver>();

    public StringTemplate() {

    }

    public StringTemplate(final String template) {
        this.template = template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    /*
     * Returns the list of defined parameters in current template A parameter is
     * simply a attribute within '$'
     */
    private Set<String> getParameters() {
        final Set<String> attributes = new HashSet<String>();
        final Matcher matcher = ATTRIBUTE_REGEXP.matcher(template);
        while (matcher.find()) {
            attributes.add(matcher.group());
        }
        return attributes;
    }

    public Collection<String> getAttributes() {
        return Collections2.transform(getParameters(), PARAMETER_TO_ATTRIBUTE);
    }

    public String getValue() {
        String processed = new String(template);
        for (String attribute : getParameters()) {
            final String resolvedAttribute = resolveAttribute((String) PARAMETER_TO_ATTRIBUTE.apply(attribute));
            processed = processed.replace(attribute, resolvedAttribute);
        }
        return processed;
    }

    public String resolveAttribute(String attribute) {
        return getResolver(attribute).resolve();
    }

    protected AttributeResolver getResolver(String attribute) {
        return resolvers.get(attribute);
    }

    public void addResolver(AttributeResolver resolver) {
        resolvers.put(resolver.get(), resolver);
    }

}
