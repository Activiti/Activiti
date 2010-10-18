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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;

/**
 * @author Tom Baeyens
 */
public class SerializableType extends ByteArrayType {

  public static final String TYPE_NAME = "serializable";
  
  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Object getValue(VariableInstanceEntity variableInstanceEntity) {
    Object cachedObject = variableInstanceEntity.getCachedValue();
    if (cachedObject!=null) {
      return cachedObject;
    }
    byte[] bytes = (byte[]) super.getValue(variableInstanceEntity);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Object deserializedObject;
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      deserializedObject = ois.readObject();
      variableInstanceEntity.setCachedValue(deserializedObject);
      
      CommandContext
        .getCurrent()
        .getDbSqlSession()
        .addDeserializedObject(deserializedObject, bytes, variableInstanceEntity);
      
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize object in variable '"+variableInstanceEntity.getName()+"'", e);
    }
    return deserializedObject;
  }

  public void setValue(Object value, VariableInstanceEntity variableInstanceEntity) {
    byte[] byteArray = serialize(value, variableInstanceEntity);
    variableInstanceEntity.setCachedValue(value);
    super.setValue(byteArray, variableInstanceEntity);
  }

  public static byte[] serialize(Object value, VariableInstanceEntity variableInstanceEntity) {
    if(value == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream ois = new ObjectOutputStream(baos);
      ois.writeObject(value);
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize value '"+value+"' in variable '"+variableInstanceEntity.getName()+"'", e);
    }
    return baos.toByteArray();
  }
  
  public boolean isAbleToStore(Object value) {
    return value instanceof Serializable;
  }
}
