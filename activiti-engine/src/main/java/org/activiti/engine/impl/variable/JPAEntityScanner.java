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

package org.activiti.engine.impl.variable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.activiti.engine.ActivitiException;

/**
 * Scans class and creates {@link EntityMetaData} based on it.
 * 

 */
public class JPAEntityScanner {

  public EntityMetaData scanClass(Class<?> clazz) {
    EntityMetaData metaData = new EntityMetaData();
    // in case with JPA Enhancement
    // method should iterate over superclasses list
    // to find @Entity and @Id annotations
    while (clazz != null && !clazz.equals(Object.class)) {

      // Class should have @Entity annotation
      boolean isEntity = isEntityAnnotationPresent(clazz);

      if (isEntity) {
        metaData.setEntityClass(clazz);
        metaData.setJPAEntity(true);
        // Try to find a field annotated with @Id
        Field idField = getIdField(clazz);
        if (idField != null) {
          metaData.setIdField(idField);
        } else {
          // Try to find a method annotated with @Id
          Method idMethod = getIdMethod(clazz);
          if (idMethod != null) {
            metaData.setIdMethod(idMethod);
          } else {
            throw new ActivitiException("Cannot find field or method with annotation @Id on class '" + clazz.getName() + "', only single-valued primary keys are supported on JPA-entities");
          }
        }
        break;
      }
      clazz = clazz.getSuperclass();
    }
    return metaData;
  }

  private Method getIdMethod(Class<?> clazz) {
    Method idMethod = null;
    // Get all public declared methods on the class. According to spec, @Id
    // should only be
    // applied to fields and property get methods
    Method[] methods = clazz.getMethods();
    Id idAnnotation = null;
    for (Method method : methods) {
      idAnnotation = method.getAnnotation(Id.class);
      if(idAnnotation != null && !method.isBridge()) {
        idMethod = method;
        break;
      }
    }
    return idMethod;
  }

  private Field getIdField(Class<?> clazz) {
    Field idField = null;
    Field[] fields = clazz.getDeclaredFields();
    Id idAnnotation = null;
    for (Field field : fields) {
      idAnnotation = field.getAnnotation(Id.class);
      if (idAnnotation != null) {
        idField = field;
        break;
      }
    }

    if (idField == null) {
      // Check superClass for fields with @Id, since getDeclaredFields
      // does
      // not return superclass-fields.
      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null && !superClass.equals(Object.class)) {
        // Recursively go up class hierarchy
        idField = getIdField(superClass);
      }
    }
    return idField;
  }

  private boolean isEntityAnnotationPresent(Class<?> clazz) {
    return (clazz.getAnnotation(Entity.class) != null);
  }
}
