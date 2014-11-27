package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.junit.Test;

public class SubProcessConverterAutoLayoutTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }
  
  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    
    // Add DI information to bpmn model
    BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
    bpmnAutoLayout.execute();
    
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }
  
  protected String getResource() {
    return "subprocessmodel_autolayout.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());
    
    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("userTask1", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertTrue(userTask.getCandidateUsers().size() == 1);
    assertTrue(userTask.getCandidateGroups().size() == 1);
    
    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertTrue(subProcess.getFlowElements().size() == 6);
    
    List<ValuedDataObject> dataObjects = ((SubProcess)flowElement).getDataObjects();
    assertTrue(dataObjects.size() == 1);

    ValuedDataObject dataObj = dataObjects.get(0);
    assertEquals("SubTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof String);
    assertEquals("Testing", dataObj.getValue());
  }
}
