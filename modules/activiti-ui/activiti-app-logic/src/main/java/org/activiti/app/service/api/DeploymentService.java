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
package org.activiti.app.service.api;

import org.activiti.app.domain.editor.Model;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.springframework.transaction.annotation.Transactional;

public interface DeploymentService {

    @Transactional
    Deployment updateAppDefinition(Model appDefinition, User user);

    @Transactional
    void deleteAppDefinition(String appDefinitionId);

}