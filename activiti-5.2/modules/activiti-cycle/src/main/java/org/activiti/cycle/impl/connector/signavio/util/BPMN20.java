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
package org.activiti.cycle.impl.connector.signavio.util;

import org.oryxeditor.server.diagram.Shape;

/**
 * @author Falko Menge
 */
public class BPMN20 extends BPMN12 {

  public static String NONE_START_EVENT = "StartNoneEvent";
  public static String ESCALATION_START_EVENT = "StartEscalationEvent";
  public static String ERROR_START_EVENT = "StartErrorEvent";
  public static String COMPENSATION_START_EVENT = "StartCompensationEvent";
  public static String PARALLEL_MULTIPLE_START_EVENT = "StartParallelMultipleEvent";

  public static boolean isStartEvent(Shape shape) {
    return isStartEvent(shape.getStencilId());
  }

  public static boolean isStartEvent(String stencilId) {
    if (NONE_START_EVENT.equals(stencilId)
            || CONDITIONAL_START_EVENT.equals(stencilId)
            || MESSAGE_START_EVENT.equals(stencilId)
            || MULTIPLE_START_EVENT.equals(stencilId)
            || SIGNAL_START_EVENT.equals(stencilId)
            || TIMER_START_EVENT.equals(stencilId)
            || ESCALATION_START_EVENT.equals(stencilId)
            || ERROR_START_EVENT.equals(stencilId)
            || COMPENSATION_START_EVENT.equals(stencilId)
            || PARALLEL_MULTIPLE_START_EVENT.equals(stencilId)
            ) {
      return true;
    }
    return false;
  }

}
