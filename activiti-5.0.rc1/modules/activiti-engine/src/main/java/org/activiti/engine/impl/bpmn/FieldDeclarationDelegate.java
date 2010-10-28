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

package org.activiti.engine.impl.bpmn;

import java.lang.reflect.Field;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.util.ReflectUtil;


/**
 * Extend this class to add support for using field injection based on {@link FieldDeclaration}s on 
 * a single object instance of a specific class.
 * 
 * @author Frederik Heremans
 */
public class FieldDeclarationDelegate {
  
  private Object objectInstance;
  private String className;
  private List<FieldDeclaration> fieldDeclarations;
  
  public FieldDeclarationDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    this.className = className;
    this.fieldDeclarations = fieldDeclarations;
  }
  
  /**
   * Get the delegate object instance that has it's fields injected based on the
   * {@link FieldDeclaration}s. The same instance is always returned,
   * it is is created and injected only once.
   */
  public Object getDelegateInstance() {
    if(objectInstance == null) {
      objectInstance = ReflectUtil.instantiate(className);
      injectFieldDeclarations(objectInstance);
    }
    return objectInstance;
  }

  private void injectFieldDeclarations(Object object) {
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        Field field = ReflectUtil.getField(declaration.getName(), object);
        if(field == null) {
          throw new ActivitiException("Field definition uses unexisting field '" + declaration.getName() + "' on class " + className);
        }
        // Check if the delegate field's type is correct
       if(!fieldTypeCompatible(declaration, field)) {
         throw new ActivitiException("Incompatible type set on field declaration '" + declaration.getName() +
            "' for class " + className + ". Declared value has type " + declaration.getValue().getClass().getName() + ", while expecting " +
            field.getType().getName());
       }
       ReflectUtil.setField(field, object, declaration.getValue());
      }
    }
  }

  private boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if(declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {      
      // Null can be set any field type
      return true;
    }
  }

  protected List<FieldDeclaration> getFieldDeclarations() {
    return fieldDeclarations;
  }
  
  protected String getDelegateClassName() {
    return className;
  }
}
