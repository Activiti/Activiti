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

package org.activiti.engine.impl.cfg.spring;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;

import org.activiti.engine.impl.el.ExecutionVariableElResolver;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.pvm.impl.runtime.ExecutionImpl;
import org.springframework.context.ApplicationContext;


/**
 * @author Tom Baeyens
 */
public class SpringExpressionManager extends ExpressionManager {
  
  protected ApplicationContext applicationContext;
  
  public SpringExpressionManager(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  protected ELResolver createElResolver(ExecutionImpl execution) {
    CompositeELResolver compositeElResolver = new CompositeELResolver();
    compositeElResolver.add(new ExecutionVariableElResolver(execution));
    compositeElResolver.add(new ApplicationContextElResolver(applicationContext));
    compositeElResolver.add(new ArrayELResolver());
    compositeElResolver.add(new ListELResolver());
    compositeElResolver.add(new MapELResolver());
    compositeElResolver.add(new BeanELResolver());
    return compositeElResolver;
  }

  
}
