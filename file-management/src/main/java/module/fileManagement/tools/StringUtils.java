package module.fileManagement.tools;

import pt.utl.ist.fenix.tools.util.StringNormalizer;

public class StringUtils extends org.apache.commons.lang.StringUtils {

    /**
     * This method searches for needle in stack using normalized strings
     * 
     * @param stack
     * @param needle
     * @return
     */
    public static boolean matches(String stack, String needle) {
	if (StringUtils.isBlank(stack) || StringUtils.isBlank(needle)) {
	    return false;
	}
	String[] needles = StringNormalizer.normalize(needle).toLowerCase().split(" ");
	final String normalizedStack = StringNormalizer.normalize(stack).toLowerCase();
	for (String value : needles) {
	    if (StringUtils.contains(normalizedStack, value)) {
		return true;
	    }
	}
	return false;
    }
}
