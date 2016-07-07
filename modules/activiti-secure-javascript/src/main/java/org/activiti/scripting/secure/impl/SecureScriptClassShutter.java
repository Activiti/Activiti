/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.scripting.secure.impl;

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
    
    public void removeWhiteListedClass(String fqcn) {
      whiteListedClasses.remove(fqcn);
  }

    public Set<String> getWhiteListedClasses() {
        return whiteListedClasses;
    }

    public void setWhiteListedClasses(Set<String> whiteListedClasses) {
        this.whiteListedClasses = whiteListedClasses;
    }

}
