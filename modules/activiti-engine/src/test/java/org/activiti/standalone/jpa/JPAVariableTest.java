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

package org.activiti.standalone.jpa;



import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class JPAVariableTest extends AbstractActivitiTestCase {

  protected static ProcessEngine cachedProcessEngine;

  private static FieldAccessJPAEntity simpleEntityFieldAccess;
  private static PropertyAccessJPAEntity simpleEntityPropertyAccess;
  private static SubclassFieldAccessJPAEntity subclassFieldAccess;
  private static SubclassPropertyAccessJPAEntity subclassPropertyAccess;
  
  private static ByteIdJPAEntity byteIdJPAEntity;
  private static ShortIdJPAEntity shortIdJPAEntity;
  private static IntegerIdJPAEntity integerIdJPAEntity;
  private static LongIdJPAEntity longIdJPAEntity;
  private static FloatIdJPAEntity floatIdJPAEntity;
  private static DoubleIdJPAEntity doubleIdJPAEntity;
  private static CharIdJPAEntity charIdJPAEntity;
  private static StringIdJPAEntity stringIdJPAEntity;
  private static DateIdJPAEntity dateIdJPAEntity;
  private static SQLDateIdJPAEntity sqlDateIdJPAEntity;
  private static BigDecimalIdJPAEntity bigDecimalIdJPAEntity;
  private static BigIntegerIdJPAEntity bigIntegerIdJPAEntity;
  private static CompoundIdJPAEntity compoundIdJPAEntity;
  
  private static FieldAccessJPAEntity entityToQuery;
  private static FieldAccessJPAEntity entityToUpdate;
  
  private static boolean entitiesInitialized = false;

  private static EntityManagerFactory entityManagerFactory;
  
  protected void initializeProcessEngine() {
    if (cachedProcessEngine==null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("org/activiti/standalone/jpa/activiti.cfg.xml");
      
      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();

      EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) processEngineConfiguration
        .getSessionFactories()
        .get(EntityManagerSession.class);
      
      entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();
    }
    processEngine = cachedProcessEngine;
  }

  public void setupJPAEntities() {
    
    if(!entitiesInitialized) {
      
      EntityManager manager = entityManagerFactory.createEntityManager();
      manager.getTransaction().begin();
      
      // Simple test data
      simpleEntityFieldAccess = new FieldAccessJPAEntity();
      simpleEntityFieldAccess.setId(1L);
      simpleEntityFieldAccess.setValue("value1");
      manager.persist(simpleEntityFieldAccess);
  
      simpleEntityPropertyAccess = new PropertyAccessJPAEntity();
      simpleEntityPropertyAccess.setId(1L);
      simpleEntityPropertyAccess.setValue("value2");
      manager.persist(simpleEntityPropertyAccess);
  
      subclassFieldAccess = new SubclassFieldAccessJPAEntity();
      subclassFieldAccess.setId(1L);
      subclassFieldAccess.setValue("value3");
      manager.persist(subclassFieldAccess);
  
      subclassPropertyAccess = new SubclassPropertyAccessJPAEntity();
      subclassPropertyAccess.setId(1L);
      subclassPropertyAccess.setValue("value4");
      manager.persist(subclassPropertyAccess);
  
      // Test entities with all possible ID types
      byteIdJPAEntity = new ByteIdJPAEntity();
      byteIdJPAEntity.setByteId((byte)1);
      manager.persist(byteIdJPAEntity);
      
      shortIdJPAEntity = new ShortIdJPAEntity();
      shortIdJPAEntity.setShortId((short)123);
      manager.persist(shortIdJPAEntity);
      
      integerIdJPAEntity = new IntegerIdJPAEntity();
      integerIdJPAEntity.setIntId(123);
      manager.persist(integerIdJPAEntity);
      
      longIdJPAEntity = new LongIdJPAEntity();
      longIdJPAEntity.setLongId(123456789L);
      manager.persist(longIdJPAEntity);
      
      floatIdJPAEntity = new FloatIdJPAEntity();
      floatIdJPAEntity.setFloatId((float) 123.45678);
      manager.persist(floatIdJPAEntity);
      
      doubleIdJPAEntity = new DoubleIdJPAEntity();
      doubleIdJPAEntity.setDoubleId(12345678.987654);
      manager.persist(doubleIdJPAEntity);
         
      charIdJPAEntity = new CharIdJPAEntity();
      charIdJPAEntity.setCharId('g');
      manager.persist(charIdJPAEntity);
      
      dateIdJPAEntity = new DateIdJPAEntity();
      dateIdJPAEntity.setDateId(new java.util.Date());
      manager.persist(dateIdJPAEntity);
      
      sqlDateIdJPAEntity = new SQLDateIdJPAEntity();
      sqlDateIdJPAEntity.setDateId(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
      manager.persist(sqlDateIdJPAEntity);
      
      stringIdJPAEntity = new StringIdJPAEntity();
      stringIdJPAEntity.setStringId("azertyuiop");
      manager.persist(stringIdJPAEntity);
      
      bigDecimalIdJPAEntity = new BigDecimalIdJPAEntity();
      bigDecimalIdJPAEntity.setBigDecimalId(new BigDecimal("12345678912345678900000.123456789123456789"));
      manager.persist(bigDecimalIdJPAEntity);
      
      bigIntegerIdJPAEntity = new BigIntegerIdJPAEntity();
      bigIntegerIdJPAEntity.setBigIntegerId(new BigInteger("12345678912345678912345678900000"));
      manager.persist(bigIntegerIdJPAEntity);
      
      manager.flush();
      manager.getTransaction().commit();
      manager.close();
      
      entitiesInitialized = true;
    }
  }
  
  public void setupIllegalJPAEntities() {
    EntityManager manager = entityManagerFactory.createEntityManager();
    manager.getTransaction().begin();
    
    compoundIdJPAEntity = new CompoundIdJPAEntity();
    EmbeddableCompoundId id = new EmbeddableCompoundId();
    id.setIdPart1(123L);
    id.setIdPart2("part2");
    compoundIdJPAEntity.setId(id);
    manager.persist(compoundIdJPAEntity);
    
    manager.flush();
    manager.getTransaction().commit();
    manager.close();
  }
  
  public void setupQueryJPAEntity() {
    EntityManager manager = entityManagerFactory.createEntityManager();
    manager.getTransaction().begin();
    
    entityToQuery = new FieldAccessJPAEntity();
    entityToQuery.setId(2L);
    manager.persist(entityToQuery);
    
    manager.flush();
    manager.getTransaction().commit();
    manager.close();
  }
  
  public void setupJPAEntityToUpdate() {
    EntityManager manager = entityManagerFactory.createEntityManager();
    manager.getTransaction().begin();
    
    entityToUpdate = new FieldAccessJPAEntity();
    entityToUpdate.setId(3L);
    manager.persist(entityToUpdate);
    manager.flush();
    manager.getTransaction().commit();
    manager.close();
  }
  
  @Deployment
  public void testStoreJPAEntityAsVariable() {
    setupJPAEntities();
    // -----------------------------------------------------------------------------
    // Simple test, Start process with JPA entities as variables
    // -----------------------------------------------------------------------------
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("simpleEntityFieldAccess", simpleEntityFieldAccess);
    variables.put("simpleEntityPropertyAccess", simpleEntityPropertyAccess);
    variables.put("subclassFieldAccess", subclassFieldAccess);
    variables.put("subclassPropertyAccess", subclassPropertyAccess);
    
    // Start the process with the JPA-entities as variables. They will be stored in the DB.
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
    
    // Read entity with @Id on field
    Object fieldAccessResult = runtimeService.getVariable(processInstance.getId(), "simpleEntityFieldAccess");
    assertTrue(fieldAccessResult instanceof FieldAccessJPAEntity);
    assertEquals(1L, ((FieldAccessJPAEntity)fieldAccessResult).getId().longValue());
    assertEquals("value1", ((FieldAccessJPAEntity)fieldAccessResult).getValue());
    
    // Read entity with @Id on property
    Object propertyAccessResult = runtimeService.getVariable(processInstance.getId(), "simpleEntityPropertyAccess");
    assertTrue(propertyAccessResult instanceof PropertyAccessJPAEntity);
    assertEquals(1L, ((PropertyAccessJPAEntity)propertyAccessResult).getId().longValue());
    assertEquals("value2", ((PropertyAccessJPAEntity)propertyAccessResult).getValue());
    
    // Read entity with @Id on field of mapped superclass 
    Object subclassFieldResult = runtimeService.getVariable(processInstance.getId(), "subclassFieldAccess");
    assertTrue(subclassFieldResult instanceof SubclassFieldAccessJPAEntity);
    assertEquals(1L, ((SubclassFieldAccessJPAEntity)subclassFieldResult).getId().longValue());
    assertEquals("value3", ((SubclassFieldAccessJPAEntity)subclassFieldResult).getValue());
    
    // Read entity with @Id on property of mapped superclass 
    Object subclassPropertyResult = runtimeService.getVariable(processInstance.getId(), "subclassPropertyAccess");
    assertTrue(subclassPropertyResult instanceof SubclassPropertyAccessJPAEntity);
    assertEquals(1L, ((SubclassPropertyAccessJPAEntity)subclassPropertyResult).getId().longValue());
    assertEquals("value4", ((SubclassPropertyAccessJPAEntity)subclassPropertyResult).getValue());
    
    // -----------------------------------------------------------------------------
    // Test updating JPA-entity to null-value and back again
    // -----------------------------------------------------------------------------
    Object currentValue = runtimeService.getVariable(processInstance.getId(), "simpleEntityFieldAccess");
    assertNotNull(currentValue);
    // Set to null
    runtimeService.setVariable(processInstance.getId(), "simpleEntityFieldAccess", null);
    currentValue = runtimeService.getVariable(processInstance.getId(), "simpleEntityFieldAccess");
    assertNull(currentValue);    
    // Set to JPA-entity again
    runtimeService.setVariable(processInstance.getId(), "simpleEntityFieldAccess", simpleEntityFieldAccess);
    currentValue = runtimeService.getVariable(processInstance.getId(), "simpleEntityFieldAccess");
    assertNotNull(currentValue);
    assertTrue(currentValue instanceof FieldAccessJPAEntity);
    assertEquals(1L, ((FieldAccessJPAEntity)currentValue).getId().longValue());
    
    
    // -----------------------------------------------------------------------------
    // Test all allowed types of ID values
    // -----------------------------------------------------------------------------
    
    variables = new HashMap<String, Object>();
    variables.put("byteIdJPAEntity", byteIdJPAEntity);
    variables.put("shortIdJPAEntity", shortIdJPAEntity);
    variables.put("integerIdJPAEntity", integerIdJPAEntity);
    variables.put("longIdJPAEntity", longIdJPAEntity);
    variables.put("floatIdJPAEntity", floatIdJPAEntity);
    variables.put("doubleIdJPAEntity", doubleIdJPAEntity);
    variables.put("charIdJPAEntity", charIdJPAEntity);
    variables.put("stringIdJPAEntity", stringIdJPAEntity);
    variables.put("dateIdJPAEntity", dateIdJPAEntity);
    variables.put("sqlDateIdJPAEntity", sqlDateIdJPAEntity);
    variables.put("bigDecimalIdJPAEntity", bigDecimalIdJPAEntity);
    variables.put("bigIntegerIdJPAEntity", bigIntegerIdJPAEntity);
    
    // Start the process with the JPA-entities as variables. They will be stored in the DB.
    ProcessInstance processInstanceAllTypes = runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
    Object byteIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "byteIdJPAEntity");
    assertTrue(byteIdResult instanceof ByteIdJPAEntity);
    assertEquals(byteIdJPAEntity.getByteId(), ((ByteIdJPAEntity)byteIdResult).getByteId());
    
    Object shortIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "shortIdJPAEntity");
    assertTrue(shortIdResult instanceof ShortIdJPAEntity);
    assertEquals(shortIdJPAEntity.getShortId(), ((ShortIdJPAEntity)shortIdResult).getShortId());
    
    Object integerIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "integerIdJPAEntity");
    assertTrue(integerIdResult instanceof IntegerIdJPAEntity);
    assertEquals(integerIdJPAEntity.getIntId(), ((IntegerIdJPAEntity)integerIdResult).getIntId());
    
    Object longIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "longIdJPAEntity");
    assertTrue(longIdResult instanceof LongIdJPAEntity);
    assertEquals(longIdJPAEntity.getLongId(), ((LongIdJPAEntity)longIdResult).getLongId());
    
    Object floatIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "floatIdJPAEntity");
    assertTrue(floatIdResult instanceof FloatIdJPAEntity);
    assertEquals(floatIdJPAEntity.getFloatId(), ((FloatIdJPAEntity)floatIdResult).getFloatId());
    
    Object doubleIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "doubleIdJPAEntity");
    assertTrue(doubleIdResult instanceof DoubleIdJPAEntity);
    assertEquals(doubleIdJPAEntity.getDoubleId(), ((DoubleIdJPAEntity)doubleIdResult).getDoubleId());
    
    Object charIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "charIdJPAEntity");
    assertTrue(charIdResult instanceof CharIdJPAEntity);
    assertEquals(charIdJPAEntity.getCharId(), ((CharIdJPAEntity)charIdResult).getCharId());
    
    Object stringIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "stringIdJPAEntity");
    assertTrue(stringIdResult instanceof StringIdJPAEntity);
    assertEquals(stringIdJPAEntity.getStringId(), ((StringIdJPAEntity)stringIdResult).getStringId());
    
    Object dateIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "dateIdJPAEntity");
    assertTrue(dateIdResult instanceof DateIdJPAEntity);
    assertEquals(dateIdJPAEntity.getDateId(), ((DateIdJPAEntity)dateIdResult).getDateId());
    
    Object sqlDateIdResult = runtimeService.getVariable(processInstanceAllTypes.getId(), "sqlDateIdJPAEntity");
    assertTrue(sqlDateIdResult instanceof SQLDateIdJPAEntity);
    assertEquals(sqlDateIdJPAEntity.getDateId(), ((SQLDateIdJPAEntity)sqlDateIdResult).getDateId());
    
    Object bigDecimalIdResult= runtimeService.getVariable(processInstanceAllTypes.getId(), "bigDecimalIdJPAEntity");
    assertTrue(bigDecimalIdResult instanceof BigDecimalIdJPAEntity);
    assertEquals(bigDecimalIdJPAEntity.getBigDecimalId(), ((BigDecimalIdJPAEntity)bigDecimalIdResult).getBigDecimalId());
    
    Object bigIntegerIdResult= runtimeService.getVariable(processInstanceAllTypes.getId(), "bigIntegerIdJPAEntity");
    assertTrue(bigIntegerIdResult instanceof BigIntegerIdJPAEntity);
    assertEquals(bigIntegerIdJPAEntity.getBigIntegerId(), ((BigIntegerIdJPAEntity)bigIntegerIdResult).getBigIntegerId());
  }
  
  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml"
  })
  public void testStoreJPAEntityListAsVariable() {
    setupJPAEntities();
    // -----------------------------------------------------------------------------
    // Simple test, Start process with lists of JPA entities as variables
    // -----------------------------------------------------------------------------
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("simpleEntityFieldAccess", Arrays.asList(simpleEntityFieldAccess, simpleEntityFieldAccess, simpleEntityFieldAccess));
    variables.put("simpleEntityPropertyAccess", Arrays.asList(simpleEntityPropertyAccess, simpleEntityPropertyAccess, simpleEntityPropertyAccess));
    variables.put("subclassFieldAccess", Arrays.asList(subclassFieldAccess, subclassFieldAccess, subclassFieldAccess));
    variables.put("subclassPropertyAccess", Arrays.asList(subclassPropertyAccess, subclassPropertyAccess, subclassPropertyAccess));
    
    // Start the process with the JPA-entities as variables. They will be stored in the DB.
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
    
    // Read entity with @Id on field
    Object fieldAccessResult = runtimeService.getVariable(processInstance.getId(), "simpleEntityFieldAccess");
    assertTrue(fieldAccessResult instanceof List<?>);
    List<?> list = (List<?>) fieldAccessResult;
    assertEquals(3L, list.size());
    assertTrue(list.get(0) instanceof FieldAccessJPAEntity);
    assertEquals(((FieldAccessJPAEntity)list.get(0)).getId(), simpleEntityFieldAccess.getId() );
    
    // Read entity with @Id on property
    Object propertyAccessResult = runtimeService.getVariable(processInstance.getId(), "simpleEntityPropertyAccess");
    assertTrue(propertyAccessResult instanceof List<?>);
    list = (List<?>) propertyAccessResult;
    assertEquals(3L, list.size());
    assertTrue(list.get(0) instanceof PropertyAccessJPAEntity);
    assertEquals(((PropertyAccessJPAEntity)list.get(0)).getId(), simpleEntityPropertyAccess.getId());
    
    // Read entity with @Id on field of mapped superclass 
    Object subclassFieldResult = runtimeService.getVariable(processInstance.getId(), "subclassFieldAccess");
    assertTrue(subclassFieldResult instanceof List<?>);
    list = (List<?>) subclassFieldResult;
    assertEquals(3L, list.size());
    assertTrue(list.get(0) instanceof SubclassFieldAccessJPAEntity);
    assertEquals(((SubclassFieldAccessJPAEntity)list.get(0)).getId(), simpleEntityPropertyAccess.getId());
    
    
    // Read entity with @Id on property of mapped superclass 
    Object subclassPropertyResult = runtimeService.getVariable(processInstance.getId(), "subclassPropertyAccess");
    assertTrue(subclassPropertyResult instanceof List<?>);
    list = (List<?>) subclassPropertyResult;
    assertEquals(3L, list.size());
    assertTrue(list.get(0) instanceof SubclassPropertyAccessJPAEntity);
    assertEquals(((SubclassPropertyAccessJPAEntity)list.get(0)).getId(), simpleEntityPropertyAccess.getId());
  }
  
  @Deployment(resources = {
      "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml"
  })
  public void testStoreJPAEntityListAsVariableEdgeCases() {
    setupJPAEntities();
    
    // Test using mixed JPA-entities which are not serializable, should not be picked up by JPA list type en therefor fail
    // due to serialization error
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("simpleEntityFieldAccess", Arrays.asList(simpleEntityFieldAccess, simpleEntityPropertyAccess));

    try {
      runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
      fail("Exception expected");
    } catch(ActivitiException ae ) {
      // Expected
    }
    
    // Test updating value to an empty list and back
    variables = new HashMap<String, Object>();
    variables.put("list", Arrays.asList(simpleEntityFieldAccess, simpleEntityFieldAccess));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
    
    runtimeService.setVariable(processInstance.getId(), "list", new ArrayList<String>());
    assertEquals(0L, ((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).size());
    
    runtimeService.setVariable(processInstance.getId(), "list", Arrays.asList(simpleEntityFieldAccess, simpleEntityFieldAccess));
    assertEquals(2L, ((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).size());
    assertTrue(((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).get(0) instanceof FieldAccessJPAEntity);
    
    // Test updating to list of Strings
    runtimeService.setVariable(processInstance.getId(), "list", Arrays.asList("TEST", "TESTING"));
    assertEquals(2L, ((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).size());
    assertTrue(((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).get(0) instanceof String);
    
    runtimeService.setVariable(processInstance.getId(), "list", Arrays.asList(simpleEntityFieldAccess, simpleEntityFieldAccess));
    assertEquals(2L, ((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).size());
    assertTrue(((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).get(0) instanceof FieldAccessJPAEntity);
    
    // Test updating to null
    runtimeService.setVariable(processInstance.getId(), "list", null);
    assertNull(runtimeService.getVariable(processInstance.getId(), "list"));
    
    runtimeService.setVariable(processInstance.getId(), "list", Arrays.asList(simpleEntityFieldAccess, simpleEntityFieldAccess));
    assertEquals(2L, ((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).size());
    assertTrue(((List<?>) runtimeService.getVariable(processInstance.getId(), "list")).get(0) instanceof FieldAccessJPAEntity);
  }
  
  // https://activiti.atlassian.net/browse/ACT-995
  @Deployment(resources="org/activiti/standalone/jpa/JPAVariableTest.testQueryJPAVariable.bpmn20.xml")
  public void testReplaceExistingJPAEntityWithAnotherOfSameType() {
    EntityManager manager = entityManagerFactory.createEntityManager();
    manager.getTransaction().begin();
    
    // Old variable that gets replaced
    FieldAccessJPAEntity oldVariable = new FieldAccessJPAEntity();
    oldVariable.setId(11L);
    oldVariable.setValue("value1");
    manager.persist(oldVariable);
    
    // New  variable
    FieldAccessJPAEntity newVariable = new FieldAccessJPAEntity();
    newVariable.setId(12L);
    newVariable.setValue("value2");
    manager.persist(newVariable);
    
    manager.flush();
    manager.getTransaction().commit();
    manager.close();
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcess");
    
    String executionId = processInstance.getId();
    String variableName = "testVariable";
    runtimeService.setVariable(executionId, variableName, oldVariable);

    runtimeService.setVariable(executionId, variableName, newVariable);
    
    Object variable = runtimeService.getVariable(executionId, variableName);
    assertEquals(newVariable.getId(), ((FieldAccessJPAEntity) variable).getId());
  }
  
  @Deployment
  public void testIllegalEntities() {
    setupIllegalJPAEntities();
    // Starting process instance with a variable that has a compound primary key, which is not supported.
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("compoundIdJPAEntity", compoundIdJPAEntity);
    
    try {
      runtimeService.startProcessInstanceByKey("JPAVariableProcessExceptions", variables);
      fail("Exception expected");
    } catch(ActivitiException ae) {
      assertTextPresent("Cannot find field or method with annotation @Id on class", ae.getMessage());
      assertTextPresent("only single-valued primary keys are supported on JPA-enities", ae.getMessage());
    }
    
    // Starting process instance with a variable that has null as ID-value
    variables = new HashMap<String, Object>();
    variables.put("nullValueEntity", new FieldAccessJPAEntity());
    
    try {
      runtimeService.startProcessInstanceByKey("JPAVariableProcessExceptions", variables);
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Value of primary key for JPA-Entity cannot be null", ae.getMessage());
    }
    
    // Starting process instance with an invalid type of ID
    // Under normal circumstances, JPA will throw an exception for this of the class is 
    // present in the PU when creating EntityanagerFactory, but we test it *just in case*
    variables = new HashMap<String, Object>();
    IllegalIdClassJPAEntity illegalIdTypeEntity = new IllegalIdClassJPAEntity();
    illegalIdTypeEntity.setId(Calendar.getInstance());
    variables.put("illegalTypeId", illegalIdTypeEntity);
    
    try {
      runtimeService.startProcessInstanceByKey("JPAVariableProcessExceptions", variables);
      fail("Exception expected");
    } catch(ActivitiException ae) {
      assertTextPresent("Unsupported Primary key type for JPA-Entity", ae.getMessage());
    }
    
    // Start process instance with JPA-entity which has an ID but isn't persisted. When reading
    // the variable we should get an exception.
    variables = new HashMap<String, Object>();
    FieldAccessJPAEntity nonPersistentEntity = new FieldAccessJPAEntity();
    nonPersistentEntity.setId(9999L);
    variables.put("nonPersistentEntity", nonPersistentEntity);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcessExceptions", variables);
    
    try {
      runtimeService.getVariable(processInstance.getId(), "nonPersistentEntity");
      fail("Exception expected");
    } catch(ActivitiException ae) {
      assertTextPresent("Entity does not exist: " + FieldAccessJPAEntity.class.getName() + " - 9999", ae.getMessage());
    }
  }
  
  @Deployment
  public void testQueryJPAVariable() {
    setupQueryJPAEntity();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("entityToQuery", entityToQuery);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("JPAVariableProcess", variables);
    
    // Query the processInstance
    ProcessInstance result = runtimeService.createProcessInstanceQuery().variableValueEquals("entityToQuery", entityToQuery).singleResult();
    assertNotNull(result);
    assertEquals(result.getId(), processInstance.getId());
    
    // Query with the same entity-type but with different ID should have no result
    FieldAccessJPAEntity unexistingEntity = new FieldAccessJPAEntity();
    unexistingEntity.setId(8888L);
    
    result = runtimeService.createProcessInstanceQuery().variableValueEquals("entityToQuery", unexistingEntity).singleResult();
    assertNull(result);
    
    // All other operators are unsupported
    try {
      runtimeService.createProcessInstanceQuery().variableValueNotEquals("entityToQuery", entityToQuery).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("JPA entity variables can only be used in 'variableValueEquals'", ae.getMessage());
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan("entityToQuery", entityToQuery).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("JPA entity variables can only be used in 'variableValueEquals'", ae.getMessage());
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("entityToQuery", entityToQuery).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("JPA entity variables can only be used in 'variableValueEquals'", ae.getMessage());
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan("entityToQuery", entityToQuery).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("JPA entity variables can only be used in 'variableValueEquals'", ae.getMessage());
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("entityToQuery", entityToQuery).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("JPA entity variables can only be used in 'variableValueEquals'", ae.getMessage());
    }
  }
  
  @Deployment
  public void testUpdateJPAEntityValues() {
    setupJPAEntityToUpdate();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("entityToUpdate", entityToUpdate);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateJPAValuesProcess", variables);
    
    // Servicetask in process 'UpdateJPAValuesProcess' should have set value on entityToUpdate.
    Object updatedEntity = runtimeService.getVariable(processInstance.getId(), "entityToUpdate");
    assertTrue(updatedEntity instanceof FieldAccessJPAEntity);
    assertEquals("updatedValue", ((FieldAccessJPAEntity)updatedEntity).getValue());
  }
}
