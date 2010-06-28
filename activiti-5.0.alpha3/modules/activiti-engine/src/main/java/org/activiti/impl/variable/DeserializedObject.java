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

import java.util.Arrays;


/**
 * @author Tom Baeyens
 */
public class DeserializedObject {

  Object deserializedObject;
  byte[] originalBytes;
  VariableInstance variableInstance;
  
  public DeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstance variableInstance) {
    this.deserializedObject = deserializedObject;
    this.originalBytes = serializedBytes;
    this.variableInstance = variableInstance;
  }

  public void flush() {
    // this first check verifies if the variable value was not overwritten with another object
    if (deserializedObject==variableInstance.getCachedValue()) {
      byte[] bytes = SerializableType.serialize(deserializedObject, variableInstance);
      if (!Arrays.equals(originalBytes, bytes)) {
        variableInstance
          .getByteArrayValue()
          .setBytes(bytes);
      }
    }
  }
}
