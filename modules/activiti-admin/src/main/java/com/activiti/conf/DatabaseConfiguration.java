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
package com.activiti.conf;

import com.activiti.domain.generator.MinimalDataGenerator;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.ejb.HibernatePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("com.activiti.repository")
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        log.info("Configuring Datasource");

        String dataSourceJndiName = env.getProperty("datasource.jndi.name");
        if (StringUtils.isNotEmpty(dataSourceJndiName)) {

            log.info("Using jndi datasource '" + dataSourceJndiName + "'");
            JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
            dsLookup.setResourceRef(env.getProperty("datasource.jndi.resourceRef", Boolean.class, Boolean.TRUE));
            DataSource dataSource = dsLookup.getDataSource(dataSourceJndiName);
            return dataSource;

        } else {

            String dataSourceDriver = env.getProperty("datasource.driver", "org.h2.Driver");
            String dataSourceUrl = env.getProperty("datasource.url", "jdbc:h2:mem:activitiadmin;DB_CLOSE_DELAY=-1");

            String dataSourceUsername = env.getProperty("datasource.username", "sa");
            String dataSourcePassword = env.getProperty("datasource.password", "");

            Integer minPoolSize = env.getProperty("datasource.min-pool-size", Integer.class);
            if (minPoolSize == null) {
                minPoolSize = 5;
            }

            Integer maxPoolSize = env.getProperty("datasource.max-pool-size", Integer.class);
            if (maxPoolSize == null) {
                maxPoolSize = 20;
            }

            Integer acquireIncrement = env.getProperty("datasource.acquire-increment", Integer.class);
            if (acquireIncrement == null) {
                acquireIncrement = 1;
            }

            String preferredTestQuery = env.getProperty("datasource.preferred-test-query");

            Boolean testConnectionOnCheckin = env.getProperty("datasource.test-connection-on-checkin", Boolean.class);
            if (testConnectionOnCheckin == null) {
                testConnectionOnCheckin = true;
            }

            Boolean testConnectionOnCheckOut = env.getProperty("datasource.test-connection-on-checkout", Boolean.class);
            if (testConnectionOnCheckOut == null) {
                testConnectionOnCheckOut = true;
            }

            Integer maxIdleTime = env.getProperty("datasource.max-idle-time", Integer.class);
            if (maxIdleTime == null) {
                maxIdleTime = 1800;
            }

            Integer maxIdleTimeExcessConnections = env.getProperty("datasource.max-idle-time-excess-connections", Integer.class);
            if (maxIdleTimeExcessConnections == null) {
                maxIdleTimeExcessConnections = 1800;
            }

            if (log.isInfoEnabled()) {
                log.info("Configuring Datasource with following properties (omitted password for security)");
                log.info("datasource driver: " + dataSourceDriver);
                log.info("datasource url : " + dataSourceUrl);
                log.info("datasource user name : " + dataSourceUsername);
                log.info("Min pool size | Max pool size | acquire increment : " + minPoolSize + " | " + maxPoolSize + " | " + acquireIncrement);
            }

            ComboPooledDataSource ds = new ComboPooledDataSource();
            try {
                ds.setDriverClass(dataSourceDriver);
            } catch (PropertyVetoException e) {
                log.error("Could not set Jdbc Driver class", e);
                return null;
            }

            // Connection settings
            ds.setJdbcUrl(dataSourceUrl);
            ds.setUser(dataSourceUsername);
            ds.setPassword(dataSourcePassword);

            // Pool config: see http://www.mchange.com/projects/c3p0/#configuration
            ds.setMinPoolSize(minPoolSize);
            ds.setMaxPoolSize(maxPoolSize);
            ds.setAcquireIncrement(acquireIncrement);
            if (preferredTestQuery != null) {
                ds.setPreferredTestQuery(preferredTestQuery);
            }
            ds.setTestConnectionOnCheckin(testConnectionOnCheckin);
            ds.setTestConnectionOnCheckout(testConnectionOnCheckOut);
            ds.setMaxIdleTimeExcessConnections(maxIdleTimeExcessConnections);
            ds.setMaxIdleTime(maxIdleTime);

            return ds;
        }
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        log.debug("Configuring EntityManager");
        LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
        lcemfb.setPersistenceProvider(new HibernatePersistence());
        lcemfb.setPersistenceUnitName("persistenceUnit");
        lcemfb.setDataSource(dataSource());
        lcemfb.setJpaDialect(new HibernateJpaDialect());
        lcemfb.setJpaVendorAdapter(jpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.cache.use_second_level_cache", false);
        jpaProperties.put("hibernate.generate_statistics", env.getProperty("hibernate.generate_statistics", Boolean.class, false));
        lcemfb.setJpaProperties(jpaProperties);

        lcemfb.setPackagesToScan("com.activiti.domain");
        lcemfb.afterPropertiesSet();
        return lcemfb.getObject();
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setShowSql(env.getProperty("hibernate.show_sql", Boolean.class, false));
        jpaVendorAdapter.setDatabasePlatform(env.getProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
        return jpaVendorAdapter;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
        return jpaTransactionManager;
    }

    @Bean(name="liquibase")
    public SpringLiquibase liquibase() {
        log.debug("Configuring Liquibase");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setChangeLog("classpath:META-INF/liquibase/db-changelog.xml");
        liquibase.setContexts("development, production");
        return liquibase;
    }

    @Bean(name="minimalDataGenerator")
    public MinimalDataGenerator minimalDataGenerator() {
    	return new MinimalDataGenerator();
    }
}

