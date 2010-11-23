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
package org.activiti.spring.impl.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


/**
 * @author Joram Barrez
 */
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
public class ActivitiInternalSpringTestCase extends ActivitiInternalTestCase implements ApplicationContextAware {
  
  protected TestContextManager testContextManager;
  
  @Autowired
  protected ApplicationContext applicationContext;
  
  public ActivitiInternalSpringTestCase() {
    super();
    this.testContextManager = new TestContextManager(getClass());
  }
  
  @Override
  public void runBare() throws Throwable {
    testContextManager.prepareTestInstance(this); // this will initialize all dependencies
    try {
      ActivitiInternalTestCase.processEngine = applicationContext.getBean(ProcessEngine.class);
      super.runBare();
    } finally {
      ActivitiInternalTestCase.processEngine = null;
    }
  }
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  
}
