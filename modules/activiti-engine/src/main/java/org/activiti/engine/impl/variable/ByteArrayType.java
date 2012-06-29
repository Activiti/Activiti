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

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Tom Baeyens
 */
public class ByteArrayType implements VariableType {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "bytes";
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
    if (valueFields.getByteArrayValueId()==null) {
      return null;
    }
    return valueFields.getByteArrayValue().getBytes();
  }

  public void setValue(Object value, ValueFields valueFields) {
    ByteArrayEntity byteArray = valueFields.getByteArrayValue();
    byte[] bytes = (byte[]) value;
    if (byteArray==null) {
      byteArray = new ByteArrayEntity(bytes);
      
      DbSqlSession dbSqlSession = Context
        .getCommandContext()
        .getDbSqlSession();      
      dbSqlSession.insert(byteArray);
      
      valueFields.setByteArrayValue(byteArray);
      
      if(valueFields instanceof VariableInstanceEntity) {
        dbSqlSession.addDeserializedObject(valueFields.getCachedValue(), bytes, (VariableInstanceEntity)valueFields);
      }
      
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
