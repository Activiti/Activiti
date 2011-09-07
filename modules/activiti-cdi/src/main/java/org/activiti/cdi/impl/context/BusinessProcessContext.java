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
package org.activiti.cdi.impl.context;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;

/**
 * Implementation of the BusinessProcessContext-scope.
 * 
 * @author Daniel Meyer
 */
@SuppressWarnings("unchecked")
public class BusinessProcessContext implements Context {

  final static Logger logger = Logger.getLogger(BusinessProcessContext.class.getName());

  protected BusinessProcess getBusinessProcess() {
    return ProgrammaticBeanLookup.lookup(BusinessProcess.class);
  }

  @Override
  public Class< ? extends Annotation> getScope() {
    return BusinessProcessScoped.class;
  }

  @Override
  public <T> T get(Contextual<T> contextual) {
    Bean<T> bean = (Bean<T>) contextual;
    String variableName = bean.getName();

    BusinessProcess businessProcess = getBusinessProcess();
    Object variable = businessProcess.getVariable(variableName);
    if (variable != null) {

      if (logger.isLoggable(Level.FINE)) {
        if(businessProcess.isAssociated()) {        
          logger.fine("Getting instance of bean '" + variableName + "' from Execution[" + businessProcess.getExecutionId() + "].");
        } else {
          logger.fine("Getting instance of bean '" + variableName + "' from transient bean store");
        }
      }

      return (T) variable;
    } else {
      return null;
    }

  }

  @Override
  public <T> T get(Contextual<T> contextual, CreationalContext<T> arg1) {

    Bean<T> bean = (Bean<T>) contextual;
    String variableName = bean.getName();

    BusinessProcess businessProcess = getBusinessProcess();
    Object variable = businessProcess.getVariable(variableName);
    if (variable != null) {

      if (logger.isLoggable(Level.FINE)) {
        if(businessProcess.isAssociated()) {        
          logger.fine("Getting instance of bean '" + variableName + "' from Execution[" + businessProcess.getExecutionId() + "].");
        } else {
          logger.fine("Getting instance of bean '" + variableName + "' from transient bean store");
        }
      }

      return (T) variable;
    } else {
      if (logger.isLoggable(Level.FINE)) {
        if(businessProcess.isAssociated()) {        
          logger.fine("Creating instance of bean '" + variableName + "' in business process context representing Execution["
                  + businessProcess.getExecutionId() + "].");
        } else {
          logger.fine("Creating instance of bean '" + variableName + "' in transient bean store");
        }
      }

      T beanInstance = bean.create(arg1);
      businessProcess.setVariable(bean.getName(), beanInstance);
      return beanInstance;
    }

  }

  @Override
  public boolean isActive() {
    // we assume the business process is always 'active'. If no task/execution is 
    // associated, temporary instances of @BusinessProcesScoped beans are cached in the 
    // conversation / request / tread 
    return true;
  }

}
