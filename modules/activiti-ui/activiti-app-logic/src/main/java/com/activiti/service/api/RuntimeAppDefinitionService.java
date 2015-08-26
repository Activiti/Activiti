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
package com.activiti.service.api;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeApp;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;

import java.util.List;

public interface RuntimeAppDefinitionService {

    /**
     * @return the app definition id created by a specific user for a specific model.
     */
    Long getDefinitionIdForModelAndUser(Long modelId, User user);
    
    /**
     * @return all {@link RuntimeAppDefinition} a user has defined. The results are based on the presence
     * of {@link RuntimeApp} entities, referencing the given user.
     */
    List<RuntimeAppDefinition> getDefinitionsForUser(User user);
    
    List<RuntimeAppDeployment> getRuntimeAppDeploymentsForAppId(Long appId);
}