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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.ActivitiOptimisticLockingException;
import org.activiti.Job;
import org.activiti.Page;
import org.activiti.ProcessInstance;
import org.activiti.SortOrder;
import org.activiti.TableMetaData;
import org.activiti.TablePage;
import org.activiti.Task;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.DbidBlock;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.db.PropertyImpl;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.job.JobImpl;
import org.activiti.impl.job.MessageImpl;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.time.Clock;
import org.activiti.impl.variable.DeserializedObject;
import org.activiti.impl.variable.VariableInstance;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class IbatisPersistenceSession implements PersistenceSession {

  private static Logger log = Logger.getLogger(IbatisPersistenceSession.class.getName());
  
  protected SqlSession sqlSession;
  protected long blockSize = 100;
  protected IdGenerator idGenerator;
  protected Map<String, String> databaseStatements;
  
  protected Inserted inserted = new Inserted(this);
  protected Loaded loaded = new Loaded(this);
  protected Deleted deleted = new Deleted(this);
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  
  protected static String[] tableNames = new String[]{
      "ACT_PROPERTY",
      "ACT_BYTEARRAY",
      "ACT_DEPLOYMENT",
      "ACT_EXECUTION",
      "ACT_ID_GROUP",
      "ACT_ID_MEMBERSHIP",
      "ACT_ID_USER",
      "ACT_JOB",
      "ACT_PROCESSDEFINITION",
      "ACT_TASK",
      "ACT_TASKINVOLVEMENT",
      "ACT_VARIABLE"
  };
  
  public IbatisPersistenceSession(SqlSession sqlSession, IdGenerator idGenerator, Map<String, String> databaseStatements) {
    this.sqlSession = sqlSession;
    this.idGenerator = idGenerator;
    this.databaseStatements = databaseStatements;
  }

  public void insert(PersistentObject persistentObject) {
    String id = String.valueOf(idGenerator.getNextDbid());
    persistentObject.setId(id);
    inserted.add(persistentObject);
  }
  
  public void delete(PersistentObject persistentObject) {
    deleted.add(persistentObject);
    inserted.remove(persistentObject);
    loaded.remove(persistentObject);
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

    for (DeserializedObject deserializedObject: deserializedObjects) {
      deserializedObject.flush();
    }
    
    inserted.flush(sqlSession);
    loaded.flush(sqlSession, updatedObjects);
    deleted.flush(sqlSession);
  }

  public void close() {
    sqlSession.close();
  }
  
  public void commit() {
    sqlSession.commit();
  }

  public void rollback() {
    sqlSession.rollback();
  }

  public String statement(String statement) {
    return databaseStatements.get(statement);
  }

  // executions ///////////////////////////////////////////////////////////////
  
  public DbExecutionImpl findExecution(String executionId) {
    // TODO check if this execution was already loaded
    DbExecutionImpl execution = (DbExecutionImpl) 
        sqlSession.selectOne(statement("selectExecution"), executionId);
    
    if (execution!=null) {
      execution = (DbExecutionImpl) loaded.add(execution);
    }
    return execution;
  }
  
  @SuppressWarnings("unchecked")
  public List<DbExecutionImpl> findRootExecutionsByProcessDefintion(String processDefinitionId) {
    List executions = sqlSession.selectList(statement("selectRootExecutionsForProcessDefinition"), processDefinitionId);
    return loaded.add(executions);
  }
  
  @SuppressWarnings("unchecked")
  public List<ExecutionImpl> findChildExecutions(String parentExecutionid) {
    List executions = sqlSession.selectList(statement("selectChildExecutions"), parentExecutionid);
    return loaded.add(executions);
  }
  
  public void deleteExecution(String executionId) {
    ExecutionImpl execution = findExecution(executionId);
    execution.end(); // TODO replace with real delete instead of end(), since this will create history traces
  }
  
  public long findProcessInstanceCountByDynamicCriteria(Map<String, Object> params) {
    return (Long) sqlSession.selectOne(statement("selectProcessInstanceCountByDynamicCriteria"), params);
  } 
  
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstancesByDynamicCriteria(Map<String, Object> params) {
    return sqlSession.selectList(statement("selectProcessInstanceByDynamicCriteria"), params);
  }
  
  // variables ////////////////////////////////////////////////////////////////

  public List<VariableInstance> findVariablesByExecutionId(String executionId) {
    List variablesInstances = sqlSession.selectList(statement("selectVariablesByExecutionId"), executionId);
    loaded.add(variablesInstances);
    return variablesInstances;
  }

  public List<VariableInstance> findVariablesByTaskId(String taskId) {
    List variableInstances = sqlSession.selectList(statement("selectVariablesByTaskId"), taskId);
    loaded.add(variableInstances);
    return variableInstances;
  }
  
  // tasks ////////////////////////////////////////////////////////////////////
  
  public TaskImpl findTask(String id) {
    TaskImpl task = (TaskImpl) sqlSession.selectOne(statement("selectTask"), id);
    if (task!=null) {
      task = (TaskImpl) loaded.add(task);
    }
    return task;
  }
  
  @SuppressWarnings("unchecked")
  public List<TaskInvolvement> findTaskInvolvementsByTask(String taskId) {
    List taskInvolvements = sqlSession.selectList(statement("selectTaskInvolvementsByTask"), taskId);
    return loaded.add(taskInvolvements);
  }
  
  @SuppressWarnings("unchecked")
  public List<TaskImpl> findTasksByExecution(String executionId) {
    List tasks = sqlSession.selectList(statement("selectTaskByExecution"), executionId);
    return loaded.add(tasks);
  }
  
  // finders for static deployment and process definition information /////////
  
  @SuppressWarnings("unchecked")
  public List<DeploymentImpl> findDeployments() {
    return (List<DeploymentImpl>) sqlSession.selectList(statement("selectDeployments"));
  };
  
  @SuppressWarnings("unchecked")
  public List<DeploymentImpl> findDeploymentsByName(String name) {
    return (List<DeploymentImpl>) sqlSession.selectList(statement("selectDeploymentsByName"), name);
  };
  
  public DeploymentImpl findDeployment(String deploymentId) {
    return (DeploymentImpl) sqlSession.selectOne(
            statement("selectDeployment"),
            deploymentId
    );
  };
  
  public DeploymentImpl findDeploymentByProcessDefinitionId(String processDefinitionId) {
    return (DeploymentImpl) sqlSession.selectOne(
            statement("selectDeploymentByProcessDefinitionId"),
            processDefinitionId
    );
  }
  
  @SuppressWarnings("unchecked")
  public List<ByteArrayImpl> findDeploymentResources(String deploymentId) {
    return sqlSession.selectList(statement("selectByteArraysForDeployment"), deploymentId);
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeploymentResourceNames(String deploymentId) {
    return sqlSession.selectList(statement("selectResourceNamesForDeployment"), deploymentId);
  }

  public ByteArrayImpl findDeploymentResource(String deploymentId, String resourceName) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceName", resourceName);
    return (ByteArrayImpl) sqlSession.selectOne(statement("selectDeploymentResource"), params);
  }
  
  @SuppressWarnings("unchecked")
  public byte[] getByteArrayBytes(String byteArrayId) {
   Map<String, Object> temp = (Map) sqlSession.selectOne(statement("selectBytesOfByteArray"), byteArrayId);
   return (byte[]) temp.get("BYTES_");
  }
  
  public ByteArrayImpl findByteArrayById(String byteArrayId) {
    ByteArrayImpl byteArray = (ByteArrayImpl) sqlSession.selectOne(statement("selectByteArrayById"), byteArrayId);
    loaded.add(byteArray);
    return byteArray;
  }

  
  public ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionImpl) sqlSession.selectOne(statement("selectProcessDefinitionById"), processDefinitionId);
  }
  
  public ProcessDefinitionImpl findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionImpl) sqlSession.selectOne(statement("selectLatestProcessDefinitionByKey"), processDefinitionKey);
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionImpl> findProcessDefinitions() {
    return sqlSession.selectList(statement("selectProcessDefinitions"));
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionImpl> findProcessDefinitionsByDeployment(String deploymentId) {
    List<ProcessDefinitionImpl> processDefinitions = new ArrayList<ProcessDefinitionImpl>();
    List<String> ids =  sqlSession.selectList(statement("selectProcessDefinitionIdsByDeployment"), deploymentId);
    for (String id : ids) {
      processDefinitions.add(findProcessDefinitionById(id));
    }
    return processDefinitions;
  }
  
  public ProcessDefinitionImpl findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionImpl) sqlSession.selectOne(statement("selectProcessDefinitionByDeploymentAndKey"), parameters);
  }

  
  // job /////////////////////////////////////////////////////////////////////
  
  public JobImpl findJobById(String jobId) {
    JobImpl job = (JobImpl) sqlSession.selectOne(statement("selectJob"), jobId);
    if (job!=null) {
      loaded.add(job);
    }
    return job;
  }
  
  public List<JobImpl> findJobs() {
    return sqlSession.selectList(statement("selectJobs"));
  }
  
  public List<JobImpl> findNextJobsToExecute(int maxNrOfJobs) {
    Date now = Clock.getCurrentTime();
    RowBounds rowBounds = new RowBounds(0, maxNrOfJobs);
    List<JobImpl> jobs = sqlSession.selectList(statement("selectNextJobsToExecute"), now, rowBounds);
    if (jobs!=null) {
      loaded.add(jobs);
    }
    return jobs;
  }
  
  @SuppressWarnings("unchecked")
  public List<JobImpl> findLockedJobs() {
    return sqlSession.selectList(statement("selectLockedJobs"));
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerImpl> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers) {
	final String query = "selectUnlockedTimersByDuedate";
	if (nrOfTimers > 0) {
		RowBounds rowBounds = new RowBounds(0,nrOfTimers);
		return sqlSession.selectList(statement(query), duedate, rowBounds);
	} else {
		return sqlSession.selectList(statement(query), duedate);
	}
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerImpl> findTimersByExecutionId(String executionId) {
    return sqlSession.selectList(statement("selectTimersByExecutionId"), executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> dynamicFindJobs(Map<String, Object> params, Page page) {
    final String query = "org.activiti.persistence.selectJobByDynamicCriteria";
    if (page == null) {
      return sqlSession.selectList(query, params);      
    } else {
      return sqlSession.selectList(query, params, new RowBounds(page.getOffset(), page.getMaxResults()));
    }
  }
  
  public long dynamicJobCount(Map<String, Object> params) {
    return (Long) sqlSession.selectOne("org.activiti.persistence.selectJobCountByDynamicCriteria", params);
  }
  
  // user /////////////////////////////////////////////////////////////////////
  
  public void saveUser(UserImpl user) {
    if (user.isNew()) {
      sqlSession.insert(statement("insertUser"), user);
    } else {
      sqlSession.update(statement("updateUser"), user);
    }
  }

  public UserImpl findUser(String userId) {
    return (UserImpl) sqlSession.selectOne(statement("selectUser"), userId);
  }
  
  public List<UserImpl> findUsersByGroup(String groupId) {
    return sqlSession.selectList(statement("selectUsersByGroup"), groupId);
  }
  
  public List<UserImpl> findUsers() {
    return sqlSession.selectList(statement("selectUsers"));
  }
  
  public boolean isValidUser(String userId) {
    return findUser(userId) != null;
  }
  
  public void deleteUser(String userId) {
    sqlSession.delete(statement("deleteMembershipsForUser"), userId);
    sqlSession.delete(statement("deleteUser"), userId);
  }

  public void saveGroup(GroupImpl group) {
    if (group.isNew()) {
      sqlSession.insert(statement("insertGroup"), group);
    } else {
      sqlSession.update(statement("updateGroup"), group);
    }
  }
  
  public GroupImpl findGroup(String groupId) {
    return (GroupImpl) sqlSession.selectOne(statement("selectGroup"), groupId);
  }
  
  public List<GroupImpl> findGroupsByUser(String userId) {
    return sqlSession.selectList(statement("selectGroupsByUser"), userId);
  }
  
  public List<GroupImpl> findGroupsByUserAndType(String userId, String groupType) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupType", groupType);
    return sqlSession.selectList(statement("selectGroupsByUserAndType"), parameters);
  }

  public List<GroupImpl> findGroups() {
    return sqlSession.selectList(statement("selectGroups"));
  }

  public void deleteGroup(String groupId) {
    sqlSession.delete(statement("deleteMembershipsForGroup"), groupId);
    sqlSession.delete(statement("deleteGroup"), groupId);
  }  

  public void createMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    sqlSession.insert(statement("insertMembership"), parameters);
  }

  public void deleteMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    sqlSession.delete(statement("deleteMembership"), parameters);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findCandidateTasks(String userId, List<String> groupIds) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userId", userId);
    params.put("groupIds", groupIds);
    List tasks = (List) sqlSession.selectList(statement("selectCandidateTasks"), params);
    return loaded.add(tasks);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByAssignee(String assignee) {
    return sqlSession.selectList(statement("selectTasksByAssignee"), assignee);
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> dynamicFindTasks(Map<String, Object> params, Page page) {
    final String query = statement("selectTaskByDynamicCriteria");
    if (page == null) {
      return sqlSession.selectList(query, params);      
    } else {
      return sqlSession.selectList(query, params, new RowBounds(page.getOffset(), page.getMaxResults()));
    }
  }
  
  public long dynamicFindTaskCount(Map<String, Object> params) {
    return (Long) sqlSession.selectOne(statement("selectTaskCountByDynamicCriteria"), params);
  }
  
  /*
   * INSERT operations
   */
  public void insertDeployment(DeploymentImpl deployment) {
    deployment.setId(String.valueOf(idGenerator.getNextDbid()));
    sqlSession.insert(statement("insertDeployment"), deployment);
    for (ByteArrayImpl resource : deployment.getResources().values()) {
      resource.setId(String.valueOf(idGenerator.getNextDbid()));
      resource.setDeploymentId(deployment.getId());
      sqlSession.insert(statement("insertByteArray"), resource);
    }
  }

  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    ProcessDefinitionImpl latestProcessDefinition = findLatestProcessDefinitionByKey(processDefinition.getKey());
    if (latestProcessDefinition!=null) {
      processDefinition.setVersion(latestProcessDefinition.getVersion()+1);
    } else {
      processDefinition.setVersion(1);
    }
    processDefinition.setId(processDefinition.getKey()+":"+processDefinition.getVersion());
    sqlSession.insert(statement("insertProcessDefinition"), processDefinition);
  }
  
  public void deleteDeployment(String deploymentId) {
    sqlSession.delete(statement("deleteProcessDefinitionsForDeployment"), deploymentId);
    sqlSession.delete(statement("deleteByteArraysForDeployment"), deploymentId);
    sqlSession.delete(statement("deleteDeployment"), deploymentId);
  }
  
  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName: tableNames) {
        tableCount.put(tableName, getTableCount(tableName));
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get table counts", e);
    }
    return tableCount;
  }
  
  protected long getTableCount(String tableName) {
    log.fine("selecting table count for "+tableName);
    Long count = (Long) sqlSession.selectOne(statement("selectTableCount"), 
            Collections.singletonMap("tableName", tableName));
    return count;
  }

  @SuppressWarnings("unchecked")
 public TablePage getTablePage(String tableName, int offset, int maxResults, 
         String sortColumn, SortOrder sortOrder) {
    
    TablePage tablePage = new TablePage();
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("tableName", tableName);
    if (sortColumn != null) {
      params.put("sortColumn", sortColumn);
      if (sortOrder.equals(SortOrder.ASCENDING)) {
        params.put("sortOrder", "asc");        
      } else {
        params.put("sortOrder", "desc");
      }
      
      tablePage.setSort(sortColumn);
      tablePage.setOrder(sortOrder);
    } 
    
    List<Map<String, Object>> tableData = (List<Map<String, Object>>) sqlSession.selectList(
      statement("selectTableData"), params, new RowBounds(offset, maxResults)
    );
    
    tablePage.setTableName(tableName);
    tablePage.setStart(offset);
    tablePage.setTotal(getTableCount(tableName));
    tablePage.setRows(tableData);
    return tablePage;
  }
  
  public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    try {
      result.setTableName(tableName);
      DatabaseMetaData metaData = sqlSession.getConnection().getMetaData();
      ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
      while(resultSet.next()) {
        String name = resultSet.getString("COLUMN_NAME");
        String type = resultSet.getString("TYPE_NAME");
        result.addColumnMetaData(name, type);
      }
    } catch (SQLException e) {
      throw new ActivitiException("Could not retrieve database metadata: " + e.getMessage());
    }
    
    return result;
  }

  public DbidBlock getNextDbidBlock() {
    PropertyImpl property = (PropertyImpl) sqlSession.selectOne(statement("selectProperty"), "next.dbid");
    long oldValue = Long.parseLong(property.getValue());
    long newValue = oldValue+blockSize;
    Map<String, Object> updateValues = new HashMap<String, Object>();
    updateValues.put("name", property.getName());
    updateValues.put("revision", property.getDbversion());
    updateValues.put("newRevision", property.getDbversion()+1);
    updateValues.put("value", Long.toString(newValue));
    int rowsUpdated = sqlSession.update(statement("updateProperty"), updateValues);
    if (rowsUpdated!=1) {
      throw new ActivitiOptimisticLockingException("couldn't get next block of dbids");
    }
    return new DbidBlock(oldValue, newValue-1);
  }
  
  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstance variableInstance) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstance));
  }

  public SqlSession getSqlSession() {
    return sqlSession;
  }
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  public static String[] getTableNames() {
    return tableNames;
  }

  private static class Loaded {

    private static Logger log = Logger.getLogger(Loaded.class.getName());

    protected static Map<Class<?>,String> updateStatementIds = new HashMap<Class<?>, String>();
    static {
      updateStatementIds.put(DbExecutionImpl.class, "updateExecution");
      updateStatementIds.put(TaskImpl.class, "updateTask");
      updateStatementIds.put(TaskInvolvement.class, "updateTaskInvolvement");
      updateStatementIds.put(VariableInstance.class, "updateVariableInstance");
      updateStatementIds.put(ByteArrayImpl.class, "updateByteArray");
      updateStatementIds.put(MessageImpl.class, "updateMessage");
      updateStatementIds.put(TimerImpl.class, "updateTimer");
    }

    protected Map<Object, LoadedObject> loadedObjects = new HashMap<Object, LoadedObject>();
    protected IbatisPersistenceSession ibatisPersistenceSession;

    public Loaded(IbatisPersistenceSession ibatisPersistenceSession) {
      this.ibatisPersistenceSession = ibatisPersistenceSession;
    }

    public PersistentObject add(PersistentObject persistentObject) {
      Object key = getKey(persistentObject);
      LoadedObject loadedObject = loadedObjects.get(key);
      if (loadedObject!=null) {
        return loadedObject.getPersistentObject();
      }
      loadedObject = new LoadedObject(persistentObject);
      loadedObjects.put(key, loadedObject);
      return persistentObject;
    }

    public List add(List persistentObjects) {
      List<PersistentObject> loadedPersistentObjects = new ArrayList<PersistentObject>();
      for (Object persistentObject: persistentObjects) {
        PersistentObject loadedPersistentObject = add((PersistentObject) persistentObject);
        loadedPersistentObjects.add(loadedPersistentObject);
      }
      return loadedPersistentObjects;

    }

    public void remove(PersistentObject persistentObject) {
      Object key = getKey(persistentObject);
      loadedObjects.remove(key);
    }

    protected Object getKey(PersistentObject persistentObject) {
      List<Object> key = new ArrayList<Object>();
      key.add(persistentObject.getClass());
      key.add(persistentObject.getId());
      return key;
    }

    public void flush(SqlSession sqlSession, List<PersistentObject> updatedObjects) {
      for (PersistentObject updatedObject: updatedObjects) {
        Class<?> updatedObjectClass = updatedObject.getClass();
        String updateStatementId = findUpdateStatementId(updatedObjectClass);
        if (updateStatementId==null) {
          throw new ActivitiException("no update statement id for "+updatedObject.getClass());
        }
        log.fine("updating: "+updatedObjectClass+"["+updatedObject.getId()+"]");
        sqlSession.update(ibatisPersistenceSession.statement(updateStatementId), updatedObject);
      }
      updatedObjects.clear();
    }

    protected String findUpdateStatementId(Class< ? > updatedObjectClass) {
      if (updatedObjectClass==null) {
        return null;
      }
      String updateStatementId = updateStatementIds.get(updatedObjectClass);
      if (updateStatementId==null) {
        return findUpdateStatementId(updatedObjectClass.getSuperclass());
      }
      return updateStatementId;
    }

    public List<PersistentObject> getUpdatedObjects() {
      List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
      for (LoadedObject loadedObject: loadedObjects.values()) {
        PersistentObject persistentObject = (PersistentObject) loadedObject.getPersistentObject();
        Object originalState = loadedObject.getPersistentObjectState();
        if (!originalState.equals(persistentObject.getPersistentState())) {
          updatedObjects.add(persistentObject);
        } else {
          log.finest("persistent object '"+persistentObject+"' was not updated");
        }
      }
      return updatedObjects;
    }
  }

  private static class Inserted {

    private static Logger log = Logger.getLogger(Inserted.class.getName());

    protected static Map<Class<?>,String> insertStatementIds = new HashMap<Class<?>, String>();
    static {
      insertStatementIds.put(DbExecutionImpl.class, "insertExecution");
      insertStatementIds.put(JobImpl.class, "insertJob");
      insertStatementIds.put(TaskImpl.class, "insertTask");
      insertStatementIds.put(TaskInvolvement.class, "insertTaskInvolvement");
      insertStatementIds.put(VariableInstance.class, "insertVariableInstance");
      insertStatementIds.put(ByteArrayImpl.class, "insertByteArray");
      insertStatementIds.put(MessageImpl.class, "insertMessage");
      insertStatementIds.put(TimerImpl.class, "insertTimer");
    }

    protected List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();
    protected IbatisPersistenceSession ibatisPersistenceSession;

    public Inserted(IbatisPersistenceSession ibatisPersistenceSession) {
      this.ibatisPersistenceSession = ibatisPersistenceSession;
    }

    public void add(PersistentObject persistentObject) {
      insertedObjects.add(persistentObject);
    }

    public void remove(PersistentObject persistentObject) {
      insertedObjects.remove(persistentObject);
    }

    public List<PersistentObject> getInsertedObjects() {
      return insertedObjects;
    }

    public void flush(SqlSession sqlSession) {
      for (PersistentObject insertedObject: insertedObjects) {
        Class<?> insertedObjectClass = insertedObject.getClass();
        String insertStatementId = findInsertStatementId(insertedObjectClass);
        if (insertStatementId==null) {
          throw new ActivitiException("no insert statement id for "+insertedObject.getClass());
        }
        log.fine("inserting: "+insertedObjectClass+"["+insertedObject.getId()+"]");
        sqlSession.insert(ibatisPersistenceSession.statement(insertStatementId), insertedObject);
      }
      insertedObjects.clear();
    }

    protected String findInsertStatementId(Class< ? > insertedObjectClass) {
      if (insertedObjectClass==null) {
        return null;
      }
      String insertStatementId = insertStatementIds.get(insertedObjectClass);
      if (insertStatementId==null) {
        return findInsertStatementId(insertedObjectClass.getSuperclass());
      }
      return insertStatementId;
    }

    public int size() {
      return insertedObjects.size();
    }
  }

  private static class Deleted {

    private static Logger log = Logger.getLogger(Deleted.class.getName());

    protected static Map<Class<?>,String> deleteStatementIds = new HashMap<Class<?>, String>();
    static {
      deleteStatementIds.put(DbExecutionImpl.class, "deleteExecution");
      deleteStatementIds.put(TaskImpl.class, "deleteTask");
      deleteStatementIds.put(TaskInvolvement.class, "deleteTaskInvolvement");
      deleteStatementIds.put(VariableInstance.class, "deleteVariableInstance");
      deleteStatementIds.put(ByteArrayImpl.class, "deleteByteArray");
      deleteStatementIds.put(MessageImpl.class, "deleteJob");
      deleteStatementIds.put(TimerImpl.class, "deleteJob");
    }

    protected List<PersistentObject> deletedObjects = new ArrayList<PersistentObject>();
    protected Set<PersistentObjectId> deletedObjectIds = new HashSet<PersistentObjectId>();
    protected IbatisPersistenceSession ibatisPersistenceSession;

    public Deleted(IbatisPersistenceSession ibatisPersistenceSession) {
      this.ibatisPersistenceSession = ibatisPersistenceSession;
    }

    public void add(PersistentObject persistentObject) {
      PersistentObjectId deletedObjectId = new PersistentObjectId(persistentObject);
      if (!deletedObjectIds.contains(deletedObjectId)) {
        deletedObjects.add(persistentObject);
        deletedObjectIds.add(deletedObjectId);
      }
    }

    public int size() {
      return deletedObjects.size();
    }

    public List<PersistentObject> getDeletedObjects() {
      return deletedObjects;
    }

    public void flush(SqlSession sqlSession) {
      for (PersistentObject deletedObject: deletedObjects) {
        Class<?> deletedObjectClass = deletedObject.getClass();
        String deleteStatementId = findDeleteStatementId(deletedObjectClass);
        if (deleteStatementId==null) {
          throw new ActivitiException("no delete statement id for "+deletedObject.getClass());
        }
        log.fine("deleting: "+deletedObjectClass+"["+deletedObject.getId()+"]");
        sqlSession.delete(ibatisPersistenceSession.statement(deleteStatementId), deletedObject);
      }
      deletedObjects.clear();
      deletedObjectIds.clear();
    }

    protected String findDeleteStatementId(Class< ? > deletedObjectClass) {
      if (deletedObjectClass==null) {
        return null;
      }
      String deleteStatementId = deleteStatementIds.get(deletedObjectClass);
      if (deleteStatementId==null) {
        return findDeleteStatementId(deletedObjectClass.getSuperclass());
      }
      return deleteStatementId;
    }
  }
}
