/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.query.events.handlers;

import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessVariableCreatedHandler {

    private final EntityFinder entityFinder;

    private final ProcessInstanceRepository processInstanceRepository;

    private final VariableRepository variableRepository;

    @Autowired
    public ProcessVariableCreatedHandler(EntityFinder entityFinder,
                                         ProcessInstanceRepository processInstanceRepository,
                                         VariableRepository variableRepository) {
        this.entityFinder = entityFinder;
        this.processInstanceRepository = processInstanceRepository;
        this.variableRepository = variableRepository;
    }

    public void handle(Variable variable) {
        String processInstanceId = variable.getProcessInstanceId();
        ProcessInstance processInstance = entityFinder.findById(processInstanceRepository,
                                                                Long.parseLong(processInstanceId),
                                                                "Unable to find process instance for the given id: " + processInstanceId);
        variableRepository.save(variable);

        processInstance.addVariable(variable);
        processInstanceRepository.save(processInstance);
    }
}
