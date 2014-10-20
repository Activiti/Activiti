package org.activiti.rest.conf.jpa;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories({"org.activiti.rest.api.jpa.repository"})
@EnableTransactionManagement
public class DatabaseConfiguration {

  @Bean(name="entityManagerFactory")
  public EntityManagerFactory entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setDataSource(dataSource());
    entityManagerFactoryBean.setPackagesToScan("org.activiti.rest.api.jpa.model");
    entityManagerFactoryBean.setPersistenceUnitName("test");
    HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
    hibernateJpaVendorAdapter.setShowSql(false);
    hibernateJpaVendorAdapter.setGenerateDdl(true);
    hibernateJpaVendorAdapter.setDatabase(Database.H2);
    entityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
    entityManagerFactoryBean.afterPropertiesSet();
    return entityManagerFactoryBean.getObject();
  }
  
  @Bean
  public DataSource dataSource() { 
    SimpleDriverDataSource ds = new SimpleDriverDataSource();
    ds.setDriverClass(org.h2.Driver.class);
    
    // Connection settings
    ds.setUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
    ds.setUsername("sa");
    
    return ds;
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory());
    return transactionManager;
  }
}
