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
package org.activiti.migration.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.def.Task;


/**
 * @author Joram Barrez
 */
@SuppressWarnings("unchecked")
public class Jbpm3DaoImpl implements Jbpm3Dao {
  
  protected SessionFactory sessionFactory;
  
  public static String NODES_FOR_PROCESSDEFINITION_QUERY =
    "select node from org.jbpm.graph.def.Node node where node.processDefinition = :processDefinition";
  
  public static String TASK_FOR_TASKNODE_QUERY = 
    "select task from org.jbpm.taskmgmt.def.Task task "
    + "inner join fetch task.taskNode "
    + "where task.taskNode = :taskNode";
  
  public static String TRANSITION_QUERY = 
    "select transition from org.jbpm.graph.def.Transition transition "
      + "inner join fetch transition.from "
      + "inner join fetch transition.to "
      + " where transition.from = :node";
  
  public List<ProcessDefinition> getAllProcessDefinitions() {
    return executeInSession(new HibernateCallback<List<ProcessDefinition>>() {
      public List<ProcessDefinition> execute(Session session) {
        return session.createCriteria(ProcessDefinition.class).list();
      }
    });
  }
  
  public List<Node> getNodes(final ProcessDefinition processDefinition) {
    return executeInSession(new HibernateCallback<List<Node>>() {
      public List<Node> execute(Session session) {
        return session.createQuery(NODES_FOR_PROCESSDEFINITION_QUERY).setEntity("processDefinition", processDefinition).list();
      }
    });
  }
  
  public List<Task> getTasks(final TaskNode taskNode) {
    return executeInSession(new HibernateCallback<List<Task>>() {
      public List<Task> execute(Session session) {
        return session.createQuery(TASK_FOR_TASKNODE_QUERY).setEntity("taskNode", taskNode).list();
      }
    });
  }
  
  public List<Transition> getOutgoingTransitions(final Node node) {
    return executeInSession(new HibernateCallback<List<Transition>>() {
      public List<Transition> execute(Session session) {
        return session.createQuery(TRANSITION_QUERY).setEntity("node", node).list();
      }
    });
  }
  
  public <T> T executeInSession(HibernateCallback<T> hibernateCallback) {
    Session session = null;
    try {
       session = sessionFactory.openSession();
       return hibernateCallback.execute(session);
    } finally {
      if (session != null && session.isOpen()) {
        session.close();
      }
    }
  }
  
  public void close() {
    sessionFactory.close();
  }

  // Getters and setters //////////////////////////////////////////////////////////////////////////
  
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
  
  
  // Inner classes ///////////////////////////////////////////////////////////////////////////////
  
  interface HibernateCallback<T> {
    T execute(Session session);
  }

}
