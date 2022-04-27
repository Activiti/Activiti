/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Deployment;


public class GetDeploymentResourceCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String resourceName;

  public GetDeploymentResourceCmd(String deploymentId, String resourceName) {
    this.deploymentId = deploymentId;
    this.resourceName = resourceName;
  }

  public InputStream execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiIllegalArgumentException("deploymentId is null");
    }
    if (resourceName == null) {
      throw new ActivitiIllegalArgumentException("resourceName is null");
    }

    ResourceEntity resource = commandContext.getResourceEntityManager().findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);
    if (resource == null) {
      if (commandContext.getDeploymentEntityManager().findById(deploymentId) == null) {
        throw new ActivitiObjectNotFoundException("deployment does not exist: " + deploymentId, Deployment.class);
      } else {
        throw new ActivitiObjectNotFoundException("no resource found with name '" + resourceName + "' in deployment '" + deploymentId + "'", InputStream.class);
      }
    }
    return new ByteArrayInputStream(resource.getBytes());
  }

}
