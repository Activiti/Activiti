package org.activiti.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.FieldBaseStructureInstance;
import org.activiti.engine.impl.bpmn.IOSpecification;
import org.activiti.engine.impl.bpmn.ItemDefinition;
import org.activiti.engine.impl.bpmn.ItemInstance;
import org.activiti.engine.impl.bpmn.MessageInstance;
import org.activiti.engine.impl.bpmn.SimpleStructureDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;


public class WebServiceUELTest extends AbstractWebServiceTaskTest {

  public void testWebServiceInvocationWithDataFlowUEL() throws Exception {
    //TODO replace structure creation with a query to the repository
    SimpleStructureDefinition simpleStructureDefinition = new SimpleStructureDefinition("structure-id");
    simpleStructureDefinition.setFieldName(0, "prefix", String.class);
    simpleStructureDefinition.setFieldName(1, "suffix", String.class);
    ItemDefinition itemDefinition = new ItemDefinition("definition-id", simpleStructureDefinition);
    
    ItemInstance itemInstance = itemDefinition.createInstance();
    FieldBaseStructureInstance structureInstance = (FieldBaseStructureInstance) itemInstance.getStructureInstance();
    structureInstance.setFieldValue("prefix", "The counter has the value ");
    structureInstance.setFieldValue("suffix", ". Good news");
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dataInputOfProcess", itemInstance);
    
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(
            "webServiceInvocationWithDataFlowUEL",
            variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);
    
    MessageInstance response = (MessageInstance) processEngine.getRuntimeService().getVariable(instance.getId(), "dataOutputOfProcess");
    
    structureInstance = (FieldBaseStructureInstance) response.getStructureInstance();
    assertEquals("The counter has the value -1. Good news", structureInstance.getFieldValue("prettyPrint"));
  }
}
