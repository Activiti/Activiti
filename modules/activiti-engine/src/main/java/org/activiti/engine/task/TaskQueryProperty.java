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

package org.activiti.engine.task;

import java.util.HashMap;
import java.util.Map;



/**
 * Contains the possible properties that can be used in a {@link TaskQuery}.
 * 
 * @author Joram Barrez
 */
public class TaskQueryProperty {
  
  private static final Map<String, TaskQueryProperty> properties = new HashMap<String, TaskQueryProperty>();

  public static final TaskQueryProperty TASK_ID = new TaskQueryProperty("T.ID_");
  public static final TaskQueryProperty NAME = new TaskQueryProperty("T.NAME_");
  public static final TaskQueryProperty DESCRIPTION = new TaskQueryProperty("T.DESCRIPTION_");
  public static final TaskQueryProperty PRIORITY = new TaskQueryProperty("T.PRIORITY_");
  public static final TaskQueryProperty ASSIGNEE = new TaskQueryProperty("T.ASSIGNEE_");
  public static final TaskQueryProperty PROCESS_INSTANCE_ID = new TaskQueryProperty("T.PROC_INST_ID_");
  public static final TaskQueryProperty EXECUTION_ID = new TaskQueryProperty("T.EXECUTION_ID_");

  
  private String name;

  public TaskQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static TaskQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
