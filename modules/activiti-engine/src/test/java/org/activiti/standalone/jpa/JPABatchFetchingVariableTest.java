package org.activiti.standalone.jpa;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Test for JPA batch-fetching support
 *
 * @author <a href="mailto:eugene.khrustalev@gmail.com">Eugene Khrustalev</a>
 */
public class JPABatchFetchingVariableTest extends AbstractActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(JPABatchFetchingVariableTest.class);

  protected static ProcessEngine cachedProcessEngine;
  protected static Stack<String> calledMethodsWithArgs = new Stack<String>();

  private static List<StringIdJPAEntity> stringEntities = new ArrayList<StringIdJPAEntity>();
  private static List<IntegerIdJPAEntity> integerEntities = new ArrayList<IntegerIdJPAEntity>();

  @Override
  protected void initializeProcessEngine() {
    if (cachedProcessEngine == null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
          .createProcessEngineConfigurationFromResource("org/activiti/standalone/jpa/activiti.cfg.xml");

      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();

      EntityManagerSessionFactory sessionFactory = (EntityManagerSessionFactory) processEngineConfiguration
          .getSessionFactories()
          .get(EntityManagerSession.class);

      EntityManagerFactory entityManagerFactory = sessionFactory.getEntityManagerFactory();
      // Replace process engine configuration entityManagerFactory by proxy object
      entityManagerFactory = (EntityManagerFactory) Proxy.newProxyInstance(getClass().getClassLoader(),
          new Class[]{EntityManagerFactory.class},
          new EntityManagerFactoryInvocationHandler(entityManagerFactory));
      processEngineConfiguration.setJpaPersistenceUnitName(null);
      processEngineConfiguration.setJpaEntityManagerFactory(entityManagerFactory);
      // ... and rebuild process engine
      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();

      setupJPAVariables();
    }
    processEngine = cachedProcessEngine;
  }

  private EntityManager getEntityManager() {
    EntityManagerFactory factory = (EntityManagerFactory) cachedProcessEngine
        .getProcessEngineConfiguration()
        .getJpaEntityManagerFactory();
    return factory.createEntityManager();
  }

  private void setupJPAVariables() {
    EntityManager em = getEntityManager();
    em.getTransaction().begin();

    for (int i = 0; i < 100; i++) {
      StringIdJPAEntity stringEntity = new StringIdJPAEntity();
      stringEntity.setStringId("string" + i);
      em.persist(stringEntity);
      stringEntities.add(stringEntity);

      IntegerIdJPAEntity integerEntity = new IntegerIdJPAEntity();
      integerEntity.setIntId(i);
      em.persist(integerEntity);
      integerEntities.add(integerEntity);
    }

    em.flush();
    em.getTransaction().commit();
    em.close();
  }

  private Task getTask(ProcessInstance instance) {
    return processEngine.getTaskService()
        .createTaskQuery()
        .processInstanceId(instance.getProcessInstanceId())
        .includeProcessVariables()
        .singleResult();
  }

  private List<Task> getTasks(ProcessInstance instance) {
    return processEngine.getTaskService()
        .createTaskQuery()
        .processInstanceId(instance.getProcessInstanceId())
        .includeProcessVariables()
        .list();
  }

  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPABatchFetchingVariableTest.emptyProcess.bpmn20.xml"
  })
  public void testEmptyProcess() throws Exception {
    calledMethodsWithArgs.clear();

    Map params = new HashMap();
    params.put("stringEntities", stringEntities);
    params.put("integerEntities", integerEntities);
    for (int i = 0; i < stringEntities.size(); i++) {
      params.put("stringEntity" + i, stringEntities.get(i));
      params.put("integerEntity" + i, integerEntities.get(i));
    }
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPABatchFetchingProcess", params);
    //assertTrue("Empty process should not query any variable", calledMethodsWithArgs.size() == 0);
  }

  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml"
  })
  public void testFetchingJpaEntityList() throws Exception {
    calledMethodsWithArgs.clear();

    Map params = new HashMap();
    params.put("stringEntities", stringEntities);
    params.put("integerEntities", integerEntities);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    calledMethodsWithArgs.clear();

    Task task = getTask(instance);
    // 1. query for stringEntities list
    // 2. query for integerEntities list
    assertTrue(calledMethodsWithArgs.size() == 2);

    assertEquals(calledMethodsWithArgs.get(0),
        "createQuery('select e from " + StringIdJPAEntity.class.getName() + " e where e.stringId in (:ids)')");
    assertEquals(calledMethodsWithArgs.get(1),
        "createQuery('select e from " + IntegerIdJPAEntity.class.getName() + " e where e.intId in (:ids)')");
  }

  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml"
  })
  public void testFetchingJpaEntities() throws Exception {
    calledMethodsWithArgs.clear();

    Map params = new HashMap();
    params.put("stringEntities", stringEntities);
    params.put("integerEntities", integerEntities);
    for (int i = 0; i < stringEntities.size(); i++) {
      params.put("stringEntity" + i, stringEntities.get(i));
      params.put("integerEntity" + i, integerEntities.get(i));
    }
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    calledMethodsWithArgs.clear();

    Task task = getTask(instance);
    // 1. query for stringEntities list
    // 2. query for integerEntities list
    // 3. query for integerEntityXX entities
    // 4. query form stringEntityXX entities
    assertTrue(calledMethodsWithArgs.size() == 4);

    stringEntities = (List<StringIdJPAEntity>) task.getProcessVariables().get("stringEntities");
    integerEntities = (List<IntegerIdJPAEntity>) task.getProcessVariables().get("integerEntities");
    for (int i = 0; i < 100; i++) {
      assertEquals(stringEntities.get(i).getStringId(), "string"+i);
      assertEquals(integerEntities.get(i).getIntId(), i);
    }
  }

  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPABatchFetchingVariableTest.complexProcess.bpmn20.xml"
  })
  public void testFetchingJpaEntitiesComplex() throws Exception {
    calledMethodsWithArgs.clear();

    Map params = new HashMap();
    params.put("stringList", Arrays.asList(stringEntities.get(0), stringEntities.get(1)));
    params.put("integerEntities", Arrays.asList(integerEntities.get(0), integerEntities.get(1)));
    for (int i = 0; i < 5; i++) {
      params.put("stringEntity" + i, stringEntities.get(i));
      params.put("integerEntity" + i, integerEntities.get(i));
    }
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPABatchFetchingComplexProcess", params);

    calledMethodsWithArgs.clear();

    final List<Task> tasks = getTasks(instance);
    // (jpa-entity-list count * task count) + (jpa-entity class count)
    assertTrue(calledMethodsWithArgs.size() == 6);
  }

  /**
   * {@link javax.persistence.EntityManagerFactory} proxy class to provide custom {@link javax.persistence.EntityManager}
   */
  public static class EntityManagerFactoryInvocationHandler implements InvocationHandler {

    private final EntityManagerFactory factory;

    public EntityManagerFactoryInvocationHandler(EntityManagerFactory factory) {
      this.factory = factory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object result = method.invoke(factory, args);
      if (method.getName().equals("createEntityManager")) {
        final Object entityManager = result;
        // return proxy instead of real entity manager
        return Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{EntityManager.class}, new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(entityManager, args);
                if (method.getName().equals("find") || method.getName().equals("createQuery")) {
                  StringBuilder builder = new StringBuilder(method.getName());
                  builder.append("(");
                  if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                      if (i > 0) builder.append(", ");
                      if(args[i] instanceof String) {
                        builder.append("'").append(args[i]).append("'");
                      } else {
                        builder.append(args[i]);
                      }
                    }
                  }
                  builder.append(")");
                  logger.debug("{}", builder);
                  calledMethodsWithArgs.push(builder.toString());
                }
                // wrap Query object
                if(result instanceof Query) {
                  final Query query = (Query) result;
                  result = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Query.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                      Object result = method.invoke(query, args);
                      if(!method.getName().equals("toString")) {
                        logger.debug("{}({})", method.getName(), args == null ? "" : args);
                        if (method.getName().equals("getResultList")) {
                          logger.debug("Found {} entities", ((Collection) result).size());
                        }
                      }
                      return result;
                    }
                  });
                }
                return result;
              }
            });
      }
      return result;
    }
  }
}
