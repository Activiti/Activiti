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
package org.activiti.impl.spring;

import java.util.logging.Logger;

import org.activiti.Configuration;
import org.activiti.impl.ProcessEngineImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;


/**
 * @author Tom Baeyens
 */
public class SpringProcessManagerFactory extends ProcessEngineImpl implements ApplicationContextAware, ApplicationListener {
  
  private static Logger log = Logger.getLogger(SpringProcessManagerFactory.class.getName());

  protected ApplicationContext applicationContext;
  
  public SpringProcessManagerFactory(Configuration configuration) {
    super(configuration);
  }
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    log.info("spring application event "+applicationEvent);
    if (applicationEvent instanceof ContextStoppedEvent) {
      close();
    }
  }
}
