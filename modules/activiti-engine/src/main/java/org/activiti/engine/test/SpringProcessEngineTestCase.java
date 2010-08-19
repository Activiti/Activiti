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




/**
 * @author Tom Baeyens
 */
public class SpringProcessEngineTestCase extends ProcessEngineTestCase {

//  private void initializeProcessEngineFromSpringConfigurationResource() {
//    log.fine("==== BUILDING SPRING APPLICATION CONTEXT AND PROCESS ENGINE =========================================");
//    
//    ApplicationContext applicationContext = new ClassPathXmlApplicationContext(springApplicationContextConfigurationResource);
//    Map<String, ProcessEngine> beansOfType = applicationContext.getBeansOfType(ProcessEngine.class);
//    if ( (beansOfType==null)
//         || (beansOfType.isEmpty())
//       ) {
//      throw new ActivitiException("no "+ProcessEngine.class.getName()+" defined in the application context "+springApplicationContextConfigurationResource);
//    }
//    
//    this.springApplicationContext = applicationContext;
//    
//    processEngine = beansOfType.values().iterator().next();
//    log.fine("==== SPRING PROCESS ENGINE CREATED ==================================================================");
//  }
//  protected Object getSpringBean(String springBeanName) {
//    // detyped storage of the application context and casting here is done to prevent a hard dependency of spring classes 
//    // on the classpath in case spring is not actually used.
//    return ((ApplicationContext)springApplicationContext).getBean(springBeanName);
//  }


}
