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
package org.activiti.engine.impl.cmd;

import java.util.Arrays;
import java.util.Map;

import org.activiti.engine.Deployment;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.impl.util.ClockUtil;

/**
 * @author Tom Baeyens
 */
public class DeployCmd<T> implements Command<Deployment> {

  protected DeploymentBuilderImpl deploymentBuilder;

  public DeployCmd(DeploymentBuilderImpl deploymentBuilder) {
    this.deploymentBuilder = deploymentBuilder;
  }

  public Deployment execute(CommandContext commandContext) {
    DeploymentEntity deployment = deploymentBuilder.getDeployment();
    RepositorySession repositorySession = commandContext.getRepositorySession();
    
    deployment.setDeploymentTime(ClockUtil.getCurrentTime());

    if ( deploymentBuilder.isDuplicateFilterEnabled() ) {
      DeploymentEntity existingDeployment = repositorySession.findLatestDeploymentByName(deployment.getName());
      if ( (existingDeployment!=null)
           && !deploymentsDiffer(deployment, existingDeployment)) {
        return existingDeployment;
      }
    }

    repositorySession.deployNew(deployment);
    
    return deployment;
  }

  protected boolean deploymentsDiffer(DeploymentEntity deployment, DeploymentEntity saved) {
    Map<String, ResourceEntity> resources = deployment.getResources();
    Map<String, ResourceEntity> savedResources = saved.getResources();
    if (!resources.keySet().equals(savedResources.keySet())) {
      return false;
    }
    for (String resourceName: resources.keySet()) {
      ResourceEntity resource = resources.get(resourceName);
      byte[] bytes = resource.getBytes();
      ResourceEntity savedResource = savedResources.get(resourceName);
      byte[] savedBytes = savedResource.getBytes();
      if (!Arrays.equals(bytes, savedBytes)) {
        return false;
      }
    }
    return true;
  }

//  private boolean resourcesDiffer(ByteArrayEntity value, ByteArrayEntity other) {
//    if (value == null && other == null) {
//      return false;
//    }
//    String bytes = createKey(value.getBytes());
//    String savedBytes = other == null ? null : createKey(other.getBytes());
//    return !bytes.equals(savedBytes);
//  }
//
//  private String createKey(byte[] bytes) {
//    if (bytes == null) {
//      return "";
//    }
//    MessageDigest digest;
//    try {
//      digest = MessageDigest.getInstance("MD5");
//    } catch (NoSuchAlgorithmException e) {
//      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
//    }
//    bytes = digest.digest(bytes);
//    return String.format("%032x", new BigInteger(1, bytes));
//  }
}
