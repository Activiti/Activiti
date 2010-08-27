package org.activiti.cycle.impl.connector.signavio.action;

import java.util.Map;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.el.ExpressionManager;


public class ValidateActivitiDeployment extends CreateTechnicalBpmnXmlAction {

  @Override
  public void execute(Map<String, Object> parameter) throws Exception {    
    // TODO: Okay, this needs more serious thiniking where we get the engine
    // from!
    ProcessEngineConfiguration processEngineConfiguration = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine())
            .getProcessEngineConfiguration();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    
    String sourceJson = getBpmn20Json();
    String transformedJson = applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20(transformedJson); 
    
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    // parse to validate
    bpmnParser
      .createParse().sourceString(bpmnXml).name(getArtifact().getId()).execute();    
    // That's it, now we get an exception is the file is invalid
  }

  @Override
  public String getFormAsHtml() {
    // we don't need any parameters, so we don't need a form
    // TODO: MAybe this should be an own superclass?
    return null;
  }

  @Override
  public String getLabel() {
    return "Validate BPMN 2.0 for Activiti";
  }

}
