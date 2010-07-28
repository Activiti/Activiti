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
package org.activiti.engine;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * builder for creating new deployments.
 * 
 * A builder instance can be obtained through {@link org.activiti.engine.ProcessService#createDeployment()}.
 * 
 * Multiple resources can be added to one deployment before calling the {@link #deploy()}
 * operation.
 * 
 * After deploying, no more changes can be made to the returned deployment
 * and the builder instance can be disposed.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DeploymentBuilder {
  
  DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
  DeploymentBuilder addClasspathResource(String resource);
  DeploymentBuilder addString(String resourceName, String text);
  DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);
  
  DeploymentBuilder name(String name);

  Deployment deploy();
}
