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
package org.activiti.dmn.api;

import java.io.InputStream;
import java.util.List;

import org.activiti.dmn.model.DmnDefinition;

/**
 * Service providing access to the repository of process definitions and deployments.
 *
 * @author Tijs Rademakers
 * @author Yvo Swillens
 */
public interface DmnRepositoryService {

  DmnDeploymentBuilder createDeployment();

  void deleteDeployment(String deploymentId);

  DmnDecisionTableQuery createDecisionTableQuery();

  NativeDecisionTableQuery createNativeDecisionTableQuery();

  void setDeploymentCategory(String deploymentId, String category);

  void setDeploymentTenantId(String deploymentId, String newTenantId);

  List<String> getDeploymentResourceNames(String deploymentId);

  InputStream getResourceAsStream(String deploymentId, String resourceName);

  DmnDeploymentQuery createDeploymentQuery();

  NativeDmnDeploymentQuery createNativeDeploymentQuery();

  DmnDecisionTable getDecisionTable(String decisionTableId);

  InputStream getDmnResource(String decisionTableId);

  void setDecisionTableCategory(String decisionTableId, String category);

  DmnDefinition getDmnDefinition(String decisionTableId);
}
