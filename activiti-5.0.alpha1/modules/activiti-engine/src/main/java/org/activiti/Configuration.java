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
package org.activiti;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.cfg.ConfigurationParser;
import org.activiti.impl.util.ReflectUtil;


/**
 * loads configuration from a file and is then used to build a {@link org.activiti.ProcessEngine}.
 * 
 * Typical usage
 * <pre>ProcessEngine processEngine = 
 *     new Configuration()
 *     .configurationResource("activiti.cfg.xml")
 *     .buildProcessEngine();</pre>
 * 
 * 
 * @see ProcessEngines
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class Configuration {
  
  private static Logger log = Logger.getLogger(Configuration.class.getName());
  
  public static final String NAME_COMMANDEXECUTOR = "CommandExecutor";
  public static final String NAME_DBSCHEMA = "DbSchema";
  public static final String NAME_IBATISSQLSESSIONFACTORY = "IbatisSqlSessionFactory";
  public static final String NAME_DBIDGENERATOR = "DbidGenerator";
  public static final String NAME_DEPLOYERMANAGER = "DeployerManager";
  public static final String NAME_PROCESSCACHE = "ProcessCache";
  public static final String NAME_PERSISTENCETYPEISSQL = "PersistenceTypeIsSQL";
  public static final String NAME_TRANSACTIONALOBJECTDESCRIPTORS = "TransactionalObjectDescriptors";
  public static final String NAME_JOBEXECUTOR = "JobExecutor";
  
  protected String name = null;
  protected String type = null;
  protected Map<String, Object> configurations = new HashMap<String, Object>();
  
  public Configuration() {
    log.info("activiti version "+ProcessEngine.VERSION);
  }
  
  public Configuration configurationObject(String name, Object object) {
    configurations.put(name, object);
    return this;
  }

  public Configuration configurationResource(String resourceName) {
    ConfigurationParser.INSTANCE
      .createParse()
      .configuration(this)
      .sourceResource(resourceName)
      .execute();

    return this;
  }
  
  public Configuration configurationUrl(URL url) {
    ConfigurationParser.INSTANCE
      .createParse()
      .configuration(this)
      .sourceUrl(url)
      .execute();
  
    return this;
  }


  public ProcessEngine buildProcessEngine() {
    if (type!=null) {
      return ReflectUtil.instantiate(type, new Object[]{this});
    }
    return new ProcessEngineImpl(this);
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Map<String, Object> getConfigurations() {
    return configurations;
  }
  public void setConfigurations(Map<String, Object> configurations) {
    this.configurations = configurations;
  }

}
