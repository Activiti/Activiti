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
package org.activiti.impl.variable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.activiti.ActivitiException;
import org.activiti.impl.interceptor.CommandContext;

/**
 * @author Tom Baeyens
 */
public class SerializableType extends ByteArrayType {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "serializable";
  }

  public Object getValue(VariableInstance variableInstance) {
    Object cachedObject = variableInstance.getCachedValue();
    if (cachedObject!=null) {
      return cachedObject;
    }
    byte[] bytes = (byte[]) super.getValue(variableInstance);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Object deserializedObject;
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      deserializedObject = ois.readObject();
      variableInstance.setCachedValue(deserializedObject);
      
      CommandContext
        .getCurrentCommandContext()
        .getPersistenceSession()
        .addDeserializedObject(deserializedObject, bytes, variableInstance);
      
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize object in variable '"+variableInstance.getName()+"'", e);
    }
    return deserializedObject;
  }

  public void setValue(Object value, VariableInstance variableInstance) {
    byte[] byteArray = serialize(value, variableInstance);
    variableInstance.setCachedValue(value);
    super.setValue(byteArray, variableInstance);
  }

  public static byte[] serialize(Object value, VariableInstance variableInstance) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream ois = new ObjectOutputStream(baos);
      ois.writeObject(value);
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize value '"+value+"' in variable '"+variableInstance.getName()+"'", e);
    }
    return baos.toByteArray();
  }
  
  public boolean isAbleToStore(Object value) {
    return value instanceof Serializable;
  }
}
