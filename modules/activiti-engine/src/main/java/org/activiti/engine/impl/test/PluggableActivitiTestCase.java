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

package org.activiti.engine.impl.test;

import java.io.InputStream;
import java.util.Properties;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;


/** Base class for the activiti test cases.
 * 
 * The main reason not to use our own test support classes is that we need to 
 * run our test suite with various configurations, e.g. with and without spring,
 * standalone or on a server etc.  Those requirements create some complications 
 * so we think it's best to use a separate base class.  That way it is much easier 
 * for us to maintain our own codebase and at the same time provide stability 
 * on the test support classes that we offer as part of our api (in org.activiti.engine.test).
 * 
 * @author Tom Baeyens
 */
public class PluggableActivitiTestCase extends AbstractActivitiTestCase {
  
  private static final ProcessEngineInitializer processEngineInitializer = getProcessEngineInitializer();

  protected static ProcessEngine cachedProcessEngine;

  private static ProcessEngineInitializer getProcessEngineInitializer() {
    String processEngineInitializerClassName = null;
    InputStream initializersInputStream = ReflectUtil.getResourceAsStream("activiti.initializer.properties");
    if (initializersInputStream!=null) {
      Properties properties = new Properties();
      try {
        properties.load(initializersInputStream);
        processEngineInitializerClassName = properties.getProperty("process.engine.initializer");
        if (processEngineInitializerClassName!=null) {
          return (ProcessEngineInitializer) ReflectUtil.instantiate(processEngineInitializerClassName);
        }
    
      } catch (Exception e) {
        throw new RuntimeException("couldn't instantiate process engine initializer "+properties+": "+e, e);
      } finally {
        IoUtil.closeSilently(initializersInputStream);
      }
    }
    return new DefaultProcessEngineInitializer();
  }

  protected void initializeProcessEngine() {
    if (cachedProcessEngine==null) {
      cachedProcessEngine = processEngineInitializer.getProcessEngine();
      if (cachedProcessEngine==null) {
        throw new ActivitiException("proces engine initializer '"+processEngineInitializer.getClass().getName()+"' didn't return a process engine");
      }
    }
    processEngine = cachedProcessEngine;
  }
}
