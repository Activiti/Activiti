package org.activiti.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.Message;
import org.activiti.engine.impl.bpmn.MessageInstance;
import org.activiti.engine.runtime.ProcessInstance;


public class WebServiceUELTest extends AbstractWebServiceTaskTest {

  public void testWebServiceInvocationWithDataFlowUEL() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    Message message = null;
    MessageInstance processInput = message.createInstance();
    processInput.setFieldValue("prefix", "The counter has the value ");
    processInput.setFieldValue("suffix", ". Good news");
    variables.put("dataInputOfProcess", processInput);
    
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(
            "webServiceInvocationWithDataFlowUEL",
            variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);
    
    MessageInstance response = (MessageInstance) processEngine.getRuntimeService().getVariable(instance.getId(), "dataOutputOfProcess");
    
    assertEquals("The counter has the value -1. Good news", response.getFieldValue("prettyPrint"));
  }
}
