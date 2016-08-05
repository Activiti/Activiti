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
package org.activiti.form.api;

import java.io.InputStream;

import org.activiti.form.model.FormDefinition;

/**
 * Builder for creating new deployments.
 * 
 * A builder instance can be obtained through {@link org.activiti.form.engine.FormRepositoryService#createDeployment()}.
 * 
 * Multiple resources can be added to one deployment before calling the {@link #deploy()} operation.
 * 
 * After deploying, no more changes can be made to the returned deployment and the builder instance can be disposed.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public interface FormDeploymentBuilder {

  FormDeploymentBuilder addInputStream(String resourceName, InputStream inputStream);

  FormDeploymentBuilder addClasspathResource(String resource);

  FormDeploymentBuilder addString(String resourceName, String text);
  
  FormDeploymentBuilder addFormBytes(String resourceName, byte[] formBytes);

  FormDeploymentBuilder addFormDefinition(String resourceName, FormDefinition formDefinition);

  /**
   * Gives the deployment the given name.
   */
  FormDeploymentBuilder name(String name);

  /**
   * Gives the deployment the given category.
   */
  FormDeploymentBuilder category(String category);

  /**
   * Gives the deployment the given tenant id.
   */
  FormDeploymentBuilder tenantId(String tenantId);
  
  /**
   * Gives the deployment the given parent deployment id.
   */
  FormDeploymentBuilder parentDeploymentId(String parentDeploymentId);

  /**
   * Deploys all provided sources to the Activiti engine.
   */
  FormDeployment deploy();

}
