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
package org.activiti.engine.impl.persistence.deploy;

import java.io.Serializable;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * @author Joram Barrez
 */
public class ProcessDefinitionCacheEntry implements Serializable {

    private static final long serialVersionUID = 6833801933658529070L;

    protected ProcessDefinitionEntity processDefinitionEntity;
    protected BpmnModel bpmnModel;
    protected Process process;

    public ProcessDefinitionCacheEntry(ProcessDefinitionEntity processDefinitionEntity, BpmnModel bpmnModel, Process process) {
        this.processDefinitionEntity = processDefinitionEntity;
        this.bpmnModel = bpmnModel;
        this.process = process;
    }

    public ProcessDefinitionEntity getProcessDefinitionEntity() {
        return processDefinitionEntity;
    }

    public void setProcessDefinitionEntity(ProcessDefinitionEntity processDefinitionEntity) {
        this.processDefinitionEntity = processDefinitionEntity;
    }
    
    public BpmnModel getBpmnModel() {
		return bpmnModel;
	}

	public void setBpmnModel(BpmnModel bpmnModel) {
		this.bpmnModel = bpmnModel;
	}

	public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

}
