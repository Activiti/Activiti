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


import org.activiti.engine.api.internal.Internal;

/**
 * Common interface for regular and historic variable entities.
 * 

 */
@Internal
public interface ValueFields {

  /**
   * @return the name of the variable
   */
  String getName();
  
  /**
   * @return the process instance id of the variable
   */
  String getProcessInstanceId();
  
  /**
   * @return the execution id of the variable
   */
  String getExecutionId();

  /**
   * @return the task id of the variable
   */
  String getTaskId();

  /**
   * @return the first text value, if any, or null.
   */
  String getTextValue();

  /**
   * Sets the first text value. A value of null is allowed.
   */
  void setTextValue(String textValue);

  /**
   * @return the second text value, if any, or null.
   */
  String getTextValue2();

  /**
   * Sets second text value. A value of null is allowed.
   */
  void setTextValue2(String textValue2);

  /**
   * @return the long value, if any, or null.
   */
  Long getLongValue();

  /**
   * Sets the long value. A value of null is allowed.
   */
  void setLongValue(Long longValue);

  /**
   * @return the double value, if any, or null.
   */
  Double getDoubleValue();

  /**
   * Sets the double value. A value of null is allowed.
   */
  void setDoubleValue(Double doubleValue);

  /**
   * @return the byte array value, if any, or null.
   */
  byte[] getBytes();

  /**
   * Sets the byte array value. A value of null is allowed.
   */
  void setBytes(byte[] bytes);

  Object getCachedValue();

  void setCachedValue(Object cachedValue);

}
