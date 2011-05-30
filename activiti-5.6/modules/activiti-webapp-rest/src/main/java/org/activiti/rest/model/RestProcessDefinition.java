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

package org.activiti.rest.model;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * Model used in REST-API, adding field startFormResourceKey to task.
 *
 * @author Frederik Heremans
 */
public class RestProcessDefinition extends ProcessDefinitionEntity {

  private static final long serialVersionUID = 1L;


  public RestProcessDefinition(ProcessDefinitionEntity processDefinition) {
    this.setId(processDefinition.getId());
    this.setKey(processDefinition.getKey());
    this.setName(processDefinition.getName());
    this.setVersion(processDefinition.getVersion());
    this.setDeploymentId(processDefinition.getDeploymentId());
    this.setResourceName(processDefinition.getResourceName());
    this.setDiagramResourceName(processDefinition.getDiagramResourceName());
  }

  protected String startFormResourceKey;

  protected boolean isGraphicNotationDefined;

  public boolean isGraphicNotationDefined() {
    return isGraphicNotationDefined;
  }

  public void setGraphicNotationDefined(boolean graphicNotationDefined) {
    isGraphicNotationDefined = graphicNotationDefined;
  }

  public String getStartFormResourceKey() {
    return startFormResourceKey;
  }

  public void setStartFormResourceKey(String startFormResourceKey) {
    this.startFormResourceKey = startFormResourceKey;
  }

}
