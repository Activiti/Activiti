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
package org.activiti.impl.form;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.activiti.ActivitiException;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.task.TaskImpl;

import de.odysseus.el.ExpressionFactoryImpl;

/**
 * @author Tom Baeyens
 */
public class JuelFormEngine implements FormEngine {
  
  ExpressionFactory expressionFactory = new ExpressionFactoryImpl();

  public String render(DeploymentImpl deployment, String formReference, TaskImpl task) {
    try {
      // get the template
      ByteArrayImpl formResourceByteArray = deployment.getResource(formReference);
      if (formResourceByteArray==null) {
        throw new ActivitiException("form '"+formReference+"' not available in "+deployment);
      }
      byte[] formResourceBytes = formResourceByteArray.getBytes();
      String formString = new String(formResourceBytes);
      
      ELContext elContext = new TaskElContext(task);
      ValueExpression result = expressionFactory.createValueExpression(elContext, formString, String.class);
      return (String) result.getValue(elContext);
      
    } catch (Exception e) {
      throw new ActivitiException("problem rendering template: "+e.getMessage(), e);
    }
  }

  private Object createFormData(TaskImpl task) {
    if (task!=null) {
      return task.getExecutionVariables();
    }
    return null;
  }
}
