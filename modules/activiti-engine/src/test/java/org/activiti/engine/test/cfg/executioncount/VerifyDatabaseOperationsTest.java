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

package org.activiti.engine.test.cfg.executioncount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.profiler.ActivitiProfiler;
import org.activiti.engine.test.profiler.CommandStats;
import org.activiti.engine.test.profiler.ConsoleLogger;
import org.activiti.engine.test.profiler.ProfileSession;
import org.activiti.engine.test.profiler.ProfilingDbSqlSessionFactory;
import org.activiti.engine.test.profiler.TotalExecutionTimeCommandInterceptor;
import org.junit.Assert;

/**
 * @author Joram Barrez
 */
public class VerifyDatabaseOperationsTest extends PluggableActivitiTestCase {
  
  protected boolean oldExecutionTreeFetchValue;
  protected boolean oldExecutionRelationshipCountValue;
  protected boolean oldTaskRelationshipCountValue;
  protected boolean oldenableProcessDefinitionInfoCacheValue;
  protected CommandInterceptor oldFirstCommandInterceptor;
  protected DbSqlSessionFactory oldDbSqlSessionFactory;
  protected HistoryLevel oldHistoryLevel;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Enable flags
    this.oldExecutionTreeFetchValue = processEngineConfiguration.getPerformanceSettings().isEnableEagerExecutionTreeFetching();
    this.oldExecutionRelationshipCountValue = processEngineConfiguration.getPerformanceSettings().isEnableExecutionRelationshipCounts();
    this.oldTaskRelationshipCountValue = processEngineConfiguration.getPerformanceSettings().isEnableTaskRelationshipCounts();
    this.oldenableProcessDefinitionInfoCacheValue = processEngineConfiguration.isEnableProcessDefinitionInfoCache();
    oldHistoryLevel = ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).getHistoryLevel();

    processEngineConfiguration.getPerformanceSettings().setEnableEagerExecutionTreeFetching(true);
    processEngineConfiguration.getPerformanceSettings().setEnableExecutionRelationshipCounts(true);
    processEngineConfiguration.getPerformanceSettings().setEnableTaskRelationshipCounts(true);
    processEngineConfiguration.setEnableProcessDefinitionInfoCache(false);
    ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).setHistoryLevel(HistoryLevel.AUDIT);

    // The time interceptor should be first
    CommandExecutorImpl commandExecutor = ((CommandExecutorImpl) processEngineConfiguration.getCommandExecutor());
    this.oldFirstCommandInterceptor = commandExecutor.getFirst();
    
    TotalExecutionTimeCommandInterceptor timeCommandInterceptor = new TotalExecutionTimeCommandInterceptor();
    timeCommandInterceptor.setNext(oldFirstCommandInterceptor);
    commandExecutor.setFirst(timeCommandInterceptor);
    
    // Add dbsqlSession factory that captures CRUD operations
    this.oldDbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
    DbSqlSessionFactory newDbSqlSessionFactory = new ProfilingDbSqlSessionFactory();
    newDbSqlSessionFactory.setDatabaseType(oldDbSqlSessionFactory.getDatabaseType());
    newDbSqlSessionFactory.setDatabaseTablePrefix(oldDbSqlSessionFactory.getDatabaseTablePrefix());
    newDbSqlSessionFactory.setTablePrefixIsSchema(oldDbSqlSessionFactory.isTablePrefixIsSchema());
    newDbSqlSessionFactory.setDatabaseCatalog(oldDbSqlSessionFactory.getDatabaseCatalog());
    newDbSqlSessionFactory.setDatabaseSchema(oldDbSqlSessionFactory.getDatabaseSchema());
    newDbSqlSessionFactory.setSqlSessionFactory(oldDbSqlSessionFactory.getSqlSessionFactory());
    newDbSqlSessionFactory.setIdGenerator(oldDbSqlSessionFactory.getIdGenerator());
    newDbSqlSessionFactory.setDbIdentityUsed(oldDbSqlSessionFactory.isDbIdentityUsed());
    newDbSqlSessionFactory.setDbHistoryUsed(oldDbSqlSessionFactory.isDbHistoryUsed());
    processEngineConfiguration.addSessionFactory(newDbSqlSessionFactory);
  }
  
  @Override
  protected void tearDown() throws Exception {
    
    processEngineConfiguration.getPerformanceSettings().setEnableEagerExecutionTreeFetching(oldExecutionTreeFetchValue);
    processEngineConfiguration.getPerformanceSettings().setEnableExecutionRelationshipCounts(oldExecutionRelationshipCountValue);
    processEngineConfiguration.getPerformanceSettings().setEnableTaskRelationshipCounts(oldTaskRelationshipCountValue);
    processEngineConfiguration.setEnableProcessDefinitionInfoCache(oldenableProcessDefinitionInfoCacheValue);
    ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).setHistoryLevel(oldHistoryLevel);
    
    ((CommandExecutorImpl) processEngineConfiguration.getCommandExecutor()).setFirst(oldFirstCommandInterceptor);;
    
    processEngineConfiguration.addSessionFactory(oldDbSqlSessionFactory);
    
    // Validate (cause this tended to be screwed up)
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      Assert.assertTrue(historicActivityInstance.getStartTime() != null);
      Assert.assertTrue(historicActivityInstance.getEndTime() != null);
    }
    
    ActivitiProfiler.getInstance().reset();
    
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }
  
  public void testStartToEnd() {
    deployStartProcessInstanceAndProfile("process01.bpmn20.xml", "process01");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-2", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testVariablesAndPassthrough() {
    deployStartProcessInstanceAndProfile("process-variables-servicetask01.bpmn20.xml", "process-variables-servicetask01");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricVariableInstanceEntityImpl-bulk-with-4", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }

  public void testManyVariablesViaServiceTaskAndPassthroughs() {
    deployStartProcessInstanceAndProfile("process-variables-servicetask02.bpmn20.xml", "process-variables-servicetask02");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricVariableInstanceEntityImpl-bulk-with-50", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testOnlyPassThroughs() {
    deployStartProcessInstanceAndProfile("process02.bpmn20.xml", "process02");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }

  public void testParallelForkAndJoin() {
    deployStartProcessInstanceAndProfile("process03.bpmn20.xml", "process03");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-7", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testNestedParallelForkAndJoin() {
    deployStartProcessInstanceAndProfile("process04.bpmn20.xml", "process04");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-21", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testExlusiveGateway() {
    deployStartProcessInstanceAndProfile("process05.bpmn20.xml", "process05");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-5", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricVariableInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testOneTaskProcess() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    stopProfiling();
    
    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "CompleteTaskCmd");
    
    // Start process instance
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "ExecutionEntityImpl-bulk-with-2", 1L,
        "TaskEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-2", 1L,
        "HistoricTaskInstanceEntityImpl", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    // Task Query
    assertDatabaseSelects("org.activiti.engine.impl.TaskQueryImpl", 
        "selectTaskByQueryCriteria", 1L);
    assertNoInserts("org.activiti.engine.impl.TaskQueryImpl");
    assertNoUpdates("org.activiti.engine.impl.TaskQueryImpl");
    assertNoDeletes("org.activiti.engine.impl.TaskQueryImpl");
    
    // Complete the task
    assertDatabaseSelects("CompleteTaskCmd", 
        "selectById org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl", 1L,
        "selectById org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityImpl", 1L,
        "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 1L,
        "selectUnfinishedHistoricActivityInstanceExecutionIdAndActivityId", 2L,
        "selectTasksByParentTaskId", 1L,
        "selectExecutionsWithSameRootProcessInstanceId", 1L,
        "selectTasksByExecutionId", 1L
        );
    assertDatabaseInserts("CompleteTaskCmd", 
        "HistoricActivityInstanceEntityImpl", 1L);
    assertDatabaseUpdates("CompleteTaskCmd", "org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityImpl", 1L,
            "org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl", 2L,
            "org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl", 1L,
            "org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl", 1L);
    assertDatabaseDeletes("CompleteTaskCmd", 
        "TaskEntityImpl", 1L,
        "ExecutionEntityImpl", 2L);
    
  }
  
  public void testRemovingTaskVariables() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();

    long variableCount = 3;

    Map<String, Object> vars = createVariables(variableCount, "local");

    taskService.setVariablesLocal(task.getId(), vars);
        
    vars.put("someRandVar", "someRandVal");

    taskService.removeVariablesLocal(task.getId(), vars.keySet());

    taskService.removeVariablesLocal(task.getId(), vars.keySet());
    taskService.removeVariablesLocal(task.getId(), vars.keySet());
    taskService.complete(task.getId());
    stopProfiling();

    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "SetTaskVariablesCmd", "RemoveTaskVariablesCmd",
      "CompleteTaskCmd");
    assertDatabaseInserts("SetTaskVariablesCmd", "HistoricVariableInstanceEntityImpl-bulk-with-3", 1L, "VariableInstanceEntityImpl-bulk-with-3", 1L);

    assertDatabaseDeletes("RemoveTaskVariablesCmd", "VariableInstanceEntityImpl", variableCount, "HistoricVariableInstanceEntityImpl", variableCount);
  }
	  
  public void testClaimingTask() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();
    
    taskService.claim(task.getId(), "user1");
    taskService.unclaim(task.getId());
            
    taskService.complete(task.getId());
    stopProfiling();

    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "ClaimTaskCmd" ,"CompleteTaskCmd");
        
    assertNoDeletes("ClaimTaskCmd");
    assertDatabaseInserts("ClaimTaskCmd", "IdentityLinkEntityImpl", 1L, "HistoricIdentityLinkEntityImpl", 1L);    
	}
	  
  public void testTaskCandidateUsers() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.addCandidateUser(task.getId(), "user1");
    taskService.addCandidateUser(task.getId(), "user2");
    
    taskService.deleteCandidateUser(task.getId(), "user1");
    taskService.deleteCandidateUser(task.getId(), "user2");

    taskService.deleteCandidateUser(task.getId(), "user2");
    taskService.deleteCandidateUser(task.getId(), "user3");
    taskService.deleteCandidateUser(task.getId(), "user4");
       
    taskService.complete(task.getId());
    stopProfiling();
	  
    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "AddIdentityLinkCmd", "DeleteIdentityLinkCmd",
      "CompleteTaskCmd");

    assertNoDeletes("AddIdentityLinkCmd");
    assertDatabaseInserts("AddIdentityLinkCmd", "CommentEntityImpl", 2L, "HistoricIdentityLinkEntityImpl-bulk-with-2", 2L, "IdentityLinkEntityImpl-bulk-with-2",2l);
    assertDatabaseSelects("AddIdentityLinkCmd", "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L, "selectIdentityLinksByTask", 2L,
      "selectExecutionsWithSameRootProcessInstanceId", 2L, "selectIdentityLinksByProcessInstance", 2L);
    assertDatabaseUpdates("AddIdentityLinkCmd", "org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L,
      "org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl", 2L);

    assertDatabaseDeletes("DeleteIdentityLinkCmd", "IdentityLinkEntityImpl", 2L, "HistoricIdentityLinkEntityImpl", 2L);
    assertDatabaseInserts("DeleteIdentityLinkCmd", "CommentEntityImpl", 5L);
    assertDatabaseSelects("DeleteIdentityLinkCmd", "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 5L,
      "selectIdentityLinkByTaskUserGroupAndType", 5L, "selectById org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityImpl", 2L,
      "selectIdentityLinksByTask", 5L);
    assertDatabaseUpdates("DeleteIdentityLinkCmd", "org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L);
  }

  public void testTaskCandidateGroups() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.addCandidateGroup(task.getId(), "group1");
    taskService.addCandidateGroup(task.getId(), "group2");   
	    
    taskService.deleteCandidateGroup(task.getId(), "group1");
    taskService.deleteCandidateGroup(task.getId(), "group2");

    taskService.deleteCandidateGroup(task.getId(), "group2");
    taskService.deleteCandidateGroup(task.getId(), "group3");
    taskService.deleteCandidateGroup(task.getId(), "group4");
	    
    taskService.complete(task.getId());
    stopProfiling();
	    
    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "AddIdentityLinkCmd", "DeleteIdentityLinkCmd",
           "CompleteTaskCmd");

    assertNoDeletes("AddIdentityLinkCmd");
    assertDatabaseInserts("AddIdentityLinkCmd", "CommentEntityImpl", 2L, "IdentityLinkEntityImpl", 2L, "HistoricIdentityLinkEntityImpl",
           2L);
    assertDatabaseSelects("AddIdentityLinkCmd", "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L, "selectIdentityLinksByTask", 2L);
    assertDatabaseUpdates("AddIdentityLinkCmd", "org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L);

    assertDatabaseDeletes("DeleteIdentityLinkCmd", "IdentityLinkEntityImpl", 2L, "HistoricIdentityLinkEntityImpl", 2L);
    assertDatabaseInserts("DeleteIdentityLinkCmd", "CommentEntityImpl", 5L);
    assertDatabaseSelects("DeleteIdentityLinkCmd", "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 5L,
	  "selectIdentityLinkByTaskUserGroupAndType", 5L, "selectById org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityImpl", 2L,
	  "selectIdentityLinksByTask", 5L);
    assertDatabaseUpdates("DeleteIdentityLinkCmd", "org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 2L);
  }

  private Map<String, Object> createVariables(long count, String prefix) {
    Map<String, Object> vars = new HashMap<String, Object>();
    for (int i = 0; i < count; i++) {
      vars.put(prefix + "_var0" + i, prefix + "+values0" + i);
    }
    return vars;
  }
  
  
  // ---------------------------------
  // HELPERS
  // ---------------------------------
  
  protected void assertExecutedCommands(String...commands) {
    ProfileSession profileSession = ActivitiProfiler.getInstance().getProfileSessions().get(0);
    Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
    
    if (commands.length != allStats.size()) {
      System.out.println("Following commands were found: ");
      for (String command : allStats.keySet()) {
        System.out.println(command);
      }
    }
    Assert.assertEquals(commands.length, allStats.size());
    
    for (String command : commands) {
      Assert.assertNotNull("Could not get stats for " + command, getStatsForCommand(command, allStats));
    }
  }

  protected void assertDatabaseSelects(String commandClass, Object ... expectedSelects) {
    CommandStats stats = getStats(commandClass);
    if (expectedSelects.length / 2 != stats.getDbSelects().size()) {
      Assert.fail("Unexpected number of database selects : " + stats.getDbSelects().size());
    }
    
    for (int i=0; i<expectedSelects.length; i+=2) {
      String dbSelect = (String) expectedSelects[i];
      Long count = (Long) expectedSelects[i+1];
      
      Assert.assertEquals("Wrong select count for " + dbSelect, count, stats.getDbSelects().get(dbSelect));
    }
  }

  protected void assertDatabaseInserts(String commandClass, Object ... expectedInserts) {
    CommandStats stats = getStats(commandClass);
    
    if (expectedInserts.length / 2 != stats.getDbInserts().size()) {
      Assert.fail("Unexpected number of database inserts : " + stats.getDbInserts().size() + ", but expected " + expectedInserts.length / 2);
    }
    
    for (int i=0; i<expectedInserts.length; i+=2) {
      String dbInsert = (String) expectedInserts[i];
      Long count = (Long) expectedInserts[i+1];
      
      Assert.assertEquals("Insert count for " + dbInsert + "not correct", count, stats.getDbInserts().get("org.activiti.engine.impl.persistence.entity." + dbInsert));
    }
  }
  
  protected void assertDatabaseUpdates(String commandClass, Object... expectedUpdates) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals("Unexpected number of database updates for " + commandClass + ". ", expectedUpdates.length / 2, stats.getDbUpdates().size());

    for (int i = 0; i < expectedUpdates.length; i += 2) {
      String dbUpdate = (String) expectedUpdates[i];
      Long count = (Long) expectedUpdates[i + 1];

      Assert.assertEquals("Wrong update count for " + dbUpdate, count, stats.getDbUpdates().get(dbUpdate));
    }
  }
  
  protected void assertDatabaseDeletes(String commandClass,  Object ... expectedDeletes) {
    CommandStats stats = getStats(commandClass);
    
    if (expectedDeletes.length / 2 != stats.getDbDeletes().size()) {
      Assert.fail("Unexpected number of database deletes : " + stats.getDbDeletes().size());
    }
    
    for (int i=0; i<expectedDeletes.length; i+=2) {
      String dbDelete = (String) expectedDeletes[i];
      Long count = (Long) expectedDeletes[i+1];
      
      Assert.assertEquals("Delete count count for " + dbDelete + "not correct", count, stats.getDbDeletes().get("org.activiti.engine.impl.persistence.entity." + dbDelete));
    }
  }
  
  protected void assertNoInserts(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbInserts().size());
  }
  
  protected void assertNoUpdatesAndDeletes(String commandClass) {
    assertNoDeletes(commandClass);
    assertNoUpdates(commandClass);
  }
  
  protected void assertNoDeletes(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbDeletes().size());
  }
  
  protected void assertNoUpdates(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbUpdates().size());
  }
  
  protected CommandStats getStats(String commandClass) {
    ProfileSession profileSession = ActivitiProfiler.getInstance().getProfileSessions().get(0);
    Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
    CommandStats stats = getStatsForCommand(commandClass, allStats);
    return stats;
  }

  protected CommandStats getStatsForCommand(String commandClass, Map<String, CommandStats> allStats) {
    String clazz = commandClass;
    if (!clazz.startsWith("org.activiti")) {
      clazz = "org.activiti.engine.impl.cmd." + clazz;
    }
    CommandStats stats = allStats.get(clazz);
    return stats;
  }

  
  // HELPERS
  
  protected ActivitiProfiler deployStartProcessInstanceAndProfile(String path, String processDefinitionKey) {
    return deployStartProcessInstanceAndProfile(path, processDefinitionKey, true);
  }
  
  protected ActivitiProfiler deployStartProcessInstanceAndProfile(String path, String processDefinitionKey, boolean stopProfilingAfterStart) {
    deploy(path);
    ActivitiProfiler activitiProfiler = startProcessInstanceAndProfile(processDefinitionKey);
    if (stopProfilingAfterStart) {
      stopProfiling();
    }
    return activitiProfiler;
  }
  
  protected void deploy(String path) {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/cfg/executioncount/" + path).deploy();
  }
  
  protected ActivitiProfiler startProcessInstanceAndProfile(String processDefinitionKey) {
    ActivitiProfiler activitiProfiler = ActivitiProfiler.getInstance();
    activitiProfiler.startProfileSession("Profiling session");
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    return activitiProfiler;
  }
  
  protected void stopProfiling() {
    ActivitiProfiler profiler = ActivitiProfiler.getInstance();
    profiler.stopCurrentProfileSession();
    new ConsoleLogger(profiler).log();;
  }

}