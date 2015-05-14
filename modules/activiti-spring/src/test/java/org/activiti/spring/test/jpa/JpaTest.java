package org.activiti.spring.test.jpa;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Frederik Heremans
 */
@ContextConfiguration( locations =  "JPASpringTest-context.xml")
public class JpaTest extends SpringActivitiTestCase {

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
        String[] defs = {"org/activiti/spring/test/jpa/JPASpringTest.bpmn20.xml"};
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
/*

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
class JpaConfiguration {

    @Bean LoanRequestBean loanRequestBean (){
        return new LoanRequestBean();
    }
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
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /// restoring manual JPA bits

    @Bean
    public SpringProcessEngineConfiguration activitiConfiguration(
            ResourcePatternResolver resourcePatternResolver,
            DataSource dataSource,
            PlatformTransactionManager transactionManager ) throws IOException {

        Resource[] resources = resourcePatternResolver.getResources("classpath://*/
/**.bpmn20.xml");

        SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration();
        if (resources != null && resources.length > 0) {
            engine.setDeploymentResources(resources);
        }
        engine.setDataSource(dataSource);
        engine.setTransactionManager(transactionManager);


        */
/*
        conf.setDeploymentName(defaultText(
                activitiProperties.getDeploymentName(),
                conf.getDeploymentName()));
        conf.setDatabaseSchema(defaultText(
                activitiProperties.getDatabaseSchema(),
                conf.getDatabaseSchema()));
        *//*


        engine.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        return engine;
    }

    @Bean
    public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration configuration) throws Exception {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration(configuration);
        return processEngineFactoryBean;
    }
}

	*/
/*@Bean
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
}
 */
