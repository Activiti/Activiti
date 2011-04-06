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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.IoUtil;

/**
 * @author Tom Baeyens
 */
public class SerializableType extends ByteArrayType {

  public static final String TYPE_NAME = "serializable";
  
  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Object getValue(ValueFields valueFields) {
    Object cachedObject = valueFields.getCachedValue();
    if (cachedObject!=null) {
      return cachedObject;
    }
    byte[] bytes = (byte[]) super.getValue(valueFields);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Object deserializedObject;
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      deserializedObject = ois.readObject();
      valueFields.setCachedValue(deserializedObject);
      
      if (valueFields instanceof VariableInstanceEntity) {
        Context
          .getCommandContext()
          .getDbSqlSession()
          .addDeserializedObject(deserializedObject, bytes, (VariableInstanceEntity) valueFields);
      }
      
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize object in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(bais);
    }
    return deserializedObject;
  }

  public void setValue(Object value, ValueFields valueFields) {
    byte[] byteArray = serialize(value, valueFields);
    valueFields.setCachedValue(value);
    super.setValue(byteArray, valueFields);
  }

  public static byte[] serialize(Object value, ValueFields valueFields) {
    if(value == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = null;
    try {
      ois = new ObjectOutputStream(baos);
      ois.writeObject(value);
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize value '"+value+"' in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(ois);
    }
    return baos.toByteArray();
  }
  
  public boolean isAbleToStore(Object value) {
    return value instanceof Serializable;
  }
}
