package org.activiti.engine.test.api.repository;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by P3700487 on 2/19/2015.
 */
public class LaneExtensionTest {

  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();

  @Test
  @Deployment
  public void testLaneExtensionElement() {
    ProcessDefinition processDefinition = activitiRule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("swimlane-extension").singleResult();
    BpmnModel bpmnModel = activitiRule.getRepositoryService().getBpmnModel(processDefinition.getId());
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
    System.out.println(new String(xml));
    Process bpmnProcess = bpmnModel.getMainProcess();
    for (Lane l : bpmnProcess.getLanes()) {
      Map<String, List<ExtensionElement>> extensions = l.getExtensionElements();
      Assert.assertTrue(extensions.size() > 0);
    }
  }

}
