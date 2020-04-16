package org.activiti.standalone.jpa;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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

/**
 * Test for JPA enhancement support
 *
 */
public class JPAEnhancedVariableTest extends AbstractActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(JPAEnhancedVariableTest.class);
  private static EntityManagerFactory entityManagerFactory;
  protected static ProcessEngine cachedProcessEngine;

  private static FieldAccessJPAEntity fieldEntity;
  private static FieldAccessJPAEntity fieldEntity2;
  private static PropertyAccessJPAEntity propertyEntity;

  @Override
  protected void initializeProcessEngine() {
    if (cachedProcessEngine == null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
          .createProcessEngineConfigurationFromResource("org/activiti/standalone/jpa/activiti.cfg.xml");

      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();

      EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) processEngineConfiguration.getSessionFactories().get(EntityManagerSession.class);

      entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();

      setupJPAVariables();
    }
    processEngine = cachedProcessEngine;
  }

  private static void setupJPAVariables() {
    EntityManager em = entityManagerFactory.createEntityManager();
    em.getTransaction().begin();

    fieldEntity = new FieldAccessJPAEntity();
    fieldEntity.setId(1L);
    fieldEntity.setValue("fieldEntity");
    em.persist(fieldEntity);

    propertyEntity = new PropertyAccessJPAEntity();
    propertyEntity.setId(1L);
    propertyEntity.setValue("propertyEntity");
    em.persist(propertyEntity);

    em.flush();
    em.getTransaction().commit();
    em.close();

    // load enhanced versions of entities
    em = entityManagerFactory.createEntityManager();

    fieldEntity = em.find(FieldAccessJPAEntity.class, fieldEntity.getId());
    propertyEntity = em.find(PropertyAccessJPAEntity.class, propertyEntity.getId());

    em.getTransaction().begin();

    fieldEntity2 = new FieldAccessJPAEntity();
    fieldEntity2.setId(2L);
    fieldEntity2.setValue("fieldEntity2");
    em.persist(fieldEntity2);

    em.flush();
    em.getTransaction().commit();
    em.close();
  }

  private Task getTask(ProcessInstance instance) {
    return processEngine.getTaskService().createTaskQuery().processInstanceId(instance.getProcessInstanceId()).includeProcessVariables().singleResult();
  }

  @Deployment(resources = { "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml" })
  public void testEnhancedEntityVariables() throws Exception {
    // test if enhancement is used
    if (FieldAccessJPAEntity.class == fieldEntity.getClass() || PropertyAccessJPAEntity.class == propertyEntity.getClass()) {
      logger.warn("Entity enhancement is not used");
      return;
    }

    // start process with enhanced jpa variables
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fieldEntity", fieldEntity);
    params.put("propertyEntity", propertyEntity);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    Task task = getTask(instance);
    for (Map.Entry<String, Object> entry : task.getProcessVariables().entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      if (name.equals("fieldEntity")) {
        assertThat(value).isInstanceOf(FieldAccessJPAEntity.class);
      } else if (name.equals("propertyEntity")) {
        assertThat(value).isInstanceOf(PropertyAccessJPAEntity.class);
      } else {
        fail();
      }
    }
  }

  @Deployment(resources = { "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml" })
  public void testEnhancedEntityListVariables() throws Exception {
    // test if enhancement is used
    if (FieldAccessJPAEntity.class == fieldEntity.getClass() || PropertyAccessJPAEntity.class == propertyEntity.getClass()) {
      logger.warn("Entity enhancement is not used");
      return;
    }

    // start process with lists of enhanced jpa variables
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("list1", asList(fieldEntity, fieldEntity));
    params.put("list2", asList(propertyEntity, propertyEntity));
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    Task task = getTask(instance);
    List list = (List) task.getProcessVariables().get("list1");
    assertThat(list.size() == 2).isTrue();
    assertThat(list.get(0)).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(list.get(1)).isInstanceOf(FieldAccessJPAEntity.class);

    list = (List) task.getProcessVariables().get("list2");
    assertThat(list.size() == 2).isTrue();
    assertThat(list.get(0)).isInstanceOf(PropertyAccessJPAEntity.class);
    assertThat(list.get(1)).isInstanceOf(PropertyAccessJPAEntity.class);

    // start process with enhanced and persisted only jpa variables in the
    // same list
    params.putAll(singletonMap("list", asList(fieldEntity, fieldEntity2)));
    instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    task = getTask(instance);
    list = (List) task.getProcessVariables().get("list");
    assertThat(list.size() == 2).isTrue();
    assertThat(list.get(0)).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity) list.get(0)).getId().equals(1L)).isTrue();
    assertThat(list.get(1)).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity) list.get(1)).getId().equals(2L)).isTrue();

    // shuffle list and start a new process
    params.putAll(singletonMap("list", asList(fieldEntity2, fieldEntity)));
    instance = processEngine.getRuntimeService().startProcessInstanceByKey("JPAVariableProcess", params);

    task = getTask(instance);
    list = (List) task.getProcessVariables().get("list");
    assertThat(list.size() == 2).isTrue();
    assertThat(list.get(0)).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity) list.get(0)).getId().equals(2L)).isTrue();
    assertThat(list.get(1)).isInstanceOf(FieldAccessJPAEntity.class);
    assertThat(((FieldAccessJPAEntity) list.get(1)).getId().equals(1L)).isTrue();

    // start process with mixed jpa entities in list
    assertThatExceptionOfType(Exception.class).isThrownBy(() -> processEngine.getRuntimeService()
      .startProcessInstanceByKey("JPAVariableProcess", singletonMap("list", asList(fieldEntity, propertyEntity))));
  }
}
