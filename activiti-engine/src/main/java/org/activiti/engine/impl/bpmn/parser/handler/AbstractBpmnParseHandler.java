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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.parse.BpmnParseHandler;

public abstract class AbstractBpmnParseHandler<T extends BaseElement> implements BpmnParseHandler {

    public Set<Class<? extends BaseElement>> getHandledTypes() {
        Set<Class<? extends BaseElement>> types = new HashSet<Class<? extends BaseElement>>();
        types.add(getHandledType());
        return types;
    }

    protected abstract Class<? extends BaseElement> getHandledType();

    @SuppressWarnings("unchecked")
    public void parse(BpmnParse bpmnParse,
                      BaseElement element) {
        T baseElement = (T) element;
        executeParse(bpmnParse,
                     baseElement);
    }

    protected abstract void executeParse(BpmnParse bpmnParse,
                                         T element);

    protected ExecutionListener createExecutionListener(BpmnParse bpmnParse,
                                                        ActivitiListener activitiListener) {
        ExecutionListener executionListener = null;

        if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = bpmnParse.getListenerFactory().createClassDelegateExecutionListener(activitiListener);
        } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = bpmnParse.getListenerFactory().createExpressionExecutionListener(activitiListener);
        } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = bpmnParse.getListenerFactory().createDelegateExpressionExecutionListener(activitiListener);
        }
        return executionListener;
    }

    protected String getPrecedingEventBasedGateway(BpmnParse bpmnParse,
                                                   IntermediateCatchEvent event) {
        String eventBasedGatewayId = null;
        for (SequenceFlow sequenceFlow : event.getIncomingFlows()) {
            FlowElement sourceElement = bpmnParse.getBpmnModel().getFlowElement(sequenceFlow.getSourceRef());
            if (sourceElement instanceof EventGateway) {
                eventBasedGatewayId = sourceElement.getId();
                break;
            }
        }
        return eventBasedGatewayId;
    }

    protected void processArtifacts(BpmnParse bpmnParse,
                                    Collection<Artifact> artifacts) {
        // associations
        for (Artifact artifact : artifacts) {
            if (artifact instanceof Association) {
                createAssociation(bpmnParse,
                                  (Association) artifact);
            }
        }
    }

    protected void createAssociation(BpmnParse bpmnParse,
                                     Association association) {
        BpmnModel bpmnModel = bpmnParse.getBpmnModel();
        if (bpmnModel.getArtifact(association.getSourceRef()) != null || bpmnModel.getArtifact(association.getTargetRef()) != null) {

            // connected to a text annotation so skipping it
            return;
        }

        // ActivityImpl sourceActivity =
        // parentScope.findActivity(association.getSourceRef());
        // ActivityImpl targetActivity =
        // parentScope.findActivity(association.getTargetRef());

        // an association may reference elements that are not parsed as
        // activities (like for instance
        // text annotations so do not throw an exception if sourceActivity or
        // targetActivity are null)
        // However, we make sure they reference 'something':
        // if (sourceActivity == null) {
        // bpmnModel.addProblem("Invalid reference sourceRef '" +
        // association.getSourceRef() + "' of association element ",
        // association.getId());
        // } else if (targetActivity == null) {
        // bpmnModel.addProblem("Invalid reference targetRef '" +
        // association.getTargetRef() + "' of association element ",
        // association.getId());
    /*
     * } else { if (sourceActivity.getProperty("type").equals("compensationBoundaryCatch" )) { Object isForCompensation = targetActivity.getProperty(PROPERTYNAME_IS_FOR_COMPENSATION); if
     * (isForCompensation == null || !(Boolean) isForCompensation) { logger.warn( "compensation boundary catch must be connected to element with isForCompensation=true" ); } else { ActivityImpl
     * compensatedActivity = sourceActivity.getParentActivity(); compensatedActivity.setProperty(BpmnParse .PROPERTYNAME_COMPENSATION_HANDLER_ID, targetActivity.getId()); } } }
     */
    }
}
