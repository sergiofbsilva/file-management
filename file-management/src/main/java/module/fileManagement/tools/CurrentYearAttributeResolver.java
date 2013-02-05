package module.fileManagement.tools;

import org.joda.time.DateTime;

public class CurrentYearAttributeResolver implements AttributeResolver {

    @Override
    public String get() {
        return "year";
    }

    @Override
    public String resolve() {
        return Integer.toString(new DateTime().getYear());
    }
}
