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

import com.querydsl.core.types.Predicate;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableUpdater {

    private final EntityFinder entityFinder;

    private VariableRepository variableRepository;

    @Autowired
    public VariableUpdater(EntityFinder entityFinder,
                           VariableRepository variableRepository) {
        this.entityFinder = entityFinder;
        this.variableRepository = variableRepository;
    }

    public void update(Variable updatedVariable, Predicate predicate, String notFoundMessage) {
        Variable variable = entityFinder.findOne(variableRepository,
                                            predicate,
                                            notFoundMessage);
        variable.setLastUpdatedTime(updatedVariable.getLastUpdatedTime());
        variable.setType(updatedVariable.getType());
        variable.setValue(updatedVariable.getValue());

        variableRepository.save(variable);
    }

}
