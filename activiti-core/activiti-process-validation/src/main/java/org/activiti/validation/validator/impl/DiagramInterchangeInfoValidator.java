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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**

 */
public class DiagramInterchangeInfoValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    if (!bpmnModel.getLocationMap().isEmpty()) {

      // Location map
      for (String bpmnReference : bpmnModel.getLocationMap().keySet()) {
        if (bpmnModel.getFlowElement(bpmnReference) == null) {
          // ACT-1625: don't warn when artifacts are referenced from
          // DI
          if (bpmnModel.getArtifact(bpmnReference) == null) {
            // check if it's a Pool or Lane, then DI is ok
            if (bpmnModel.getPool(bpmnReference) == null && bpmnModel.getLane(bpmnReference) == null) {
              addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: could not find " + bpmnReference);
            }
          }
        } else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof FlowNode)) {
          addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_FLOWNODE, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: " + bpmnReference
              + " does not reference a flow node");
        }
      }

    }

    if (!bpmnModel.getFlowLocationMap().isEmpty()) {
      // flowlocation map
      for (String bpmnReference : bpmnModel.getFlowLocationMap().keySet()) {
        if (bpmnModel.getFlowElement(bpmnReference) == null && bpmnModel.getMessageFlow(bpmnReference) == null) {
          // ACT-1625: don't warn when artifacts are referenced from
          // DI
          if (bpmnModel.getArtifact(bpmnReference) == null) {
            addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: could not find " + bpmnReference);
          }
        }

        if (bpmnModel.getFlowElement(bpmnReference) != null  && !(bpmnModel.getFlowElement(bpmnReference) instanceof SequenceFlow)) {
          addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_SEQ_FLOW, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: " + bpmnReference
              + " does not reference a sequence flow");
        }

      }
    }
  }
}
