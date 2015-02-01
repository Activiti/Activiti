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
package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


/**
 * Implementation of an {@link ELResolver} that resolves expressions 
 * with the process variables of a given {@link VariableScope} as context.
 * <br>
 * Also exposes the currently logged in username to be used in expressions (if any)
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class VariableScopeElResolver extends ELResolver {
  
  public static final String EXECUTION_KEY = "execution";
  public static final String TASK_KEY = "task";
  public static final String LOGGED_IN_USER_KEY = "authenticatedUserId";
  
  protected VariableScope variableScope;
  
  public VariableScopeElResolver(VariableScope variableScope) {
    this.variableScope = variableScope;
  }

  public Object getValue(ELContext context, Object base, Object property)  {
    
    if (base == null) {
      String variable = (String) property; // according to javadoc, can only be a String
      
      if( (EXECUTION_KEY.equals(property) && variableScope instanceof ExecutionEntity)
              || (TASK_KEY.equals(property) && variableScope instanceof TaskEntity) ) {
        context.setPropertyResolved(true);
        return variableScope;
      } else if (EXECUTION_KEY.equals(property) && variableScope instanceof TaskEntity) {
        context.setPropertyResolved(true);
        return ((TaskEntity) variableScope).getExecution();
      } else if(LOGGED_IN_USER_KEY.equals(property)){
        context.setPropertyResolved(true);
        return Authentication.getAuthenticatedUserId();
      } else {
        if (variableScope.hasVariable(variable)) {
          context.setPropertyResolved(true); // if not set, the next elResolver in the CompositeElResolver will be called
          return variableScope.getVariable(variable);
        }        
      }
    }
    
    // property resolution (eg. bean.value) will be done by the BeanElResolver (part of the CompositeElResolver)
    // It will use the bean resolved in this resolver as base.
    
    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (base == null) {
      String variable = (String) property;
      return !variableScope.hasVariable(variable);
    }
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base == null) {
      String variable = (String) property;
      if (variableScope.hasVariable(variable)) {
        variableScope.setVariable(variable, value);
      }
    }
  }
  
  public Class< ? > getCommonPropertyType(ELContext arg0, Object arg1) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
    return null;
  }

  public Class< ? > getType(ELContext arg0, Object arg1, Object arg2) {
    return Object.class;
  }

}
