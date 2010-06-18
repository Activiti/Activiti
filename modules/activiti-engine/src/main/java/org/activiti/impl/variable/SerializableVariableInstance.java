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


/**
 * @author Tom Baeyens
 */
public class SerializableVariableInstance extends ByteArrayVariableInstance {

  private static final long serialVersionUID = 1L;
  
  transient Object cachedObject = null;

  public String getTypeName() {
    return "serializable";
  }

  public Object getValue() {
    if (cachedObject!=null) {
      return cachedObject;
    }
    byte[] bytes = (byte[]) super.getValue();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Object deserializedObject;
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      deserializedObject = ois.readObject();
      cachedObject = deserializedObject;
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize object in variable '"+name+"'", e);
    }
    // TODO associate the deserialized object with the transaction so that it is checked for changes when the transaction completes  
    return deserializedObject;
  }

  public void setValue(Object value) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream ois = new ObjectOutputStream(baos);
      ois.writeObject(value);
      cachedObject = value;
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize value '"+value+"' in variable '"+name+"'", e);
    }
    super.setValue(baos.toByteArray());
  }
  
  public boolean isAbleToStore(Object value) {
    return value instanceof Serializable;
  }
}
