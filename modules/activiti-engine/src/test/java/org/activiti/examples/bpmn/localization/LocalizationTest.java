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
package org.activiti.examples.bpmn.localization;

import java.util.List;
import java.util.Locale;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.apache.log4j.Logger;

/**
 * Example of using localization
 * 
 * @author Robert Hafner
 */
public class LocalizationTest extends PluggableActivitiTestCase {

  @Deployment(resources = {"org/activiti/examples/bpmn/localization/Localization.bpmn20.xml",
                           "org/activiti/examples/bpmn/localization/Localization_en_GB.l10n.properties"})
  public void testDefaultLocale() {
    Logger logger = Logger.getLogger(LocalizationTest.class);
    logger.warn("OK 123");
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("localization");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).locale(new Locale("en", "GB")).list();
    assertEquals(1, tasks.size());
    assertEquals("Colour Task", tasks.get(0).getName());
    assertEquals("Colour Task Description", tasks.get(0).getDescription());
  
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).locale(new Locale("en", "US")).list();
    assertEquals("Color Task", tasks.get(0).getName());
    assertEquals("Color Task Description", tasks.get(0).getDescription());
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).locale(new Locale("de")).list();
    assertEquals("Color Task", tasks.get(0).getName());
    assertEquals("Color Task Description", tasks.get(0).getDescription());
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals("Color Task", tasks.get(0).getName());
    assertEquals("Color Task Description", tasks.get(0).getDescription());
  }
}
