package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.impl.ActivitiCycleDbAwareTest;
import org.activiti.cycle.impl.CycleTestUtils;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.engine.repository.Deployment;

/**
 * Tests whether the {@link ActivitiCompliantBpmn20Provider} produces
 * activiti-compliant bpmn.
 * 
 * @author daniel.meyer@camunda.com
 */
public class ActivitiCompliantBpmnProviderTest extends ActivitiCycleDbAwareTest {

  public void testExtractPool() throws Exception {

    SignavioConnectorInterface connector = CycleComponentFactory.getCycleComponentInstance(SignavioConnector.class);
    connector.startConfiguration();
    connector.addConfigurationEntry(SignavioConnector.CONFIG_KEY_LOGIN_REQUIRED, Boolean.FALSE);
    connector.configurationFinished();

    String json = CycleTestUtils.loadResourceAsString("extractPoolTest.json", getClass());
    String transformedJson = ActivitiCompliantBpmn20Provider.transformJson(json);
    String bpmnXml = ActivitiCompliantBpmn20Provider.transformToBpmn20(connector, transformedJson, "extractPoolTest");        
    bpmnXml = ActivitiCompliantBpmn20Provider.adjustBpmndi(bpmnXml);
    
    // asserts that the transformed bpmn can be parsed by activiti:
    Deployment deployment = repositoryService.createDeployment().addString("extractPoolTest.bpmn20.xml", bpmnXml).deploy();       

    repositoryService.deleteDeployment(deployment.getId());
  } 
 
}
