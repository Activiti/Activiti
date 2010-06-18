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

import org.activiti.Page;
import org.activiti.ProcessInstance;
import org.activiti.ProcessInstanceQuery;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.query.AbstractQuery;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Joram Barrez
 */
public class ProcessInstanceQueryImpl extends AbstractQuery<ProcessInstance> implements ProcessInstanceQuery {

  protected String processDefinitionKey;
  
  public ProcessInstanceQueryImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }
  
  public ProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  protected long executeCount(TransactionContext transactionContext) {
    return transactionContext
      .getTransactionalObject(PersistenceSession.class)
      .dynamicFindProcessInstanceCount(createParamMap());
  }

  protected List<ProcessInstance> executeList(TransactionContext transactionContext, Page page) {
    return transactionContext
      .getTransactionalObject(PersistenceSession.class)
      .dynamicFindProcessInstances(createParamMap());
  }
  
  protected Map<String, Object> createParamMap() {
    Map<String, Object> params = new HashMap<String, Object>();
    if (processDefinitionKey != null) {
      params.put("processDefinitionKey", processDefinitionKey);
    }
    return params;
  }

}
