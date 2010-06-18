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

import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class ByteArrayType implements Type {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "bytes";
  }

  public Object getValue(VariableInstance variableInstance) {
    if (variableInstance.getByteArrayValueId()==null) {
      return null;
    }
    return variableInstance.getByteArrayValue().getBytes();
  }

  public void setValue(Object value, VariableInstance variableInstance) {
    ByteArrayImpl byteArray = variableInstance.getByteArrayValue();
    byte[] bytes = (byte[]) value;
    if (byteArray==null) {
      byteArray = new ByteArrayImpl(this, bytes);
      CommandContext
        .getCurrent()
        .getPersistenceSession()
        .insert(byteArray);
      variableInstance.setByteArrayValue(byteArray);
    } else {
      byteArray.setBytes(bytes);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return byte[].class.isAssignableFrom(value.getClass());
  }
}
