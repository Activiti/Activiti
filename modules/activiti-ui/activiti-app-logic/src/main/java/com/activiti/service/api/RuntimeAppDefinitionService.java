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

import java.util.List;

import org.activiti.engine.identity.User;

import com.activiti.domain.runtime.RuntimeApp;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;

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