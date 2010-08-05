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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.VariableMapper;

import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ExecutionELContext extends ELContext {
  
  private final ExecutionImpl execution;
  private ELResolver elResolver;
    
  public ExecutionELContext(ExecutionImpl execution) {
    this.execution = execution;
  }
  
  public void setElResolver(ELResolver elResolver) {
    this.elResolver = elResolver;
  }

  public ELResolver getELResolver() {
    CompositeELResolver elResolver = new CompositeELResolver();
    elResolver.add(new ExecutionVariableElResolver(execution));
    if (this.elResolver!=null) {
      elResolver.add(this.elResolver);
    }
    elResolver.add(new ArrayELResolver());
    elResolver.add(new ListELResolver());
    elResolver.add(new MapELResolver());
    elResolver.add(new BeanELResolver());
    return elResolver;
  }
  
  public FunctionMapper getFunctionMapper() {
    return new ActivitiFunctionMapper();
  }
  public VariableMapper getVariableMapper() {
    return null;
  }
}
