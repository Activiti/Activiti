package org.activiti.cycle.impl.db.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;

public abstract class AbstractCycleDaoMyBatisImpl {

	private static HashMap<String, DbSqlSessionFactory> dbFactories = new HashMap<String, DbSqlSessionFactory>();
	protected String processEngineName = ProcessEngines.NAME_DEFAULT;
	protected static String DEFAULT_ENGINE = "DEFAULT_PROCESS_ENGINE";

	private static Logger log = Logger.getLogger(AbstractCycleDaoMyBatisImpl.class.getName());

	protected SqlSessionFactory getSessionFactory() {
		if (dbFactories.get(processEngineName) == null) {
			synchronized (dbFactories) {
				// lazy initialization, only done once per proces engine!
				if (dbFactories.get(processEngineName) == null) {

					ProcessEngineImpl engineImpl = (ProcessEngineImpl) ProcessEngines.getProcessEngine(processEngineName);
					ProcessEngineConfigurationImpl processEngineConfiguration = engineImpl.getProcessEngineConfiguration();

					DataSource dataSource = processEngineConfiguration.getDataSource();
					TransactionFactory transactionFactory = processEngineConfiguration.getTransactionFactory();
					SqlSessionFactory sqlSessionFactory = createSessionFactory(dataSource, transactionFactory);

					DbSqlSessionFactory factory = new DbSqlSessionFactory();
					factory.setDatabaseType(processEngineConfiguration.getDatabaseType());
					factory.setIdGenerator(processEngineConfiguration.getIdGenerator());
					factory.setSqlSessionFactory(sqlSessionFactory);

					dbFactories.put(processEngineName, factory);
				}
			}
		}
		return dbFactories.get(processEngineName).getSqlSessionFactory();
	}

	public SqlSessionFactory createSessionFactory(DataSource dataSource, TransactionFactory transactionFactory) {
		InputStream inputStream = null;
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			inputStream = classLoader.getResourceAsStream("org/activiti/db/cycle/ibatis/activiti.ibatis.mem.conf.xml");

			// update the jdbc parameters to the configured ones...
			Environment environment = new Environment("default", transactionFactory, dataSource);
			Reader reader = new InputStreamReader(inputStream);
			XMLConfigBuilder parser = new XMLConfigBuilder(reader);
			Configuration configuration = parser.getConfiguration();
			configuration.setEnvironment(environment);
			configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
			configuration = parser.parse();

			return new DefaultSqlSessionFactory(configuration);

		} catch (Exception e) {
			throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
		} finally {
			IoUtil.closeSilently(inputStream);
		}
	}

	protected SqlSession openSession() {
		SqlSessionFactory sqlMapper = getSessionFactory();
		return sqlMapper.openSession();
	}

}
