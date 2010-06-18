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
package org.activiti.impl.cfg;

import java.io.IOException;
import java.io.Reader;

import org.activiti.ActivitiException;
import org.activiti.impl.xml.Element;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


/**
 * @author Joram Barrez
 */
public class SqlSessionFactoryBinding implements ConfigurationBinding {
  
  private static final String TAG_NAME = "sql-session-factory";

  public boolean matches(Element element, ConfigurationParse configurationParse) {
    return TAG_NAME.equals(element.getTagName());
  }

  public Object parse(Element element, ConfigurationParse configurationParse) {
    try {
      Reader reader = Resources.getResourceAsReader(getResource(element));
      SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
      SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
      return sqlSessionFactory;
    } catch (IOException e) {
      throw new ActivitiException("Error while reading " + TAG_NAME + " resource", e);
    }
  }
  
  protected String getResource(Element element) {
    String resource = element.attribute("resource");
    if (resource == null || resource.isEmpty()) {
      throw new ActivitiException("Null or empty resource for " + TAG_NAME);
    }
    return resource;
  }

//  public Object parse(Element element, ConfigurationParse configurationParse) {
//    Map<String, Object> configurations = configurationParse.getConfiguration().getConfigurations();
//    String jdbcUrl = (String) configurations.get("JdbcUrl");
//    String jdbcDriver = (String) configurations.get("JdbcDriver");
//    String jdbcUsername = (String) configurations.get("JdbcUsername");
//    String jdbcPassword = (String) configurations.get("JdbcPassword");
//    
//    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    PooledDataSource dataSource = new PooledDataSource(contextClassLoader, jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);
//    TransactionFactory transactionFactory = new JdbcTransactionFactory(); 
//    Environment environment = new Environment("development", transactionFactory, dataSource);
//    Configuration configuration = new Configuration(environment); 
//    addMapping(configuration, "org/activiti/db/ibatis/deployment.mapping.xml");
//    addMapping(configuration, "org/activiti/db/ibatis/engine.mapping.xml");
//    addMapping(configuration, "org/activiti/db/ibatis/execution.mapping.xml");
//    addMapping(configuration, "org/activiti/db/ibatis/identity.mapping.xml");
//    addMapping(configuration, "org/activiti/db/ibatis/processdefinition.mapping.xml");
//    addMapping(configuration, "org/activiti/db/ibatis/task.mapping.xml");
//    
//    SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
//    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);
//
//    return sqlSessionFactory;
//  }
//
//  private void addMapping(Configuration configuration, String resource) {
//    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    InputStream inputStream = contextClassLoader.getResourceAsStream(resource);
//    InputStreamReader reader = new InputStreamReader(inputStream);
//    XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, resource, new HashMap<String, XNode>());
//    mapperParser.parse();
//  }

}
