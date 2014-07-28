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
package org.activiti.workflow.simple.converter;


public interface ConversionConstants {
  
  String DEFAULT_SEQUENCEFLOW_PREFIX = "sequenceFlow";
  
  String USER_TASK_ID_PREFIX = "userTask";
  String GATEWAY_ID_PREFIX = "gateway";
  String EVENT_ID_PREFIX = "event";
  String END_EVENT_ID_PREFIX = "endEvent";
  String BOUNDARY_ID_PREFIX = "boundaryEvent";
  String SCRIPT_TASK_ID_PREFIX = "scriptTask";
  String SERVICE_TASK_ID_PREFIX = "serviceTask";
  String INTERMEDIATE_EVENT_ID_PREVIX = "intermediateEvent";

}
