package org.activiti.cycle.impl.conf;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.variable.Type;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;


public class CycleDbSqlSessionFactory extends DbSqlSessionFactory {
  
  private static Logger log = Logger.getLogger(CycleDbSqlSessionFactory.class.getName());

  @Override
  public void dbSchemaCheckVersion() {
    // TODO: Introduce proper version checking for cycle tables as well
    // ignore it for now
  }

  @Override
  protected SqlSessionFactory createSessionFactory(DataSource dataSource, TransactionFactory transactionFactory) {
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("org/activiti/db/cycle/ibatis/activiti.ibatis.mem.conf.xml");

      // update the jdbc parameters to the configured ones...
      Environment environment = new Environment("default", transactionFactory, dataSource);
      Reader reader = new InputStreamReader(inputStream);
      XMLConfigBuilder parser = new XMLConfigBuilder(reader);
      Configuration configuration = parser.getConfiguration();
      configuration.setEnvironment(environment);
      configuration.getTypeHandlerRegistry().register(Type.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
      configuration = parser.parse();
      
      return new DefaultSqlSessionFactory(configuration);

    } catch (Exception e) {
      throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
    }
  }
  
  @Override
  public void dbSchemaCreate() {
    executeSchemaResource("create", databaseName, sqlSessionFactory);
  }

  @Override
  public void dbSchemaDrop() {
    executeSchemaResource("drop", databaseName, sqlSessionFactory);
  }
  
  public static void executeSchemaResource(String operation, String databaseName, SqlSessionFactory sqlSessionFactory) {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    boolean success = false;
    try {
      Connection connection = sqlSession.getConnection();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String resource = "org/activiti/db/cycle/" + operation + "/activiti." + databaseName + "." + operation + ".sql";
      InputStream inputStream = classLoader.getResourceAsStream(resource);
      if (inputStream == null) {
        throw new ActivitiException("resource '" + resource + "' is not available for creating the schema");
      }

      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resource);
      String ddlStatements = new String(bytes);
      StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
      while (tokenizer.hasMoreTokens()) {
        String ddlStatement = tokenizer.nextToken().trim();
        if (!ddlStatement.startsWith("#")) {
          Statement jdbcStatement = connection.createStatement();
          try {
            log.finest("\n" + ddlStatement);
            jdbcStatement.execute(ddlStatement);
            jdbcStatement.close();
          } catch (Exception e) {
            if (exception == null) {
              exception = e;
            }
            log.log(Level.SEVERE, "problem during schema " + operation + ", statement '" + ddlStatement, e);
          }
        }
      }

      if (exception != null) {
        throw exception;
      }

      success = true;

    } catch (Exception e) {
      throw new ActivitiException("couldn't create db schema", e);

    } finally {
      if (success) {
        sqlSession.commit(true);
      } else {
        sqlSession.rollback(true);
      }
      sqlSession.close();
    }

    log.fine("activiti cycle db schema " + operation + " successful");
  }



}
