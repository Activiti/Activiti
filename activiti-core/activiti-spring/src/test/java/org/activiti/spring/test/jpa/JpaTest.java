/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.test.jpa;

import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.activiti.engine.impl.util.CollectionUtil.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 */
@ContextConfiguration(locations = "JPASpringTest-context.xml")
public class JpaTest extends SpringActivitiTestCase {

  public void testJpaVariableHappyPath() {
    before();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", map(
      "customerName", "John Doe",
      "amount", 15000L
    ));

    // Variable should be present containing the loanRequest created by the spring bean
    Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertThat(value).isNotNull();
    assertThat(value).isInstanceOf(LoanRequest.class);
    LoanRequest request = (LoanRequest) value;
    assertThat(request.getCustomerName()).isEqualTo("John Doe");
    assertThat(request.getAmount().longValue()).isEqualTo(15000L);
    assertThat(request.isApproved()).isFalse();

    // We will approve the request, which will update the entity
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId(), singletonMap("approvedByManager", Boolean.TRUE));

    // If approved, the processsInstance should be finished, gateway based on loanRequest.approved value
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    // Cleanup
    deleteDeployments();
  }

  public void testJpaVariableDisapprovalPath() {

    before();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", map(
      "customerName", "Jane Doe",
      "amount", 50000
    ));

    // Variable should be present containing the loanRequest created by the spring bean
    Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertThat(value).isNotNull();
    assertThat(value).isInstanceOf(LoanRequest.class);
    LoanRequest request = (LoanRequest) value;
    assertThat(request.getCustomerName()).isEqualTo("Jane Doe");
    assertThat(request.getAmount().longValue()).isEqualTo(50000L);
    assertThat(request.isApproved()).isFalse();

    // We will disapprove the request, which will update the entity
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId(), singletonMap("approvedByManager", Boolean.FALSE));

    runtimeService.getVariable(processInstance.getId(), "loanRequest");
    request = (LoanRequest) value;
    assertThat(request.isApproved()).isFalse();

    // If disapproved, an extra task will be available instead of the process ending
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("Send rejection letter");

    // Cleanup
    deleteDeployments();
  }

  protected void before() {
    String[] defs = { "org/activiti/spring/test/jpa/JPASpringTest.bpmn20.xml" };
    for (String pd : defs)
      repositoryService.createDeployment().addClasspathResource(pd).deploy();
  }

  protected void deleteDeployments() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }
}
/*
 *
 * @Configuration
 *
 * @EnableTransactionManagement(proxyTargetClass = true) class JpaConfiguration {
 *
 * @Bean LoanRequestBean loanRequestBean (){ return new LoanRequestBean(); }
 *
 * @Bean public OpenJpaVendorAdapter openJpaVendorAdapter() { OpenJpaVendorAdapter openJpaVendorAdapter = new OpenJpaVendorAdapter();
 * openJpaVendorAdapter.setDatabasePlatform(H2Dictionary.class.getName()); return openJpaVendorAdapter; }
 *
 * @Bean public DataSource dataSource() { BasicDataSource basicDataSource = new BasicDataSource(); basicDataSource.setUsername("sa"); basicDataSource.setUrl("jdbc:h2:mem:activiti");
 * basicDataSource.setDefaultAutoCommit(false); basicDataSource.setDriverClassName(org.h2.Driver.class.getName()); basicDataSource.setPassword(""); return basicDataSource; }
 *
 * @Bean public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean( OpenJpaVendorAdapter openJpaVendorAdapter, DataSource ds) { LocalContainerEntityManagerFactoryBean emf = new
 * LocalContainerEntityManagerFactoryBean(); emf.setPersistenceXmlLocation( "classpath:/org/activiti/spring/test/jpa/custom-persistence.xml"); emf.setJpaVendorAdapter(openJpaVendorAdapter);
 * emf.setDataSource(ds); return emf; }
 *
 * @Bean public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
 *
 * /// restoring manual JPA bits
 *
 * @Bean public SpringProcessEngineConfiguration activitiConfiguration( ResourcePatternResolver resourcePatternResolver, DataSource dataSource, PlatformTransactionManager transactionManager ) throws
 * IOException {
 *
 * Resource[] resources = resourcePatternResolver.getResources("classpath://
 */
/**
 * .bpmn20.xml");
 *
 * SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration(); if (resources != null && resources.length > 0) { engine.setDeploymentResources(resources); }
 * engine.setDataSource(dataSource); engine.setTransactionManager(transactionManager);
 */
/*
 * conf.setDeploymentName(defaultText( activitiProperties.getDeploymentName(), conf.getDeploymentName())); conf.setDatabaseSchema(defaultText( activitiProperties.getDatabaseSchema(),
 * conf.getDatabaseSchema()));
 *//*
    *
    *
    * engine.setDatabaseSchemaUpdate(ProcessEngineConfiguration. DB_SCHEMA_UPDATE_TRUE);
    *
    * return engine; }
    *
    * @Bean public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration configuration) throws Exception { ProcessEngineFactoryBean processEngineFactoryBean = new
    * ProcessEngineFactoryBean(); processEngineFactoryBean.setProcessEngineConfiguration(configuration); return processEngineFactoryBean; } }
    */
/*
 * @Bean public AbstractActivitiConfigurer abstractActivitiConfigurer( final EntityManagerFactory emf, final PlatformTransactionManager transactionManager) {
 *
 * return new AbstractActivitiConfigurer() {
 *
 * @Override public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration engine) { engine.setTransactionManager(transactionManager);
 * engine.setJpaEntityManagerFactory(emf); engine.setJpaHandleTransaction(false); engine.setJobExecutorActivate(false); engine.setJpaCloseEntityManager(false);
 * engine.setDatabaseSchemaUpdate(ProcessEngineConfiguration .DB_SCHEMA_UPDATE_TRUE); } }; }
 *
 * @Bean public LoanRequestBean loanRequestBean() { return new LoanRequestBean(); } }
 */
