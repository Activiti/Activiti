package org.activiti.impl.scripting.secure.rhino;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.ClassShutter;

/**
 * A {@link ClassShutter} that uses a {@link Set} of Strings denoting fully qualified classnames
 * that are white listed for usage in scripts.
 * 
 * @author Joram Barrez
 */
public class SecureScriptClassShutter implements ClassShutter {

    /**
     * A collection of whitelisted classnames.
     * For each Java class used in a script, this collection will be checked.
     */
    protected Set<String> whiteListedClasses = new HashSet<String>();

    @Override
    public boolean visibleToScripts(String fullClassName) {
        return whiteListedClasses.contains(fullClassName);
    }

    public void addWhiteListedClass(String fqcn) {
        whiteListedClasses.add(fqcn);
    }

    public Set<String> getWhiteListedClasses() {
        return whiteListedClasses;
    }

    public void setWhiteListedClasses(Set<String> whiteListedClasses) {
        this.whiteListedClasses = whiteListedClasses;
    }

}
