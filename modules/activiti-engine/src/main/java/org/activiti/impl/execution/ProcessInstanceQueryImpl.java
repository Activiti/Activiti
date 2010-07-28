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
package org.activiti.impl.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.Page;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.ProcessInstanceQuery;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.query.AbstractListQuery;


/**
 * @author Joram Barrez
 */
public class ProcessInstanceQueryImpl extends AbstractListQuery<ProcessInstance> implements ProcessInstanceQuery {

  protected String processDefinitionKey;
  
  protected CommandExecutor commandExecutor;
  
  public ProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public ProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  protected long executeCount(CommandContext commandContext) {
    return commandContext
      .getPersistenceSession()
      .findProcessInstanceCountByDynamicCriteria(createParamMap());
  }

  protected List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
    return commandContext
      .getPersistenceSession()
      .findProcessInstancesByDynamicCriteria(createParamMap());
  }
  
  protected Map<String, Object> createParamMap() {
    Map<String, Object> params = new HashMap<String, Object>();
    if (processDefinitionKey != null) {
      params.put("processDefinitionKey", processDefinitionKey);
    }
    return params;
  }

}
