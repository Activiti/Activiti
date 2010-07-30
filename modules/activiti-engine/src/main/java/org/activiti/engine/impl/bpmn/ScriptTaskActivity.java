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
package org.activiti.engine.impl.bpmn;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.activity.ActivityContext;


/**
 * activity implementation of the BPMN 2.0 script task.
 * 
 * @author Joram Barrez
 */
public class ScriptTaskActivity extends TaskActivity {
  
  private final String script;
  
  private final String language;

  public ScriptTaskActivity(String script, String language) {
    this.script = script;
    this.language = language;
  }
  
  public void start(ActivityContext activityContext) throws Exception {
    ScriptingEngines scriptingEngines = CommandContext
      .getCurrent()
      .getProcessEngineConfiguration()
      .getScriptingEngines();
    
    scriptingEngines.evaluate(script, language, activityContext);
    leave(activityContext);
  }
  
}
