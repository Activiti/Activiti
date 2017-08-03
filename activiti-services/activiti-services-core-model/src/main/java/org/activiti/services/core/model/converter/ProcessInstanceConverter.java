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

package org.activiti.services.core.model.converter;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceConverter implements ModelConverter<ProcessInstance, org.activiti.services.core.model.ProcessInstance> {

    private final ListConverter listConverter;

    @Autowired
    public ProcessInstanceConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public org.activiti.services.core.model.ProcessInstance from(ProcessInstance source) {
        org.activiti.services.core.model.ProcessInstance processInstance = null;
        if (source != null) {
            processInstance = new org.activiti.services.core.model.ProcessInstance(source.getId(),
                                                                                                 source.getName(),
                                                                                                 source.getDescription(),
                                                                                                 source.getProcessDefinitionId(),
                                                                                                 source.getStartUserId(),
                                                                                                 source.getStartTime(),
                                                                                                 source.getBusinessKey(),
                                                                                                 calculateStatus(source));
        }
        return processInstance;
    }

    private String calculateStatus(ProcessInstance source) {
        if (source.isSuspended()) {
            return org.activiti.services.core.model.ProcessInstance.ProcessInstanceStatus.SUSPENDED.name();
        } else if (source.isEnded()) {
            return org.activiti.services.core.model.ProcessInstance.ProcessInstanceStatus.COMPLETED.name();
        }
        return org.activiti.services.core.model.ProcessInstance.ProcessInstanceStatus.RUNNING.name();
    }

    @Override
    public List<org.activiti.services.core.model.ProcessInstance> from(List<ProcessInstance> processInstances) {
        return listConverter.from(processInstances,
                                  this);
    }
}
