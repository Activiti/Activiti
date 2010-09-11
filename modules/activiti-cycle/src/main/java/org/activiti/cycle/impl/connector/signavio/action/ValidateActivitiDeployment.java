package org.activiti.cycle.impl.connector.signavio.action;

import java.util.Map;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.el.ExpressionManager;


public class ValidateActivitiDeployment extends CreateTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public ValidateActivitiDeployment() {
    // TODO: remove when real labels are introduced in the GUI
    super("Validate for Activiti");
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {    
    // TODO: Okay, this needs more serious thiniking where we get the engine
    // from!
    ProcessEngineConfiguration processEngineConfiguration = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine())
            .getProcessEngineConfiguration();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    
    String sourceJson = getBpmn20Json((SignavioConnector) connector, artifact);
    String transformedJson = applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20((SignavioConnector) connector, transformedJson); 
    
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    // parse to validate
    bpmnParser.createParse().sourceString(bpmnXml).name(artifact.getId()).execute();    
    // That's it, now we get an exception is the file is invalid
  }


}
