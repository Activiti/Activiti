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

/**
 * Class containing meta-data about Entity-classes.
 * 
 * @author Frederik Heremans
 */
public class EntityMetaData {

  private boolean isJPAEntity = false;
  private Class< ? > entityClass;
  private Method idMethod;
  private Field idField;

  public boolean isJPAEntity() {
    return isJPAEntity;
  }

  public void setJPAEntity(boolean isJPAEntity) {
    this.isJPAEntity = isJPAEntity;
  }

  public Class< ? > getEntityClass() {
    return entityClass;
  }

  public void setEntityClass(Class< ? > entityClass) {
    this.entityClass = entityClass;
  }

  public Method getIdMethod() {
    return idMethod;
  }

  public void setIdMethod(Method idMethod) {
    this.idMethod = idMethod;
    idMethod.setAccessible(true);
  }

  public Field getIdField() {
    return idField;
  }

  public void setIdField(Field idField) {
    this.idField = idField;
    idField.setAccessible(true);
  }

  public Class<?> getIdType() {
    Class<?> idType = null;
    if(idField != null) {
      idType = idField.getType();
    } else if (idMethod != null) {
      idType = idMethod.getReturnType();
    } 
    return idType;
  }
}
