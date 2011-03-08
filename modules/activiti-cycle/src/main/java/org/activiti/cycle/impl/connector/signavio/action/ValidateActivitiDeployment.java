package org.activiti.cycle.impl.connector.signavio.action;

import java.util.Map;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;


public class ValidateActivitiDeployment extends CreateTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public ValidateActivitiDeployment() {
    // TODO: remove when real labels are introduced in the GUI
    super("Validate for Activiti");
  }
  
  public String getFormResourceName() {
    // we don't need any form here
    return null;
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {    
    BpmnParse parse = createParseObject(connector, artifact);
    // parse to validate
    parse.execute();
    // That's it, now we get an exception if the file is invalid
  }
  
  public static BpmnParse createParseObject(RepositoryConnector connector, RepositoryArtifact artifact) {
    String bpmnXml = ActivitiCompliantBpmn20Provider.createBpmnXml((SignavioConnectorInterface) connector, artifact);
    
    // TODO: Okay, this needs more serious thinking where we get the engine
    // from!
    ExpressionManager expressionManager = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration().getExpressionManager();
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    Context.setProcessEngineConfiguration( ((ProcessEngineImpl)ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration());
    
    // Unfortunately the deployment id is requested while parsing, so we have to
    // set a DeploymentEntity to avoid an NPE
    DeploymentEntity deployment = new DeploymentEntity();
    deployment.setId("VALIDATION_DEPLOYMENT");
    
    // parse to validate
    BpmnParse parse = bpmnParser.createParse().deployment(deployment).sourceString(bpmnXml).name(artifact.getNodeId());
    return parse;
  }


}
