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

import java.util.Arrays;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 */
public class DeserializedObject {

  protected SerializableType type;
  protected Object deserializedObject;
  protected byte[] originalBytes;
  protected VariableInstanceEntity variableInstanceEntity;

  public DeserializedObject(SerializableType type, Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    this.type = type;
    this.deserializedObject = deserializedObject;
    this.originalBytes = serializedBytes;
    this.variableInstanceEntity = variableInstanceEntity;
  }

  public void verifyIfBytesOfSerializedObjectChanged() {
    // this first check verifies if the variable value was not overwritten with another object
    if (deserializedObject == variableInstanceEntity.getCachedValue() && !variableInstanceEntity.isDeleted()) {
      byte[] bytes = type.serialize(deserializedObject, variableInstanceEntity);
      if (!Arrays.equals(originalBytes, bytes)) {

        // Add an additional check to prevent byte differences due to JDK changes etc
        Object originalObject = type.deserialize(originalBytes, variableInstanceEntity);
        byte[] refreshedOriginalBytes = type.serialize(originalObject, variableInstanceEntity);

        if (!Arrays.equals(refreshedOriginalBytes, bytes)) {
          variableInstanceEntity.setBytes(bytes);
        }
      }
    }
  }
}
