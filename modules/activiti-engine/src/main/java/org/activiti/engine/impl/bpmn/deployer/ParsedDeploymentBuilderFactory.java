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
package org.activiti.engine.impl.bpmn.deployer;

import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;

public class ParsedDeploymentBuilderFactory {

  protected BpmnParser bpmnParser;

  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }

  public void setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
  }

  public ParsedDeploymentBuilder getBuilderForDeployment(DeploymentEntity deployment) {
    return getBuilderForDeploymentAndSettings(deployment, null);
  }

  public ParsedDeploymentBuilder getBuilderForDeploymentAndSettings(DeploymentEntity deployment,
      Map<String, Object> deploymentSettings) {
    return new ParsedDeploymentBuilder(deployment, bpmnParser, deploymentSettings);
  }
  
}