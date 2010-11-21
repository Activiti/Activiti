package org.activiti.cycle.impl.connector.signavio.action;

import java.util.Map;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.repository.DeploymentEntity;


public class ValidateActivitiDeployment extends CreateTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public ValidateActivitiDeployment() {
    // TODO: remove when real labels are introduced in the GUI
    super("Validate for Activiti");
  }
  
  public String getFormResourceName() {
    // we donn't need any form here
    return null;
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {    
    // TODO: Okay, this needs more serious thiniking where we get the engine
    // from!
    ProcessEngineConfiguration processEngineConfiguration = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine())
            .getProcessEngineConfiguration();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    
    String bpmnXml = ActivitiCompliantBpmn20Provider.createBpmnXml((SignavioConnector) connector, artifact); 
    
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    
    // Unfortunately the deployment id is requested while parsing, so we have to
    // set a DeploymentEntity to avoid a NPE
    DeploymentEntity deployment = new DeploymentEntity();
    deployment.setId("VALIDATION_DEPLOYMENT");
    
    // parse to validate
    bpmnParser.createParse().deployment(deployment).sourceString(bpmnXml).name(artifact.getNodeId()).execute();    
    // That's it, now we get an exception is the file is invalid
  }


}
