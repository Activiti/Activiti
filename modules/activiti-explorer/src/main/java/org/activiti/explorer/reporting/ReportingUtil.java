/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.explorer.reporting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.WorkflowDefinition;


/**
 * @author Joram Barrez
 */
public class ReportingUtil {
  
  
  public static Connection getCurrentDatabaseConnection() {
    return Context.getCommandContext().getDbSqlSession().getSqlSession().getConnection();
  }
  
  public static ResultSet executeSelectSqlQuery(String sql) throws Exception {
    
    Connection connection = getCurrentDatabaseConnection();
    Statement select = connection.createStatement();
    return select.executeQuery(sql);
  }
  
  public static ProcessDefinition getProcessDefinition(DelegateExecution delegateExecution) {
    ExecutionEntity executionEntity = (ExecutionEntity) delegateExecution;
    if (executionEntity.getProcessDefinition() != null) {
      return (ProcessDefinition) executionEntity.getProcessDefinition();
    } else {
      return ProcessEngines.getDefaultProcessEngine()
              .getRepositoryService()
              .createProcessDefinitionQuery()
              .processDefinitionId(delegateExecution.getProcessDefinitionId())
              .singleResult();
    }
  }
  
  // WARNING!!! DemoWare!!!
  public static void generateTaskDurationReport(String processDefinitionId) {
    
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    // Fetch process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId).singleResult();
    
    // Report descriptin
    String reportDescription = "Average task duration report for process definition " + processDefinition.getName() + " ( version " + processDefinition.getVersion() + ")"; 
    
    // Script (just plain String for the moment)
    String script = "importPackage(java.sql);" +
                    "importPackage(java.lang);" +
                    "importPackage(org.activiti.explorer.reporting);" +
                    "" +
                    "var processDefinitionId = '" + processDefinitionId + "';" +
                    "" +
                    "var result = ReportingUtil.executeSelectSqlQuery(\"select NAME_, avg(DURATION_) from ACT_HI_TASKINST where PROC_DEF_ID_ = '"
                      + processDefinitionId + "' and END_TIME_ is not null group by NAME_\");" +
                    "" +
                    "var reportData = new ReportData();" +
                    "var dataset = reportData.newDataset();" +
                    "dataset.type = 'pieChart';" +
                    "dataset.description = '" + reportDescription + "';" +
                    "" +
                    "while (result.next()) { "+
                    "  var name = result.getString(1);" +
                    "  var val = result.getLong(2) / 1000;" +
                    "  dataset.add(name, val);" +
                    "}" +
                    "" +
                    "execution.setVariable('reportData', reportData.toBytes());";
    
    // Generate bpmn model
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name(processDefinition.getName() + " task duration report")
      .description(reportDescription)
      .addScriptStep(script);
    
    // Convert to BPMN 2.0 XML
    WorkflowDefinitionConversion conversion = ExplorerApp.get().getWorkflowDefinitionConversionFactory()
            .createWorkflowDefinitionConversion(workflowDefinition);
    conversion.convert();
    conversion.getBpmnModel().setTargetNamespace("activiti-report");
    
    // Generate DI
    BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(conversion.getBpmnModel());
    bpmnAutoLayout.execute();
    
    // Deploy
    repositoryService.createDeployment()
      .name(processDefinition.getName() + " - task duration report")
      .addString(conversion.getProcess().getId() + ".bpmn20.xml", conversion.getBpmn20Xml())
      .deploy();
  }

}
