/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.runtime;

import java.util.List;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeApp;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;

public interface RuntimeAppDefinitionInternalService {

    /**
     * @return a {@link RuntimeAppDefinition} for the given id. Returns null, if no app definition
     * has been found.
     */
    RuntimeAppDefinition getRuntimeAppDefinition(Long id);

    /**
     * @return a {@link RuntimeAppDefinition} which is a deployed version of the model with the given id or {@literal null}
     * if the model currently not deployed.
     */
    RuntimeAppDefinition getRuntimeAppDefinitionForModel(Long modelId);
    
    List<RuntimeAppDeployment> getRuntimeAppDeploymentsForApp(RuntimeAppDefinition appDefinition);

    /**
     * @return a {@link RuntimeAppDefinition}, representing the deployed version of an app definition model with the given id. In case a {@link RuntimeAppDefinition} already exists
     * for the given model, only an additional {@link RuntimeApp} is created, referencing the existing definition and given user.
     */
    RuntimeAppDefinition createRuntimeAppDefinition(User user, String name, String description, Long modelId, String definition);
    
    /**
     * @return a {@link RuntimeAppDeployment}, representing the deployed version of an app definition model with the given id.
     */
    RuntimeAppDeployment createRuntimeAppDeployment(User user, RuntimeAppDefinition appDefinition, Long modelId, String definition);

    void updateRuntimeAppDefinition(RuntimeAppDefinition appDefinition);
    
    void updateRuntimeAppDeployment(RuntimeAppDeployment appDeployment);

    /**
     * @return if there's an app definition created by a specific user for a specific model.
     */
    RuntimeAppDefinition getDefinitionForModelAndUser(Long modelId, User user);

    /**
     * @return a new {@link RuntimeApp}, connecting a user with a {@link RuntimeAppDefinition}. No checks
     * are performed to see if a user has rights to ass this definition.
     */
    RuntimeApp addAppDefinitionForUser(User user, RuntimeAppDefinition rad);

    /**
     * @return true, after deleting an existing {@link RuntimeApp} for the given user and the given definition. Returns
     * false if no valid {@link RuntimeApp} existed.
     */
    boolean deleteAppDefinitionForUser(User user, RuntimeAppDefinition appDefinition);
    
    /**
     * @return true, after deleting an existing app definition. Returns
     * false if no valid app definition existed.
     */
    boolean deleteAppDefinition(RuntimeAppDefinition appDefinition);

}