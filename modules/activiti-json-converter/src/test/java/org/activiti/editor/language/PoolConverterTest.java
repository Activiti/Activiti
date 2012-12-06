package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.junit.Test;

public class PoolConverterTest extends AbstractConverterTest {

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
    return "test.poolmodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    assertEquals(1, model.getPools().size());
    Process process = model.getProcess("pool");
    assertEquals(2, process.getLanes().size());
    Lane lane = process.getLanes().get(0);
    assertEquals("lane1", lane.getId());
    assertEquals(1, lane.getFlowReferences().size());
    assertTrue(lane.getFlowReferences().contains("usertask2"));
    lane = process.getLanes().get(1);
    assertEquals("lane2", lane.getId());
    assertEquals(3, lane.getFlowReferences().size());
    assertTrue(lane.getFlowReferences().contains("startevent"));
    assertTrue(lane.getFlowReferences().contains("usertask1"));
    assertTrue(lane.getFlowReferences().contains("endevent"));
    
    FlowElement usertask = process.getFlowElement("usertask1");
    assertNotNull(usertask);
    assertEquals("test2", usertask.getName());
    
    usertask = process.getFlowElement("usertask2");
    assertNotNull(usertask);
    assertEquals("test", usertask.getName());
  }
}
