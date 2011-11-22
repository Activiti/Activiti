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
package org.activiti.cdi.impl;

import java.util.EmptyStackException;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.context.CachingBeanStore;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.DelegateInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * This flushes any process variables and {@link BusinessProcessScoped
 * @BusinessProcessScoped} beans after a call to delegate code returns.
 * 
 * @author Daniel Meyer
 */
public class CdiProcessVariableFlushingDelegateInterceptor implements DelegateInterceptor {

  public void handleInvocation(DelegateInvocation invocation) throws Exception {
    
    invocation.proceed();
    
    ExecutionEntity execution = null;
    try {
      execution = Context.getExecutionContext().getExecution();
    } catch (EmptyStackException e) {
      // silently ignore, not called in the context of an execution.
      return;
    }
    CachingBeanStore beanStore = getAssociationManager().getBeanStore();
    execution.setVariables(beanStore.getAllAndClear());

  }

  protected BusinessProcessAssociationManager getAssociationManager() {
    return ProgrammaticBeanLookup.lookup(BusinessProcessAssociationManager.class);
  }

}
