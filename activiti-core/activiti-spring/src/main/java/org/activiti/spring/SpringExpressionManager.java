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

package org.activiti.spring;

import java.util.Map;
import javax.el.CompositeELResolver;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.ReadOnlyMapELResolver;
import org.springframework.context.ApplicationContext;

/**
 * {@link ExpressionManager} that exposes the full application-context or a limited set of beans in expressions.
 *

 */
public class SpringExpressionManager extends ExpressionManager {

  protected ApplicationContext applicationContext;

  /**
   * @param applicationContext
   *          the applicationContext to use. Ignored when 'beans' parameter is not null.
   * @param beans
   *          a map of custom beans to expose. If null, all beans in the application-context will be exposed.
   */
  public SpringExpressionManager(ApplicationContext applicationContext, Map<Object, Object> beans) {
    super(beans);
    this.applicationContext = applicationContext;
  }

    @Override
    protected void addBeansResolver(CompositeELResolver elResolver) {
        if (beans != null) {
            // Only expose limited set of beans in expressions
            elResolver.add(new ReadOnlyMapELResolver(beans));
        } else {
            // Expose full application-context in expressions
            elResolver.add(new ApplicationContextElResolver(applicationContext));
        }

    }

}
