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
package org.activiti5.engine.impl.compatibility;

import java.util.Map;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti5.engine.ProcessEngine;

/**
 * @author Joram Barrez
 */
public class Activiti5CompatibilityHandlerImpl implements Activiti5CompatibilityHandler {
	
	protected ProcessEngine processEngine;
	
	@Override
	public ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, 
			Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName) {
		
		return getProcessEngine().getRuntimeService().startProcessInstanceByKey(processDefinitionKey);
		
	}
	
	protected ProcessEngine getProcessEngine() {
		if (processEngine == null) {
			synchronized (this) {
	            if (processEngine == null) {
	            	processEngine = ProcessEngineFactory.buildProcessEngine(Context.getProcessEngineConfiguration());
	            }
            }
		}
		return processEngine;
	}

}
