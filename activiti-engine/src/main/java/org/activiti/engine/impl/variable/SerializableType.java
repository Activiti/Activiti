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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;

/**


 */
public class SerializableType extends ByteArrayType {

  public static final String TYPE_NAME = "serializable";
  
  protected boolean trackDeserializedObjects;
  
  public String getTypeName() {
    return TYPE_NAME;
  }
  
  public SerializableType() {
    
  }
  
  public SerializableType(boolean trackDeserializedObjects) {
    this.trackDeserializedObjects = trackDeserializedObjects;
  }

  public Object getValue(ValueFields valueFields) {
    Object cachedObject = valueFields.getCachedValue();
    if (cachedObject != null) {
      return cachedObject;
    }
    
    byte[] bytes = (byte[]) super.getValue(valueFields);
    if (bytes != null) {
      
	    Object deserializedObject = deserialize(bytes, valueFields);
      valueFields.setCachedValue(deserializedObject);
      
      if (trackDeserializedObjects && valueFields instanceof VariableInstanceEntity) {
        Context.getCommandContext().addCloseListener(new VerifyDeserializedObjectCommandContextCloseListener(
            new DeserializedObject(this, valueFields.getCachedValue(), bytes, (VariableInstanceEntity)valueFields)));
      }

      return deserializedObject;
    }
    return null; // byte array is null
  }

  public void setValue(Object value, ValueFields valueFields) {
    byte[] bytes = serialize(value, valueFields);
    valueFields.setCachedValue(value);
    
    super.setValue(bytes, valueFields);
    
    if (trackDeserializedObjects && valueFields instanceof VariableInstanceEntity) {
      Context.getCommandContext().addCloseListener(new VerifyDeserializedObjectCommandContextCloseListener(
          new DeserializedObject(this, valueFields.getCachedValue(), bytes, (VariableInstanceEntity)valueFields)));
    }

  }

  public byte[] serialize(Object value, ValueFields valueFields) {
    if (value == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = null;
    try {
      oos = createObjectOutputStream(baos);
      oos.writeObject(value);
    } catch (Exception e) {
      throw new ActivitiException("Couldn't serialize value '"+value+"' in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(oos);
    }
    return baos.toByteArray();
  }
  
  public Object deserialize(byte[] bytes, ValueFields valueFields) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    try {
      ObjectInputStream ois = createObjectInputStream(bais);
      Object deserializedObject = ois.readObject();

      return deserializedObject;
    } catch (Exception e) {
      throw new ActivitiException("Couldn't deserialize object in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(bais);
    }
  }

  public boolean isAbleToStore(Object value) {
    // TODO don't we need null support here?
    return value instanceof Serializable;
  }

  protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
    return new ObjectInputStream(is) {
      protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return ReflectUtil.loadClass(desc.getName());
      }
    };
  }

	protected ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
    return new ObjectOutputStream(os);
  }
}
