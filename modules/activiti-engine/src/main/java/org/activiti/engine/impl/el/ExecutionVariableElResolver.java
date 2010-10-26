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

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;


/**
 * Implementation of an {@link ELResolver} that resolves expressions 
 * with the process variables of a given {@link ExecutionImpl} as context.
 * 
 * @author Joram Barrez
 */
public class ExecutionVariableElResolver extends ELResolver {
  
  public static final String EXECUTION_KEY = "execution";
  
  protected ExecutionImpl execution;
  
  public ExecutionVariableElResolver(ExecutionImpl execution) {
    this.execution = execution;
  }

  public Object getValue(ELContext context, Object base, Object property)  {
    
    // Variable resolution
    if (base == null) {
      String variable = (String) property; // according to javadoc, can only be a String
      if(EXECUTION_KEY.equals(property)) {
        context.setPropertyResolved(true);
        return execution;
      } else {
        if (execution.hasVariable(variable)) {
          context.setPropertyResolved(true); // if not set, the next elResolver in the CompositeElResolver will be called
          return execution.getVariable(variable);
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
      return !execution.hasVariable(variable);
    }
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base == null) {
      String variable = (String) property;
      if (execution.hasVariable(variable)) {
        execution.setVariable(variable, value);
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
