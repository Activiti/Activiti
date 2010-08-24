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

package org.activiti.engine.test;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.pvm.impl.util.LogUtil.ThreadLogMode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;




/**
 * @author Tom Baeyens
 */
public class SpringProcessEngineTestCase extends ProcessEngineTestCase {

  private static Logger log = Logger.getLogger(SpringProcessEngineTestCase.class.getName());
  
  protected String springApplicationContextConfigurationResource;

  protected ApplicationContext applicationContext;

  public SpringProcessEngineTestCase() {
    if (getResourceAsStream("applicationContext.xml")!=null) {
      this.springApplicationContextConfigurationResource = "applicationContext.xml";
      this.configurationResource = null;
    }
  }

  public SpringProcessEngineTestCase(String springApplicationContextConfigurationResource) {
    this.springApplicationContextConfigurationResource = springApplicationContextConfigurationResource;
    this.configurationResource = null;
  }

  InputStream getResourceAsStream(String resourceName) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
  }

  void initializeProcessEngine() {
    if (configurationResource!=null) {
      super.initializeProcessEngine();
    } else if (springApplicationContextConfigurationResource!=null) {
      log.fine("==== BUILDING SPRING APPLICATION CONTEXT AND PROCESS ENGINE =========================================");
      
      this.applicationContext = new ClassPathXmlApplicationContext(springApplicationContextConfigurationResource);
      Map<String, ProcessEngine> beansOfType = applicationContext.getBeansOfType(ProcessEngine.class);
      if ( (beansOfType==null)
           || (beansOfType.isEmpty())
         ) {
        throw new ActivitiException("no "+ProcessEngine.class.getName()+" defined in the application context "+springApplicationContextConfigurationResource);
      }
      
      processEngine = beansOfType.values().iterator().next();
      log.fine("==== SPRING PROCESS ENGINE CREATED ==================================================================");
    }
  }
}
