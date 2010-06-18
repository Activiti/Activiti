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
package org.activiti.test.persistence.inmemorylistsession;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.Page;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.DbidBlock;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionDbImpl;
import org.activiti.impl.execution.JobImpl;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.tx.TransactionContext;

/**
 * This is an implementation of {@link PersistenceSession}
 *  used in testing, which is backed by a series of Lists.
 * It doesn't support all features, eats lots of memory, and
 *  is probably quite slow.
 * However, it does offer full control over what goes into 
 *  the session, and what comes out, which allows certain
 *  kinds of unit tests to be very easy to write.
 *  
 * WARNING - Uses static storage (just like a database!),
 *  so if you have two of these, they'll share the
 *  same data.
 */
public class InMemoryListBasedPersistenceSession implements PersistenceSession {
  private TransactionContext transactionCxt;
  
  // All storage lists must be static!
  public static ArrayList<JobImpl> allJobsList = new ArrayList<JobImpl>();
  public static ArrayList<JobImpl> pendingJobsList = new ArrayList<JobImpl>();
  
  
  /**
   * Special list which holds a reference 
   *  to all the other ones
   */
  public ArrayList<ArrayList<PersistentObject>> lists = new ArrayList<ArrayList<PersistentObject>>();
  public InMemoryListBasedPersistenceSession() {
    // Find all our lists of objects, and track them
    Field[] fs = getClass().getDeclaredFields();
    for(Field f : fs) {
      if(f.getName().endsWith("List")) {
        try {
          ArrayList<PersistentObject> list = (ArrayList<PersistentObject>)f.get(this);
          lists.add(list);
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
  public InMemoryListBasedPersistenceSession(TransactionContext ctx) {
    this();
    this.transactionCxt = ctx;
  }
  
  public void reset() {
    for(ArrayList<PersistentObject> a : lists) {
      a.clear();
    }
  }

  
  // ======================== Open/Close =================================
  
  
  /**
   * Does nothing
   */
  @Override
  public void close() {}

  /**
   * Does nothing
   */
  public void commit() {}

  /**
   * Does nothing
   */
  @Override
  public void flush() {}

  @Override
  public void rollback() {
    throw new IllegalStateException("Rollback not supported");
  }


  // ======================== Core =================================

  
  @Override
  public void delete(PersistentObject persistentObject) {
    if(persistentObject instanceof PersistenceTimingAware) {
      ((PersistenceTimingAware)persistentObject).deletedAt(System.currentTimeMillis());
    }
    for(ArrayList<PersistentObject> list : lists) {
      list.remove(persistentObject);
    }
  }

  @Override
  public void insert(PersistentObject persistentObject) {
    if(persistentObject instanceof PersistenceTimingAware) {
      ((PersistenceTimingAware)persistentObject).insertedAt(System.currentTimeMillis());
    }
    
    if(persistentObject instanceof JobImpl) {
      allJobsList.add( (JobImpl)persistentObject );
      pendingJobsList.add( (JobImpl)persistentObject );
    } else {
      throw new IllegalArgumentException("Unsupported type " + persistentObject.getClass() + " received - " + persistentObject);
    }
  }


  // ======================== Jobs =================================
  
  
  @SuppressWarnings("unchecked")
  @Override
  public JobImpl findJob(long jobId) {
    for(JobImpl job : allJobsList) {
      if(job.getIdL() == jobId) {
        if(job instanceof PersistenceTimingAware) {
          ((PersistenceTimingAware)job).foundAt(System.currentTimeMillis());
        }
        return job;
      }
    }
    return null;
  }

  @Override
  public List<List<Long>> findPendingJobs(int limit) {
    List<List<Long>> ids = new ArrayList<List<Long>>();
    
    synchronized (pendingJobsList) {
      int fLimit = Math.min(limit, pendingJobsList.size());
      for(int i=0; i<fLimit; i++) {
        List<Long> jobIDs = new ArrayList<Long>();
        JobImpl job = pendingJobsList.remove(0);
        
        if(job instanceof GroupOfJobs) {
          for(JobImpl ij : ((GroupOfJobs)job).get()) {
            jobIDs.add( ij.getIdL() );
          }
        } else {
          jobIDs.add( job.getIdL() );
        }
        ids.add(jobIDs);
      }
    }
    
    return ids;
  }
  
  // ======================== The Rest =================================
  
  
  @Override
  public void deleteDeployment(String deploymentId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteGroup(String groupId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void createMembership(String userId, String groupId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteMembership(String userId, String groupId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteUser(String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public long dynamicFindProcessInstanceCount(Map<String, Object> params) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<ProcessInstance> dynamicFindProcessInstances(
      Map<String, Object> params) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long dynamicFindTaskCount(Map<String, Object> params) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<Task> dynamicFindTasks(Map<String, Object> params, Page page) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Task> findCandidateTasks(String userId, List<String> groupIds) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeploymentImpl findDeployment(String deploymentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeploymentImpl findDeploymentByProcessDefinitionId(
      String processDefinitionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ByteArrayImpl findDeploymentResource(String deploymentId,
      String resourceName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> findDeploymentResourceNames(String deploymentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ByteArrayImpl> findDeploymentResources(String deploymentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DeploymentImpl> findDeployments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExecutionDbImpl findExecution(String executionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExecutionDbImpl> findExecutionsByProcessDefintion(
      String processDefinitionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GroupImpl findGroup(String groupId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GroupImpl> findGroupsByUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GroupImpl> findGroupsByUserAndType(String userId, String groupType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessDefinitionImpl findLatestProcessDefinitionByKey(
      String processDefinitionKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessDefinitionImpl findProcessDefinitionById(
      String processDefinitionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProcessDefinitionImpl> findProcessDefinitions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProcessDefinitionImpl> findProcessDefinitionsByDeployment(
      String deploymentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TaskImpl findTask(String taskId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TaskInvolvement> findTaskInvolvementsByTask(String taskId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Task> findTasksByAssignee(String assignee) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TaskImpl> findTasksByExecution(String executionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UserImpl findUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public boolean isValidUser(String userId) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public List<UserImpl> findUsersByGroup(String groupId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getDeploymentResourceBytes(String resourceId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DbidBlock getNextDbidBlock() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void insertDeployment(DeploymentImpl deployment) {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveGroup(GroupImpl group) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveUser(UserImpl user) {
    // TODO Auto-generated method stub

  }


  @Override
  public ProcessDefinitionImpl findProcessDefinitionByDeploymentAndKey(
      String deploymentId, String processDefinitionKey) {
    // TODO Auto-generated method stub
    return null;
  }
}
