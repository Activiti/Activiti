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
package org.activiti.impl.interceptor;

import org.activiti.impl.Cmd;
import org.activiti.impl.ProcessEngineImpl;


/**
 * @author Tom Baeyens
 */
public class SpringEntityManagerInterceptor extends Interceptor {

  public <T> T execute(Cmd<T> cmd, ProcessEngineImpl processManager) {
    throw new UnsupportedOperationException();
//    JpaTemplate jpaTemplate = new JpaTemplate();
//
//    EntityManager entityManager = processManager.getEntityManager();
//    if (entityManager!=null) {
//      jpaTemplate.setEntityManager(entityManager);
//    } else {
//      EntityManagerFactory entityManagerFactory = processManager.getProcessEngine().getEntityManagerFactory();
//      jpaTemplate.setEntityManagerFactory(entityManagerFactory);
//    }
//
//    return (T) jpaTemplate.execute(new ActivitiJpaCallback(cmd, processManager));
  }
  
//  class ActivitiJpaCallback implements JpaCallback {
//    Cmd<?> cmd;
//    ProcessServiceImpl processManager;
//    public ActivitiJpaCallback(Cmd<?> cmd, ProcessServiceImpl processManager) {
//      this.cmd = cmd;
//      this.processManager = processManager;
//    }
//    public Object doInJpa(EntityManager entityManager) {
//      processManager.setEntityManager(entityManager);
//      processManager.setConfiguredObject(entityManager);
//      return next.execute(cmd, processManager);
//    }
//  }
}
