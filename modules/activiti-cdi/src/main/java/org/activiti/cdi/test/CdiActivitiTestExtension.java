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
package org.activiti.cdi.test;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.conversation.ConversationImpl;

/**
 * Register additional beans for the testcases 
 * 
 * @author Daniel Meyer
 */
public class CdiActivitiTestExtension implements Extension {

  public void registerAdditionalBeans(@Observes BeforeBeanDiscovery event, BeanManager manager) {
    event.addAnnotatedType(manager.createAnnotatedType(ConversationImpl.class));
  }
 
}
