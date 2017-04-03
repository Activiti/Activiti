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

import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Joram Barrez
 * @author Bassam Al-Sarori
 */
public class SecureJavascriptUtil {

  public static Object evaluateScript(VariableScope variableScope, String script) {
    return evaluateScript(variableScope, script, null);
  }
  
  // exposes beans
  public static Object evaluateScript(VariableScope variableScope, String script, Map<Object, Object> beans) {
    Context context = Context.enter();
    try {
        Scriptable scope = context.initStandardObjects();
        SecureScriptScope secureScriptScope = new SecureScriptScope(variableScope, beans);
        scope.setPrototype(secureScriptScope);

        return context.evaluateString(scope, script, "<script>", 0, null);
    } finally {
        Context.exit();
    }
  }
  
}
