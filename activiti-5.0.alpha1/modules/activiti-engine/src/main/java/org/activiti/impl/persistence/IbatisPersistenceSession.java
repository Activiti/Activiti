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
package org.activiti.impl.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.ActivitiOptimisticLockingException;
import org.activiti.Configuration;
import org.activiti.Page;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.DbidBlock;
import org.activiti.impl.db.DbidGenerator;
import org.activiti.impl.db.PropertyImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionDbImpl;
import org.activiti.impl.execution.JobImpl;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.tx.TransactionContext;
import org.activiti.mgmt.TablePage;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class IbatisPersistenceSession implements PersistenceSession {

  private static Logger log = Logger.getLogger(IbatisPersistenceSession.class.getName());
  
  protected TransactionContext transactionContext;
  protected SqlSession sqlSession;
  protected long blockSize = 100;
  protected DbidGenerator dbidGenerator;
  
  protected Inserted inserted = new Inserted();
  protected Loaded loaded = new Loaded();
  protected Deleted deleted = new Deleted();
  
  protected static String[] tableNames = new String[]{
      "ACT_PROPERTY",
      "ACT_BYTEARRAY",
      "ACT_DEPLOYMENT",
      "ACT_EXECUTION",
      "ACT_ID_GROUP",
      "ACT_ID_MEMBERSHIP",
      "ACT_ID_USER",
      "ACT_PROCESSDEFINITION",
      "ACT_TASK",
      "ACT_TASKINVOLVEMENT"
  };
  
  public IbatisPersistenceSession(TransactionContext transactionContext) {
    this.transactionContext = transactionContext;
    
    SqlSessionFactory sqlSessionFactory = transactionContext
      .getProcessEngine()
      .getConfigurationObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
  
    this.sqlSession = sqlSessionFactory.openSession();
    
    this.dbidGenerator = transactionContext
      .getProcessEngine()
      .getConfigurationObject(Configuration.NAME_DBIDGENERATOR, DbidGenerator.class);
  }

  public void insert(PersistentObject persistentObject) {
    String id = String.valueOf(dbidGenerator.getNextDbid());
    persistentObject.setId(id);
    inserted.add(persistentObject);
  }

  public void delete(PersistentObject persistentObject) {
    deleted.add(persistentObject);
    inserted.remove(persistentObject);
    loaded.remove(persistentObject);
  }

  public void commit() {
    flush();
    sqlSession.commit();
  }

  public void flush() {
    List<PersistentObject> updatedObjects = loaded.getUpdatedObjects();
    log.fine("flushing...");
    for (PersistentObject insertedObject: inserted.getInsertedObjects()) {
      log.fine("flush insert "+PersistentObjectId.toString(insertedObject));
    }
    for (PersistentObject updatedObject: updatedObjects) {
      log.fine("flush update "+PersistentObjectId.toString(updatedObject));
    }
    for (PersistentObject deletedObject: deleted.getDeletedObjects()) {
      log.fine("flush delete "+PersistentObjectId.toString(deletedObject));
    }

    inserted.flush(sqlSession);
    loaded.flush(sqlSession, updatedObjects);
    deleted.flush(sqlSession);
  }

  public void rollback() {
    sqlSession.rollback();
  }
  
  public void close() {
    sqlSession.close();
  }

  
  public ExecutionDbImpl findExecution(String executionId) {
    // TODO check if this execution was already loaded
    ExecutionDbImpl execution = (ExecutionDbImpl) sqlSession.selectOne("org.activiti.persistence.selectExecution", executionId);
    if (execution!=null) {
      execution = (ExecutionDbImpl) loaded.add(execution);
    }
    return execution;
  }
  
  @SuppressWarnings("unchecked")
  public List<ExecutionDbImpl> findExecutionsByProcessDefintion(String processDefinitionId) {
    List executions = sqlSession.selectList("org.activiti.persistence.selectExecutionsForProcessDefinition", processDefinitionId);
    return loaded.add(executions);
  }
  
  public long dynamicFindProcessInstanceCount(Map<String, Object> params) {
    return (Long) sqlSession.selectOne("org.activiti.persistence.dynamicSelectProcessInstanceCount", params);
  } 
  
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> dynamicFindProcessInstances(Map<String, Object> params) {
    return sqlSession.selectList("org.activiti.persistence.dynamicSelectProcessInstance", params);
  }
  
  public TaskImpl findTask(String id) {
    TaskImpl task = (TaskImpl) sqlSession.selectOne("org.activiti.persistence.selectTask", id);
    if (task!=null) {
      task = (TaskImpl) loaded.add(task);
    }
    return task;
  }
  
  @SuppressWarnings("unchecked")
  public List<TaskInvolvement> findTaskInvolvementsByTask(String taskId) {
    List taskInvolvements = sqlSession.selectList("org.activiti.persistence.selectTaskInvolvementsByTask", taskId);
    return loaded.add(taskInvolvements);
  }

  @SuppressWarnings("unchecked")
  public List<TaskImpl> findTasksByExecution(String executionId) {
    List tasks = sqlSession.selectList("org.activiti.persistence.selectTaskByExecution", executionId);
    return loaded.add(tasks);
  }
  


  // finders for static deployment and process definition information /////////
  
  @SuppressWarnings("unchecked")
  public List<DeploymentImpl> findDeployments() {
    return (List<DeploymentImpl>) sqlSession.selectList("org.activiti.persistence.selectDeployments");
  };
  
  public DeploymentImpl findDeployment(String deploymentId) {
    return (DeploymentImpl) sqlSession.selectOne(
            "org.activiti.persistence.selectDeployment",
            deploymentId
    );
  };
  
  public DeploymentImpl findDeploymentByProcessDefinitionId(String processDefinitionId) {
    return (DeploymentImpl) sqlSession.selectOne(
            "org.activiti.persistence.selectDeploymentByProcessDefinitionId",
            processDefinitionId
    );
  }
  
  @SuppressWarnings("unchecked")
  public List<ByteArrayImpl> findDeploymentResources(String deploymentId) {
    return sqlSession.selectList("org.activiti.persistence.selectByteArraysForDeployment", deploymentId);
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeploymentResourceNames(String deploymentId) {
    return sqlSession.selectList("org.activiti.persistence.selectResourceNamesForDeployment", deploymentId);
  }

  public ByteArrayImpl findDeploymentResource(String deploymentId, String resourceName) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceName", resourceName);
    return (ByteArrayImpl) sqlSession.selectOne("org.activiti.persistence.selectDeploymentResource", params);
  }
  
  @SuppressWarnings("unchecked")
  public byte[] getDeploymentResourceBytes(String resourceId) {
    // TODO: figure out how to directly map a byte array with ibatis
   Map<String, Object> temp =
     (Map)sqlSession.selectOne("org.activiti.persistence.selectBytesOfByteArray", resourceId);
   return (byte[]) temp.get("BYTES_");
  }
  
  public ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionImpl) sqlSession.selectOne("org.activiti.persistence.selectProcessDefinitionById", processDefinitionId);
  }
  
  public ProcessDefinitionImpl findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionImpl) sqlSession.selectOne("org.activiti.persistence.selectLatestProcessDefinitionByKey", processDefinitionKey);
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionImpl> findProcessDefinitions() {
    return sqlSession.selectList("org.activiti.persistence.selectProcessDefinitions");
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionImpl> findProcessDefinitionsByDeployment(String deploymentId) {
    List<ProcessDefinitionImpl> processDefinitions = new ArrayList<ProcessDefinitionImpl>();
    List<String> ids =  sqlSession.selectList("org.activiti.persistence.selectProcessDefinitionIdsByDeployment", deploymentId);
    for (String id : ids) {
      processDefinitions.add(findProcessDefinitionById(id));
    }
    return processDefinitions;
  }
  
  public ProcessDefinitionImpl findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionImpl) sqlSession.selectOne("org.activiti.persistence.selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  
  // job /////////////////////////////////////////////////////////////////////
  
  public JobImpl findJob(long jobId) {
    // TODO
    return null;
  }
  public List<List<Long>> findPendingJobs(int limit) {
    // TODO
    return null;
  }
  
  // user /////////////////////////////////////////////////////////////////////
  
  public void saveUser(UserImpl user) {
    if (user.isNew()) {
      sqlSession.insert("org.activiti.persistence.insertUser", user);
    } else {
      sqlSession.update("org.activiti.persistence.updateUser", user);
    }
  }

  public UserImpl findUser(String userId) {
    return (UserImpl) sqlSession.selectOne("org.activiti.persistence.selectUser", userId);
  }
  
  public List<UserImpl> findUsersByGroup(String groupId) {
    return sqlSession.selectList("org.activiti.persistence.selectUsersByGroup", groupId);
  }
  
  public List<UserImpl> findUsers() {
    return sqlSession.selectList("org.activiti.persistence.selectUsers");
  }
  
  public boolean isValidUser(String userId) {
    return findUser(userId) != null;
  }
  
  public void deleteUser(String userId) {
    sqlSession.delete("org.activiti.persistence.deleteMembershipsForUser", userId);
    sqlSession.delete("org.activiti.persistence.deleteUser", userId);
  }

  public void saveGroup(GroupImpl group) {
    if (group.isNew()) {
      sqlSession.insert("org.activiti.persistence.insertGroup", group);
    } else {
      sqlSession.update("org.activiti.persistence.updateGroup", group);
    }
  }
  
  public GroupImpl findGroup(String groupId) {
    return (GroupImpl) sqlSession.selectOne("org.activiti.persistence.selectGroup", groupId);
  }
  
  public List<GroupImpl> findGroupsByUser(String userId) {
    return sqlSession.selectList("org.activiti.persistence.selectGroupsByUser", userId);
  }
  
  public List<GroupImpl> findGroupsByUserAndType(String userId, String groupType) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupType", groupType);
    return sqlSession.selectList("org.activiti.persistence.selectGroupsByUserAndType", parameters);
  }

  public List<GroupImpl> findGroups() {
    return sqlSession.selectList("org.activiti.persistence.selectGroups");
  }

  public void deleteGroup(String groupId) {
    sqlSession.delete("org.activiti.persistence.deleteMembershipsForGroup", groupId);
    sqlSession.delete("org.activiti.persistence.deleteGroup", groupId);
  }  

  public void createMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    sqlSession.insert("org.activiti.persistence.insertMembership", parameters);
  }

  public void deleteMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    sqlSession.delete("org.activiti.persistence.deleteMembership", parameters);
  }
  


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/// till above this section, all methods are build in context of             //
/// the new persistence strategy                                             //
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List<Task> findCandidateTasks(String userId, List<String> groupIds) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userId", userId);
    params.put("groupIds", groupIds);
    List tasks = (List) sqlSession.selectList("org.activiti.persistence.selectCandidateTasks", params);
    return loaded.add(tasks);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByAssignee(String assignee) {
    return sqlSession.selectList("org.activiti.persistence.selectTasksByAssignee", assignee);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> dynamicFindTasks(Map<String, Object> params, Page page) {
    final String query = "org.activiti.persistence.dynamicSelectTask";
    if (page == null) {
      return sqlSession.selectList(query, params);      
    } else {
      return sqlSession.selectList(query, params, new RowBounds(page.getOffset(), page.getMaxResults()));
    }
  }
  
  public long dynamicFindTaskCount(Map<String, Object> params) {
    return (Long) sqlSession.selectOne("org.activiti.persistence.dynamicSelectTaskCount", params);
  }
  
  /*
   * INSERT operations
   */
  public void insertDeployment(DeploymentImpl deployment) {
    deployment.setId(String.valueOf(dbidGenerator.getNextDbid()));
    sqlSession.insert("org.activiti.persistence.insertDeployment", deployment);
    for (ByteArrayImpl resource : deployment.getResources().values()) {
      resource.setId(String.valueOf(dbidGenerator.getNextDbid()));
      resource.setDeploymentId(deployment.getId());
      sqlSession.insert("org.activiti.persistence.insertByteArray", resource);
    }
  }

  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    sqlSession.insert("org.activiti.persistence.insertProcessDefinition", processDefinition);
  }
  
  public void deleteDeployment(String deploymentId) {
    sqlSession.delete("org.activiti.persistence.deleteProcessDefinitionsForDeployment", deploymentId);
    sqlSession.delete("org.activiti.persistence.deleteByteArraysForDeployment", deploymentId);
    sqlSession.delete("org.activiti.persistence.deleteDeployment", deploymentId);
  }
  
  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName: tableNames) {
        log.fine("selecting table count for "+tableName);
        Long count = (Long) sqlSession.selectOne("org.activiti.persistence.selectTableCount", 
                Collections.singletonMap("tableName", tableName));
        tableCount.put(tableName, count);
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get table counts", e);
    }
    return tableCount;
  }

  @SuppressWarnings("unchecked")
  public TablePage getTablePage(String tableName, int offset, int maxResults) {
    RowBounds rowBounds = new RowBounds(offset, maxResults);
    List<Map<String, Object>> tableData = (List<Map<String, Object>>) sqlSession.selectList(
      "org.activiti.persistence.selectTableData", 
      Collections.singletonMap("tableName", tableName), 
      rowBounds
    );
    
    return createTablePage(tableName, offset, tableData);
  }
  
  /**
   * Converts the given dataset to a table page.
   * Note that the first map entry is used to calculate the column names and types,
   * so make sure that also null values are represented.
   */
  protected TablePage createTablePage(String tableName, int offset, List<Map<String, Object>> tableData) {
    
    TablePage tablePage = new TablePage();
    tablePage.setTableName(tableName);
    tablePage.setOffset(offset);
    tablePage.setRows(tableData);
    
    if (tableData.size() > 0) {
      
      List<String> columNames = new ArrayList<String>();
      List<Class< ? >> columnTypes = new ArrayList<Class< ? >>();      
      
      // iBatis will remove null colums from the resulting hashMap
      // So it is unfortunately required to go over every entry since any entry 
      // can have extra colums, defining a new colum name and type
      for (Map<String, Object> row : tableData) {
        for (String columnName : row.keySet()) {
          if (!columNames.contains(columnName)) {
            columNames.add(columnName);
            columnTypes.add(row.get(columnName).getClass());
          }
        }
      }
      
      tablePage.setColumnNames(columNames);
      tablePage.setColumnTypes(columnTypes);
    }
    
    return tablePage;
  }

  public DbidBlock getNextDbidBlock() {
    PropertyImpl property = (PropertyImpl) sqlSession.selectOne("org.activiti.persistence.selectProperty", "next.dbid");
    long oldValue = Long.parseLong(property.getValue());
    long newValue = oldValue+blockSize;
    Map<String, Object> updateValues = new HashMap<String, Object>();
    updateValues.put("name", property.getName());
    updateValues.put("dbversion", property.getDbversion());
    updateValues.put("newDbversion", property.getDbversion()+1);
    updateValues.put("value", Long.toString(newValue));
    int rowsUpdated = sqlSession.update("org.activiti.persistence.updateProperty", updateValues);
    if (rowsUpdated!=1) {
      throw new ActivitiOptimisticLockingException("couldn't get next block of dbids");
    }
    return new DbidBlock(oldValue, newValue-1);
  }
}
