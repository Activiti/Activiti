/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.spring.boot;

import javax.persistence.EntityManagerFactory;

import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * Auto-configuration and starter for the Activiti + JPA integration
 *
 */
@Configuration
@AutoConfigureAfter( BasicProcessEngineAutoConfiguration.class)
public class ProcessEngineJpaAutoConfiguration {

	@Configuration
    @ConditionalOnExpression("${spring.activiti.isJpaEnabled:true}")
    @ConditionalOnClass(value={EntityManagerFactory.class})
    public static class JpaIntegrationAutoConfiguration {

        @Autowired
        private ActivitiProperties activitiProperties;
        
        @Autowired
        private EntityManagerFactory emf;
        
        @Autowired
        private SpringProcessEngineConfiguration processEngineAutoConfiguration;
        
        @Primary
        @ConditionalOnMissingBean
        @Bean
        public PlatformTransactionManager jpaTransactionManager() {
        	return new JpaTransactionManager(emf);
        }
        
        @Primary // We need to inject the entitymanagerFactory and transactionmanager first, before construction the engine
        @Bean
        public ProcessEngineFactoryBean processEngine() throws Exception { // configureJpaForActiviti() -> hack to get the order right (c
        	System.out.println("BLAH - JPA");
        	processEngineAutoConfiguration.setJpaEntityManagerFactory(emf);
        	processEngineAutoConfiguration.setJpaHandleTransaction(false);
        	processEngineAutoConfiguration.setJpaCloseEntityManager(false);
        	processEngineAutoConfiguration.setTransactionManager(jpaTransactionManager());
        	
            ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
            processEngineFactoryBean.setProcessEngineConfiguration(processEngineAutoConfiguration);
            return processEngineFactoryBean;
        }

//        @Bean
//        public InitializingBean configureJpaForActiviti() {
//            return new InitializingBean() {
//
//                @Override
//                public void afterPropertiesSet() throws Exception {
//                    if (activitiProperties.isJpaEnabled()) {
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	System.out.println("TEST2");
//                    	processEngineAutoConfiguration.setJpaEntityManagerFactory(emf);
//                    	processEngineAutoConfiguration.setJpaHandleTransaction(false);
//                    	processEngineAutoConfiguration.setJpaCloseEntityManager(false);
//                    	processEngineAutoConfiguration.setTransactionManager(jpaTransactionManager());
//                    	
//                    	// TODO: build order? Are we sure the Process Engine gets built AFTER this?
////                    	processEngineAutoConfiguration.initJpaAfterProcessEngineIsBuilt(emf, false, false);
//                    }
//                }
//            };
//        }
    }
}
