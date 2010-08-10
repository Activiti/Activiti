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
package org.activiti.engine.impl.form;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.engine.impl.persistence.task.TaskEntity;

import de.odysseus.el.ExpressionFactoryImpl;

/**
 * @author Tom Baeyens
 */
public class JuelFormEngine implements FormEngine {
  
  ExpressionFactory expressionFactory = new ExpressionFactoryImpl();

  public String render(DeploymentEntity deployment, String formReference, TaskEntity task) {
    try {
      // get the template
      ResourceEntity formResourceEntity = deployment.getResource(formReference);
      if (formResourceEntity==null) {
        throw new ActivitiException("form '"+formReference+"' not available in "+deployment);
      }
      byte[] formResourceBytes = formResourceEntity.getBytes();
      String formString = new String(formResourceBytes);
      
      ELContext elContext = new TaskElContext(task);
      ValueExpression result = expressionFactory.createValueExpression(elContext, formString, String.class);
      return (String) result.getValue(elContext);
      
    } catch (Exception e) {
      throw new ActivitiException("problem rendering template: "+e.getMessage(), e);
    }
  }

//  private Object createFormData(TaskEntity task) {
//    if (task!=null) {
//      return task.getActivityInstanceVariables();
//    }
//    return null;
//  }
}
