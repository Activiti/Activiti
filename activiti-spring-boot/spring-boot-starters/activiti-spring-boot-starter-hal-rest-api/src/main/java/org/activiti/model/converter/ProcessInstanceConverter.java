/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.model.converter;

import java.util.List;

import org.activiti.client.model.ProcessInstanceStatus;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceConverter implements ModelConverter<ProcessInstance, org.activiti.client.model.ProcessInstance> {

    private final ListConverter listConverter;

    @Autowired
    public ProcessInstanceConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public org.activiti.client.model.ProcessInstance from(ProcessInstance processInstance) {
        org.activiti.client.model.ProcessInstance clientObject = new org.activiti.client.model.ProcessInstance();
        clientObject.setId(processInstance.getId());
        clientObject.setName(processInstance.getName());
        clientObject.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        clientObject.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        clientObject.setActivityId(processInstance.getActivityId());
        if (processInstance.isSuspended()){
            clientObject.setStatus(ProcessInstanceStatus.SUSPENDED);
        } else {
            clientObject.setStatus(ProcessInstanceStatus.ACTIVE);
        }
        clientObject.setBusinessKey(processInstance.getBusinessKey());
        return clientObject;
    }

    @Override
    public List<org.activiti.client.model.ProcessInstance> from(List<ProcessInstance> processInstances) {
        return listConverter.from(processInstances, this);
    }

}
