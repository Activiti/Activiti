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

package org.activiti.rest.api;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.api.repository.DeploymentResourceResponse;
import org.activiti.rest.api.repository.DeploymentResponse;


/**
 * Interface that describes a factory class that is capable of creating response objects
 * that can be used to return as a result (or part of a result) from a REST-service call.
 * <br>
 * All methods require a {@link SecuredResource}, which is used to create URL in right context and provides
 * access to shared logic and the application.
 * 
 * @author Frederik Heremans
 */
public interface RestResponseFactory {

  DeploymentResponse createDeploymentResponse(SecuredResource resourceContext, Deployment deployment);
  
  DeploymentResourceResponse createDeploymentResourceResponse(SecuredResource resourceContext, String deploymentId, String resourceId);
  
}
