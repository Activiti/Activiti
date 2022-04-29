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

package org.activiti.engine.impl.variable;

import org.activiti.engine.api.internal.Internal;


@Internal
public interface VariableType {

  /**
   * name of variable type (limited to 100 characters length)
   */
  public String getTypeName();

  /**
   * <p>
   * Indicates if this variable type supports caching.
   * </p>
   * <p>
   * If caching is supported, the result of {@link #getValue(ValueFields)} is saved for the duration of the session and used for subsequent reads of the variable's value.
   * </p>
   * <p>
   * If caching is not supported, all reads of a variable's value require a fresh call to {@link #getValue(ValueFields)}.
   * </p>
   *
   * @return whether variables of this type are cacheable.
   */
  boolean isCachable();

  /**
   * @return whether this variable type can store the specified value.
   */
  boolean isAbleToStore(Object value);

  /**
   * Stores the specified value in the supplied {@link ValueFields}.
   */
  void setValue(Object value, ValueFields valueFields);

  /**
   * @return the value of a variable based on the specified {@link ValueFields}.
   */
  Object getValue(ValueFields valueFields);

}
