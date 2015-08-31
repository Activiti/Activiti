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

package org.activiti.compatibility.spring;

import org.activiti.compatibility.DefaultActiviti5CompatibilityHandler;
import org.activiti.compatibility.DefaultProcessEngineFactory;
import org.activiti5.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DefaultActiviti5SpringCompatibilityHandler extends DefaultActiviti5CompatibilityHandler {

  @Override
  public DefaultProcessEngineFactory getProcessEngineFactory() {
    if (processEngineFactory == null) {
      processEngineFactory = new DefaultSpringProcessEngineFactory();
    }
    return processEngineFactory;
  }
  
  public Object getCamelContextObject(String camelContextValue) {
    SpringProcessEngineConfiguration springConfiguration = (SpringProcessEngineConfiguration) getProcessEngine().getProcessEngineConfiguration();
    if (StringUtils.isEmpty(camelContextValue)) {
      camelContextValue = springConfiguration.getDefaultCamelContext();
    }

    // Get the CamelContext object and set the super's member variable.
    Object ctx = springConfiguration.getApplicationContext().getBean(camelContextValue);
    return ctx;
  }
}
