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

package org.activiti.spring.test.jpa;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.EnableActiviti;
import org.activiti.spring.annotations.AbstractActivitiConfigurer;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openjpa.jdbc.sql.H2Dictionary;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Test using spring-orm in spring-bean combined with JPA-variables in activiti.
 * 
 * @author Frederik Heremans
 * @author Josh Long
 */
@ContextConfiguration(classes = JPAConfiguration.class)
public class JPASpringTest extends SpringActivitiTestCase {

	@Test
	public void testJpaVariableHappyPath() {
		before();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("customerName", "John Doe");
		variables.put("amount", 15000L);

		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey("LoanRequestProcess", variables);

		// Variable should be present containing the loanRequest created by the
		// spring bean
		Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
		assertNotNull(value);
		assertTrue(value instanceof LoanRequest);
		LoanRequest request = (LoanRequest) value;
		assertEquals("John Doe", request.getCustomerName());
		assertEquals(15000L, request.getAmount().longValue());
		assertFalse(request.isApproved());

		// We will approve the request, which will update the entity
		variables = new HashMap<String, Object>();
		variables.put("approvedByManager", Boolean.TRUE);

		Task task = taskService.createTaskQuery()
		    .processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(task);
		taskService.complete(task.getId(), variables);

		// If approved, the processsInstance should be finished, gateway based on loanRequest.approved value
		assertEquals(0, runtimeService.createProcessInstanceQuery()
		    .processInstanceId(processInstance.getId()).count());

		// Cleanup
		deleteDeployments();
	}

	@Test
	public void testJpaVariableDisapprovalPath() {

		before();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("customerName", "Jane Doe");
		variables.put("amount", 50000);

		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey("LoanRequestProcess", variables);

		// Variable should be present containing the loanRequest created by the
		// spring bean
		Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
		assertNotNull(value);
		assertTrue(value instanceof LoanRequest);
		LoanRequest request = (LoanRequest) value;
		assertEquals("Jane Doe", request.getCustomerName());
		assertEquals(50000L, request.getAmount().longValue());
		assertFalse(request.isApproved());

		// We will disapprove the request, which will update the entity
		variables = new HashMap<String, Object>();
		variables.put("approvedByManager", Boolean.FALSE);

		Task task = taskService.createTaskQuery()
		    .processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(task);
		taskService.complete(task.getId(), variables);

		runtimeService.getVariable(processInstance.getId(), "loanRequest");
		request = (LoanRequest) value;
		assertFalse(request.isApproved());

		// If disapproved, an extra task will be available instead of the process
		// ending
		task = taskService.createTaskQuery()
		    .processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(task);
		assertEquals("Send rejection letter", task.getName());

		// Cleanup
		deleteDeployments();
	}

	protected void before() {
		String[] defs = { "org/activiti/spring/test/jpa/JPASpringTest.bpmn20.xml" };
		for (String pd : defs)
			repositoryService.createDeployment().addClasspathResource(pd).deploy();
	}

	protected void deleteDeployments() {
		for (Deployment deployment : repositoryService.createDeploymentQuery()
		    .list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}

}

@Configuration
@EnableActiviti
@EnableTransactionManagement(proxyTargetClass = true)
class JPAConfiguration {

	@Bean
	public OpenJpaVendorAdapter openJpaVendorAdapter() {
		OpenJpaVendorAdapter openJpaVendorAdapter = new OpenJpaVendorAdapter();
		openJpaVendorAdapter.setDatabasePlatform(H2Dictionary.class.getName());
		return openJpaVendorAdapter;
	}

	@Bean
	public DataSource dataSource() {
		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setUsername("sa");
		basicDataSource.setUrl("jdbc:h2:mem:activiti");
		basicDataSource.setDefaultAutoCommit(false);
		basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
		basicDataSource.setPassword("");
		return basicDataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
	    OpenJpaVendorAdapter openJpaVendorAdapter, DataSource ds) {
		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setPersistenceXmlLocation("classpath:/org/activiti/spring/test/jpa/custom-persistence.xml");
		emf.setJpaVendorAdapter(openJpaVendorAdapter);
		emf.setDataSource(ds);
		return emf;
	}

	@Bean
	public PlatformTransactionManager jpaTransactionManager(
	    EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public AbstractActivitiConfigurer abstractActivitiConfigurer(
	    final EntityManagerFactory emf,
	    final PlatformTransactionManager transactionManager) {

		return new AbstractActivitiConfigurer() {

			@Override
			public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration engine) {
				engine.setTransactionManager(transactionManager);
				engine.setJpaEntityManagerFactory(emf);
				engine.setJpaHandleTransaction(false);
				engine.setJobExecutorActivate(false);
				engine.setJpaCloseEntityManager(false);
				engine.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
			}
		};
	}

	@Bean
	public LoanRequestBean loanRequestBean() {
		return new LoanRequestBean();
	}
} // end of @Configuration

