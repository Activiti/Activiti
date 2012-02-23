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

import org.activiti.cdi.impl.el.CdiResolver;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.VariableScopeElResolver;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;

/**
 * {@link ExpressionManager} for resolving Cdi-managed beans.
 * 
 * This {@link ExpressionManager} implementation performs lazy lookup of the
 * Cdi-BeanManager and can thus be configured using the spring-based
 * configuration of the process engine:
 * 
 * <pre>
 * &lt;property name="expressionManager"&gt;
 *      &lt;bean class="org.activiti.cdi.CdiExpressionManager" /&gt;
 * &lt;/property&gt;
 * </pre>
 * 
 * @author Daniel Meyer
 */
public class CdiExpressionManager extends ExpressionManager {

  @Override
  protected ELResolver createElResolver(VariableScope variableScope) {
    CompositeELResolver compositeElResolver = new CompositeELResolver();
    compositeElResolver.add(new VariableScopeElResolver(variableScope));
    
    compositeElResolver.add(new CdiResolver());
    
    compositeElResolver.add(new ArrayELResolver());
    compositeElResolver.add(new ListELResolver());
    compositeElResolver.add(new MapELResolver());
    compositeElResolver.add(new BeanELResolver());
    return compositeElResolver;
  }

}
