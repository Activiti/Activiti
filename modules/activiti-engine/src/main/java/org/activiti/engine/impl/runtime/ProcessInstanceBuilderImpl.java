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
package org.activiti.engine.impl.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;

/**
 * @author Bassam Al-Sarori
 *
 */
public class ProcessInstanceBuilderImpl implements ProcessInstanceBuilder {

	protected RuntimeServiceImpl runtimeService;

	protected String processDefinitionId;
	protected String processDefinitionKey;
	protected String processInstanceName;
	protected String businessKey;
	protected String tenantId;
	protected Map<String, Object> variables = new HashMap<String, Object>();

	public ProcessInstanceBuilderImpl(RuntimeServiceImpl runtimeService){
		this.runtimeService = runtimeService;
	}

	public ProcessInstanceBuilder processDefinitionId(String processDefinitionId) {
	  this.processDefinitionId = processDefinitionId;
		return this;
	}

	public ProcessInstanceBuilder processDefinitionKey(String processDefinitionKey) {
	  this.processDefinitionKey = processDefinitionKey;
    return this;
	}

	public ProcessInstanceBuilder processInstanceName(String processInstanceName) {
	  this.processInstanceName = processInstanceName;
    return this;
	}

	public ProcessInstanceBuilder businessKey(String businessKey) {
	  this.businessKey = businessKey;
    return this;
	}

	public ProcessInstanceBuilder tenantId(String tenantId) {
	  this.tenantId = tenantId;
    return this;
	}

	public ProcessInstanceBuilder addVariable(String variableName, Object value) {
	  this.variables.put(variableName, value);
    return this;
	}

	public ProcessInstance start() {
	  if (processDefinitionId == null && processDefinitionKey == null) {
	    throw new ActivitiIllegalArgumentException("processDefinitionKey and processDefinitionId are null");
	  }
		return runtimeService.startProcessInstance(this);
	}

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessInstanceName() {
    return processInstanceName;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

}
