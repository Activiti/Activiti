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
package org.activiti.workflow.simple.definition;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Marker interface for all 'patterns' that are known by the simple workflow
 * API.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public interface StepDefinition {
	
  String getId();

  void setId(String id);

  /**
   * Create a clone of this {@link StepDefinition} instance.
   */
  StepDefinition clone();

  /**
   * Sets the properties of this {@link StepDefinition} instance based in the
   * properties present in the given definition.
   */
  void setValues(StepDefinition otherDefinition);

  /**
   * @return custom parameter map.
   */
  Map<String, Object> getParameters();

  /**
   * Set the custom parameters.
   */
  void setParameters(Map<String, Object> parameters);

}
