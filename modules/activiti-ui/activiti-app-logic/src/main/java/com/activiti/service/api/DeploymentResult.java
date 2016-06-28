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
package com.activiti.service.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.tuple.Pair;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RuntimeAppDeployment;

/**
 * @author jbarrez
 */
public class DeploymentResult {
	
	private List<AbstractModel> processModels = new ArrayList<AbstractModel>();
	
	private RuntimeAppDeployment runtimeAppDeployment;
	
	private Deployment deployment;
	
	private Map<Long, Pair<BpmnModel, ArrayList<ProcessDefinition>>> processModelMapping = new HashMap<Long, Pair<BpmnModel,ArrayList<ProcessDefinition>>>(); 
	
	private Map<Long, Form> formModelMapping = new HashMap<Long, Form>();
	
	public void addModelMapping(AbstractModel processModel, BpmnModel bpmnModel) {
		processModels.add(processModel);
		processModelMapping.put(processModel.getId(), Pair.of(bpmnModel, new ArrayList<ProcessDefinition>())); // Right side will be added later
	}

	/**
	 * Should be called AFTER all {@link AbstractModel}s and {@link BpmnModel}s are added!
	 */
	public void addProcessDefinition(ProcessDefinition processDefinition) {
		for (Long modelId : processModelMapping.keySet()) {
			Pair<BpmnModel, ArrayList<ProcessDefinition>> pair = processModelMapping.get(modelId);
			List<String> processDefinitionKeys = getProcessDefinitionKeys(pair.getLeft());
			if (processDefinitionKeys.contains(processDefinition.getKey())) {
				pair.getRight().add(processDefinition);
				return;
			}
		}
	}
	
	public List<ProcessDefinition> getProcessDefinitionsForProcessModel(Long modelId) {
		return processModelMapping.get(modelId).getRight();
	}
	
	private List<String> getProcessDefinitionKeys(BpmnModel bpmnModel) {
		List<String> processDefinitionKeys = new ArrayList<String>();
		for (org.activiti.bpmn.model.Process process : bpmnModel.getProcesses()) {
			processDefinitionKeys.add(process.getId());
		}
		return processDefinitionKeys;
	}
	
	public Form getRuntimeFormForFormModel(Long formModelId) {
		return formModelMapping.get(formModelId);
	}
	
	public void addFormModelMapping(Long formModelId, Form form) {
		formModelMapping.put(formModelId, form);
	}
	
	public RuntimeAppDeployment getRuntimeAppDeployment() {
		return runtimeAppDeployment;
	}
	
	public Deployment getDeployment() {
		return deployment;
	}

	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}

	public void setRuntimeAppDeployment(RuntimeAppDeployment runtimeAppDeployment) {
		this.runtimeAppDeployment = runtimeAppDeployment;
	}

	public List<AbstractModel> getProcessModels() {
		return processModels;
	}

	public void setProcessModels(List<AbstractModel> processModels) {
		this.processModels = processModels;
	}
	
}
