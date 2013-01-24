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
package org.activiti.engine.impl.bpmn.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.util.ReflectUtil;

/**
 * @author Joram Barrez
 */
public class ClassDelegateUtil {
  
  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }
  
  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    Object object = ReflectUtil.instantiate(className);
    applyFieldDeclaration(fieldDeclarations, object);
    return object;
  }
  
  public static void applyFieldDeclaration(List<FieldDeclaration> fieldDeclarations, Object target) {
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        applyFieldDeclaration(declaration, target);
      }
    }
  }
  
  public static void applyFieldDeclaration(FieldDeclaration declaration, Object target) {
    Method setterMethod = ReflectUtil.getSetter(declaration.getName(), 
      target.getClass(), declaration.getValue().getClass());
    
    if(setterMethod != null) {
      try {
        setterMethod.invoke(target, declaration.getValue());
      } catch (IllegalArgumentException e) {
        throw new ActivitiException("Error while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (IllegalAccessException e) {
        throw new ActivitiException("Illegal acces when calling '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (InvocationTargetException e) {
        throw new ActivitiException("Exception while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      }
    } else {
      Field field = ReflectUtil.getField(declaration.getName(), target);
      if(field == null) {
        throw new ActivitiIllegalArgumentException("Field definition uses unexisting field '" + declaration.getName() + "' on class " + target.getClass().getName());
      }
      // Check if the delegate field's type is correct
     if(!fieldTypeCompatible(declaration, field)) {
       throw new ActivitiIllegalArgumentException("Incompatible type set on field declaration '" + declaration.getName() 
          + "' for class " + target.getClass().getName() 
          + ". Declared value has type " + declaration.getValue().getClass().getName() 
          + ", while expecting " + field.getType().getName());
     }
     ReflectUtil.setField(field, target, declaration.getValue());
    }
  }
  
  public static boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if(declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {      
      // Null can be set any field type
      return true;
    }
  }

}
