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


package org.activiti.engine.runtime;

import org.activiti.engine.api.internal.Internal;

/*
 * Represents a modeled DataObject.
 */
@Internal
public interface DataObject {

  /**
   * Name of the DataObject.
   */
  String getName();

  /**
   * Localized Name of the DataObject.
   */
  String getLocalizedName();

  /**
   * Description of the DataObject.
   */
  String getDescription();

  /**
   * Value of the DataObject.
   */
  Object getValue();

  /**
   * Type of the DataObject.
   */
  String getType();

  /**
   * The id of the flow element in the process defining this data object.
   */
  String getDataObjectDefinitionKey();
}
