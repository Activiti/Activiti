package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOParameter;
import org.junit.Test;

public class CallActivityConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "test.callactivitymodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("callactivity");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof CallActivity);
    CallActivity callActivity = (CallActivity) flowElement;
    assertEquals("callactivity", callActivity.getId());
    assertEquals("Call activity", callActivity.getName());
    
    assertEquals("processId", callActivity.getCalledElement());
    
    List<IOParameter> parameters = callActivity.getInParameters();
    assertEquals(2, parameters.size());
    IOParameter parameter = parameters.get(0);
    assertEquals("test", parameter.getSource());
    assertEquals("test", parameter.getTarget());
    parameter = parameters.get(1);
    assertEquals("${test}", parameter.getSourceExpression());
    assertEquals("test", parameter.getTarget());
    
    parameters = callActivity.getOutParameters();
    assertEquals(1, parameters.size());
    parameter = parameters.get(0);
    assertEquals("test", parameter.getSource());
    assertEquals("test", parameter.getTarget());
  }
}
