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

import java.util.Set;

import org.activiti.tasks.secure.impl.ClassWhitelister;
import org.mozilla.javascript.ClassShutter;

/**
 * A {@link ClassShutter} that uses a {@link Set} of Strings denoting fully qualified classnames
 * that are white listed for usage in scripts.
 * 
 * @author Joram Barrez
 * @author Bassam Al-Sarori
 */
public class SecureScriptClassShutter implements ClassShutter {

    /**
     * An implementation of {@link ClassWhitelister}
     * if set will be used to determine whether 
     * a class is visible for scripts or not
     */
    protected ClassWhitelister classWhitelister;

    @Override
    public boolean visibleToScripts(String fqcn) {
        return classWhitelister.isWhitelisted(fqcn);
    }
    
    public ClassWhitelister getClassWhitelister() {
      return classWhitelister;
    }

    public void setClassWhitelister(ClassWhitelister classWhitelister) {
      this.classWhitelister = classWhitelister;
    }

}
