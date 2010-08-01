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

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.runtime.ByteArrayEntity;
import org.activiti.engine.impl.persistence.runtime.VariableInstanceEntity;

/**
 * @author Tom Baeyens
 */
public class ByteArrayType implements Type {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "bytes";
  }

  public Object getValue(VariableInstanceEntity variableInstanceEntity) {
    if (variableInstanceEntity.getByteArrayValueId()==null) {
      return null;
    }
    return variableInstanceEntity.getByteArrayValue().getBytes();
  }

  public void setValue(Object value, VariableInstanceEntity variableInstanceEntity) {
    ByteArrayEntity byteArray = variableInstanceEntity.getByteArrayValue();
    byte[] bytes = (byte[]) value;
    if (byteArray==null) {
      byteArray = new ByteArrayEntity(this, bytes);
      CommandContext
        .getCurrent()
        .getRuntimeSession()
        .insertByteArray(byteArray);
      variableInstanceEntity.setByteArrayValue(byteArray);
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
