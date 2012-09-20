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

import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;


/**
 * @author Tom Baeyens
 */
public interface ValueFields {

  String getName();

  String getTextValue();
  void setTextValue(String textValue);

  String getTextValue2();
  void setTextValue2(String textValue2);

  Long getLongValue();
  void setLongValue(Long longValue);
  
  Double getDoubleValue();
  void setDoubleValue(Double doubleValue);
  
  String getByteArrayValueId();
  ByteArrayEntity getByteArrayValue();
  void setByteArrayValue(byte[] bytes);
  
  Object getCachedValue();
  void setCachedValue(Object deserializedObject);

}
