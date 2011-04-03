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
package org.activiti.cdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandInterceptor;

/**
 * We need to do it like this, until http://jira.codehaus.org/browse/ACT-715 is
 * resolved. Then we can make CDI support pluggable and configurable for any
 * configuration.
 * 
 * @author Daniel Meyer
 */
public class CdiJtaProcessEngineConfiguration extends JtaProcessEngineConfiguration {

  @Override
  protected void initExpressionManager() {
    expressionManager = new CdiExpressionManager();    
  }
  
  @Override
  public Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> interceptorChain = new ArrayList<CommandInterceptor>(super.getDefaultCommandInterceptorsTxRequired());
    interceptorChain.add(interceptorChain.size()-1, new CdiActivitiInterceptor());
    return interceptorChain;
  }
  
  @Override
  public Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> interceptorChain = new ArrayList<CommandInterceptor>(super.getDefaultCommandInterceptorsTxRequiresNew());
    interceptorChain.add(interceptorChain.size()-1, new CdiActivitiInterceptor());
    return interceptorChain;
  }
  
}
