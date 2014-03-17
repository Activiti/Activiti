package org.activiti.engine.test.db;

import java.sql.Connection;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import junit.framework.TestCase;

public class CreateDbSchemaWithPrefixTest extends TestCase {

	public void testCreateDatabaseWithTablePrefix(){
		// both process engines will be using this datasource.
	    PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(), 
	            "org.h2.Driver", 
	            "jdbc:h2:mem:activiti-test;DB_CLOSE_DELAY=1000", 
	            "sa", 
	            "" );

	    // configure process engine with table prefix 
	    ProcessEngineConfigurationImpl config1 = (ProcessEngineConfigurationImpl) ProcessEngineConfigurationImpl
	            .createStandaloneInMemProcessEngineConfiguration()
	            .setDataSource(pooledDataSource)
	            .setDatabaseSchemaUpdate("create-drop"); // create/drop schema
	    config1.setDatabaseTablePrefix("MyPrefix_");
	    ProcessEngine engine = config1.buildProcessEngine();
	    // test engine works
	    try {
	      engine.getRepositoryService()
	        .createDeployment()
	        .addClasspathResource("org/activiti/engine/test/db/oneJobProcess.bpmn20.xml")
	        .deploy();
	      assertEquals(1, engine.getRepositoryService().createDeploymentQuery().count());
	    } finally {
	      engine.close();
	    }
	    
	}
}
