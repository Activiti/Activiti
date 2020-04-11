package org.activiti.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class HistoricProcessInstanceQueryVersionTest extends PluggableActivitiTestCase{

  private static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static final String DEPLOYMENT_FILE_PATH = "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml";

  private org.activiti.engine.repository.Deployment oldDeployment;
  private org.activiti.engine.repository.Deployment newDeployment;
  private List<String> processInstanceIds;

  protected void setUp() throws Exception {
    super.setUp();
    oldDeployment = repositoryService.createDeployment()
      .addClasspathResource(DEPLOYMENT_FILE_PATH)
      .deploy();

    processInstanceIds = new ArrayList<String>();

    Map<String, Object> startMap = new HashMap<String, Object>();
    startMap.put("test", 123);
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, startMap).getId());

    newDeployment = repositoryService.createDeployment()
          .addClasspathResource(DEPLOYMENT_FILE_PATH)
          .deploy();

    startMap.clear();
    startMap.put("anothertest", 456);
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, startMap).getId());
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(oldDeployment.getId(), true);
    repositoryService.deleteDeployment(newDeployment.getId(), true);
  }

  public void testHistoricProcessInstanceQueryByProcessDefinitionVersion() {
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(1).list().get(0).getProcessDefinitionVersion().intValue()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(2).list().get(0).getProcessDefinitionVersion().intValue()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(2).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(3).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(2).list().size()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionVersion(3).list().size()).isEqualTo(0);

    // Variables Case
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("test", 123).processDefinitionVersion(1).singleResult();
        assertThat(processInstance.getProcessDefinitionVersion().intValue()).isEqualTo(1);
        Map<String, Object> variableMap = processInstance.getProcessVariables();
        assertThat(variableMap.get("test")).isEqualTo(123);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 456).processDefinitionVersion(1).singleResult();
        assertThat(processInstance).isNull();

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 456).processDefinitionVersion(2).singleResult();
        assertThat(processInstance.getProcessDefinitionVersion().intValue()).isEqualTo(2);
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap.get("anothertest")).isEqualTo(456);
    }
  }

  public void testHistoricProcessInstanceQueryByProcessDefinitionVersionAndKey() {
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(1).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(2).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).list().size()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).list().size()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(1).list().size()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(2).list().size()).isEqualTo(0);
  }

  public void testHistoricProcessInstanceOrQueryByProcessDefinitionVersion() {
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(1).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(2).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(3).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(1).processDefinitionId("undefined").endOr().list().size()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(2).processDefinitionId("undefined").endOr().list().size()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionVersion(3).processDefinitionId("undefined").endOr().list().size()).isEqualTo(0);

    // Variables Case
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("test", "invalid").processDefinitionVersion(1).endOr().singleResult();
        assertThat(processInstance.getProcessDefinitionVersion().intValue()).isEqualTo(1);
        Map<String, Object> variableMap = processInstance.getProcessVariables();
        assertThat(variableMap.get("test")).isEqualTo(123);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionVersion(2).endOr().singleResult();
        assertThat(processInstance.getProcessDefinitionVersion().intValue()).isEqualTo(2);
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap.get("anothertest")).isEqualTo(456);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", "invalid").processDefinitionVersion(3).singleResult();
        assertThat(processInstance).isNull();
    }
  }
}
