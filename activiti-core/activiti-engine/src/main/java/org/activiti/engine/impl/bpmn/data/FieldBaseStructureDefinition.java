/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.data;

import org.activiti.engine.api.internal.Internal;

/**
 * Represents a structure definition based on fields
 *
 */
@Internal
public interface FieldBaseStructureDefinition extends StructureDefinition {

  /**
   * Obtains the number of fields that this structure has
   *
   * @return the number of fields that this structure has
   */
  int getFieldSize();

  /**
   * Obtains the name of the field in the index position
   *
   * @param index
   *          the position of the field
   * @return the name of the field
   */
  String getFieldNameAt(int index);

  /**
   * Obtains the type of the field in the index position
   *
   * @param index
   *          the position of the field
   * @return the type of the field
   */
  Class<?> getFieldTypeAt(int index);
}
