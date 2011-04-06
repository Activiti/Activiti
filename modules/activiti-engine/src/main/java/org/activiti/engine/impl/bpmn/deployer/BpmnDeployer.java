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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.mgr.ProcessDefinitionManager;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.impl.runtime.TimerEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.runtime.Job;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnDeployer implements Deployer {

  private static final Logger LOG = Logger.getLogger(BpmnDeployer.class.getName());;

  public static final String BPMN_RESOURCE_SUFFIX = "bpmn20.xml";
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  protected ExpressionManager expressionManager;
  protected BpmnParser bpmnParser;
  protected IdGenerator idGenerator;

  public void deploy(DeploymentEntity deployment) {
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
          .name(resourceName);
        
        if (!deployment.isValidatingSchema()) {
          bpmnParse.setSchemaResource(null);
        }
        
        bpmnParse.execute();
        
        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);
          
          String diagramResourceName = getDiagramResourceForProcess(resourceName, processDefinition.getKey(), resources);
          if (diagramResourceName==null && processDefinition.isGraphicalNotationDefined()) {
            try {
              byte[] diagramBytes = IoUtil.readInputStream(ProcessDiagramGenerator.generatePngDiagram(processDefinition), null);
              diagramResourceName = getProcessImageResourceName(resourceName, processDefinition.getKey(), "png");
              createResource(diagramResourceName, diagramBytes, deployment);
            } catch (Exception e) { // if anything goes wrong, we don't store the image (the process will still be executable).
              LOG.log(Level.WARNING, "Error while generating process diagram, image will not be stored in repository", e);
            }
          } 
          
          processDefinition.setDiagramResourceName(diagramResourceName);
          processDefinitions.add(processDefinition);
        }
      }
    }
    
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (deployment.isNew()) {
        int processDefinitionVersion;

        ProcessDefinitionEntity latestProcessDefinition = processDefinitionManager.findLatestProcessDefinitionByKey(processDefinition.getKey());
        if (latestProcessDefinition != null) {
          processDefinitionVersion = latestProcessDefinition.getVersion() + 1;
        } else {
          processDefinitionVersion = 1;
        }

        processDefinition.setVersion(processDefinitionVersion);
        processDefinition.setDeploymentId(deployment.getId());

        String processDefinitionId = processDefinition.getKey() 
          + ":" + processDefinition.getVersion()
          + ":" + idGenerator.getNextId(); // ACT-505
          
        // ACT-115: maximum id length is 64 charcaters
        if (processDefinitionId.length() > 64) {
          throw new ActivitiException("Invalid process definition id: '" + processDefinitionId + "': id can be maximum 64 characters");
        }
        processDefinition.setId(processDefinitionId);

        removeObsoleteTimers(processDefinition);
        addTimerDeclarations(processDefinition);

        dbSqlSession.insert(processDefinition);
        deploymentCache.addProcessDefinition(processDefinition);
        
      } else {
        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        ProcessDefinitionEntity persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        deploymentCache.addProcessDefinition(processDefinition);
      }

      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .addProcessDefinition(processDefinition);
    }
  }

  @SuppressWarnings("unchecked")
  private void addTimerDeclarations(ProcessDefinitionEntity processDefinition) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.prepareTimerEntity(null);
        Context
          .getCommandContext()
          .getJobManager()
          .schedule(timer);
      }
    }
  }

  private void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<Job> jobsToDelete = Context
      .getCommandContext()
      .getJobManager()
      .findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    
    for (Job job :jobsToDelete) {
        new DeleteJobsCmd(job.getId()).execute(Context.getCommandContext());
    }
  }


  /**
   * Returns the default name of the image resource for a certain process.
   * 
   * It will first look for an image resource which matches the process
   * specifically, before resorting to an image resource which matches the BPMN
   * 2.0 xml file resource.
   * 
   * Example: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing only one process with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   * 
   * Example 2: if the deployment contains a BPMN 2.0 xml resource called 
   * 'abc.bpmn20.xml' containing three processes (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png' 
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * processes will have the same image: abc.png.
   * 
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForProcess(String bpmnFileResource, String processKey, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String diagramForBpmnFileResource = getBpmnFileImageResourceName(bpmnFileResource, diagramSuffix);
      String processDiagramResource = getProcessImageResourceName(bpmnFileResource, processKey, diagramSuffix);
      if (resources.containsKey(processDiagramResource)) {
        return processDiagramResource;
      } else if (resources.containsKey(diagramForBpmnFileResource)) {
        return diagramForBpmnFileResource;
      }
    }
    return null;
  }
  
  protected String getBpmnFileImageResourceName(String bpmnFileResource, String diagramSuffix) {
    String bpmnFileResourceBase = bpmnFileResource.substring(0, bpmnFileResource.length()-10); // minus 10 to remove 'bpmn20.xml'
    return bpmnFileResourceBase + diagramSuffix;
  }
  
  protected String getProcessImageResourceName(String bpmnFileResource, String processKey, String diagramSuffix) {
    String bpmnFileResourceBase = bpmnFileResource.substring(0, bpmnFileResource.length()-10); // minus 10 to remove 'bpmn20.xml'
    return bpmnFileResourceBase + processKey + "." + diagramSuffix;
  }

  protected void createResource(String name, byte[] bytes, DeploymentEntity deploymentEntity) {
    ResourceEntity resource = new ResourceEntity();
    resource.setName(name);
    resource.setBytes(bytes);
    resource.setDeploymentId(deploymentEntity.getId());
    
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(resource);
  }
  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
  
  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }
  
  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }
  
  public void setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
}
