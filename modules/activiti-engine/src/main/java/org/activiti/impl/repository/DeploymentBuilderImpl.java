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
package org.activiti.impl.repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.ActivitiException;
import org.activiti.Deployment;
import org.activiti.DeploymentBuilder;
import org.activiti.impl.ProcessServiceImpl;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.util.IoUtil;


/**
 * @author Tom Baeyens
 */
public class DeploymentBuilderImpl implements DeploymentBuilder {

  private static final long serialVersionUID = 1L;

  protected ProcessServiceImpl processService;
  protected DeploymentImpl deployment = DeploymentImpl.create();

  public DeploymentBuilderImpl(ProcessServiceImpl processManager) {
    this.processService = processManager;
    deployment.resources = new HashMap<String, ByteArrayImpl>();
  }

  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ByteArrayImpl resource = new ByteArrayImpl(resourceName, bytes);
    deployment.resources.put(resourceName, resource);
    return this;
  }

  public DeploymentBuilder addClasspathResource(String resource) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(resource);
    return addInputStream(resource, inputStream);
  }

  public DeploymentBuilder addString(String resourceName, String text) {
    ByteArrayImpl resource = new ByteArrayImpl(resourceName, text.getBytes());
    deployment.resources.put(resourceName, resource);
    return this;
  }

  public DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while( entry!=null ) {
        String entryName = entry.getName();
        byte[] bytes = IoUtil.readInputStream(zipInputStream, entryName);
        ByteArrayImpl byteArray = new ByteArrayImpl(entryName, bytes);
        deployment.resources.put(entryName, byteArray);
        entry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      throw new ActivitiException("problem reading zip input stream", e);
    }
    return this;
  }
  
  public DeploymentBuilder name(String name) {
    deployment.setName(name);
    return this;
  }

  public Deployment deploy() {
    processService.deploy(deployment);
    return deployment;
  }
}
