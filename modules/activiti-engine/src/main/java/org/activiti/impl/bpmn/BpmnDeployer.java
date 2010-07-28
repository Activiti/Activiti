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
package org.activiti.impl.bpmn;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.impl.bpmn.parser.BpmnParse;
import org.activiti.impl.bpmn.parser.BpmnParser;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.calendar.BusinessCalendarManager;
import org.activiti.impl.definition.ProcessDefinitionDbImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.el.ExpressionManager;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.scripting.ScriptingEngines;

/**
 * @author Tom Baeyens
 */
public class BpmnDeployer implements Deployer {

  private static final Logger LOG = Logger.getLogger(BpmnDeployer.class.getName());;

  public static final String BPMN_RESOURCE_SUFFIX = "bpmn20.xml";

  private final ExpressionManager expressionManager;

  private final ScriptingEngines scriptingEngines;

  private final BusinessCalendarManager businessCalendarManager;

  public BpmnDeployer(ExpressionManager expressionManager, ScriptingEngines scriptingEngines, BusinessCalendarManager businessCalendarManager) {
    this.expressionManager = expressionManager;
    this.scriptingEngines = scriptingEngines;
    this.businessCalendarManager = businessCalendarManager;
  }

  public void deploy(DeploymentEntity deployment, boolean isNew, RepositorySession repositorySession) {

    Map<String, ResourceEntity> resources = deployment.getResources();

    for (String resourceName : resources.keySet()) {

      LOG.info("Processing resource " + resourceName);
      if (resourceName.endsWith(BPMN_RESOURCE_SUFFIX)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BpmnParse bpmnParse = new BpmnParser(expressionManager, scriptingEngines, businessCalendarManager)
          .createParse()
          .processDefinitionClass(ProcessDefinitionDbImpl.class)
          .sourceInputStream(inputStream)
          .execute();

        for (ProcessDefinitionImpl processDefinition : bpmnParse.getProcessDefinitions()) {
          processDefinition.setDeployment(deployment);
          processDefinition.setNew(isNew);
          repositorySession.insertProcessDefinition(processDefinition);
        }
      }
    }
  }

  public void delete(DeploymentEntity deployment) {
    // TODO if this class inserts the process definitions, then it should also be responsible for deleting them
  }
}
