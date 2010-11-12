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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.history.handler.HistoryParseListener;
import org.activiti.engine.impl.repository.Deployer;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.repository.ResourceEntity;

/**
 * @author Tom Baeyens
 */
public class BpmnDeployer implements Deployer, ProcessEngineConfigurationAware {

  private static final Logger LOG = Logger.getLogger(BpmnDeployer.class.getName());;

  public static final String BPMN_RESOURCE_SUFFIX = "bpmn20.xml";
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  protected ExpressionManager expressionManager;
  protected BpmnParser bpmnParser;

  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.expressionManager = processEngineConfiguration.getExpressionManager();
    this.bpmnParser = new BpmnParser(expressionManager);
    int historyLevel = processEngineConfiguration.getHistoryLevel();
    if (historyLevel>=ProcessEngineConfiguration.HISTORYLEVEL_ACTIVITY) {
      bpmnParser.getParseListeners().add(new HistoryParseListener(historyLevel));
    }
  }

  public List<ProcessDefinitionEntity> deploy(DeploymentEntity deployment) {
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();

    for (String resourceName : resources.keySet()) {

      LOG.info("Processing resource " + resourceName);
      if (resourceName.endsWith(BPMN_RESOURCE_SUFFIX)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BpmnParse bpmnParse = bpmnParser
          .createParse()
          .sourceInputStream(inputStream)
          .deployment(deployment)
          .name(resourceName)
          .execute();
        
        String diagramResource = getDiagramResource(resourceName, resources);

//        if (diagramResource==null) {
//          TODO ACT-340 this is where diagram resource generation would be cool to have based on the bpmn process xml 
//        }

        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);
          processDefinition.setDiagramResourceName(diagramResource);
          processDefinitions.add(processDefinition);
        }
      }
    }
    
    return processDefinitions;
  }

  protected String getDiagramResource(String resourceName, Map<String, ResourceEntity> resources) {
    String processResourceBase = resourceName.substring(0, resourceName.length()-10);
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String processDiagramResource = processResourceBase + diagramSuffix;
      if (resources.containsKey(processDiagramResource)) {
        return processDiagramResource;
      }
    }
    
    return null;
  }
}
