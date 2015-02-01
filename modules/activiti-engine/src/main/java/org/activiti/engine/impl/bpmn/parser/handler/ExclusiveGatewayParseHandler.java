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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Joram Barrez
 */
public class ExclusiveGatewayParseHandler extends AbstractActivityBpmnParseHandler<ExclusiveGateway> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return ExclusiveGateway.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, ExclusiveGateway gateway) {
    ActivityImpl activity = createActivityOnCurrentScope(bpmnParse, gateway, BpmnXMLConstants.ELEMENT_GATEWAY_EXCLUSIVE);
    activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createExclusiveGatewayActivityBehavior(gateway));
  }

}
