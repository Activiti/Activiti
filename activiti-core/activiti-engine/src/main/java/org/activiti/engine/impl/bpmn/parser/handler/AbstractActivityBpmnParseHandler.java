/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.apache.commons.lang3.StringUtils;


public abstract class AbstractActivityBpmnParseHandler<T extends FlowNode> extends AbstractFlowNodeBpmnParseHandler<T> {

  @Override
  public void parse(BpmnParse bpmnParse, BaseElement element) {
    super.parse(bpmnParse, element);

    if (element instanceof Activity && ((Activity) element).getLoopCharacteristics() != null) {
      createMultiInstanceLoopCharacteristics(bpmnParse, (Activity) element);
    }
  }

  protected void createMultiInstanceLoopCharacteristics(BpmnParse bpmnParse, Activity modelActivity) {

    MultiInstanceLoopCharacteristics loopCharacteristics = modelActivity.getLoopCharacteristics();

    MultiInstanceActivityBehavior miActivityBehavior = createMultiInstanceActivityBehavior(
          bpmnParse, modelActivity, loopCharacteristics);

    modelActivity.setBehavior(miActivityBehavior);

    ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();

    // loop cardinality
    if (StringUtils.isNotEmpty(loopCharacteristics.getLoopCardinality())) {
      miActivityBehavior.setLoopCardinalityExpression(expressionManager.createExpression(loopCharacteristics.getLoopCardinality()));
    }

    // completion condition
    if (StringUtils.isNotEmpty(loopCharacteristics.getCompletionCondition())) {
      miActivityBehavior.setCompletionConditionExpression(expressionManager.createExpression(loopCharacteristics.getCompletionCondition()));
    }

    // activiti:collection
    if (StringUtils.isNotEmpty(loopCharacteristics.getInputDataItem())) {
      if (loopCharacteristics.getInputDataItem().contains("{")) {
        miActivityBehavior.setCollectionExpression(expressionManager.createExpression(loopCharacteristics.getInputDataItem()));
      } else {
        miActivityBehavior.setCollectionVariable(loopCharacteristics.getInputDataItem());
      }
    }

    // activiti:elementVariable
    if (StringUtils.isNotEmpty(loopCharacteristics.getElementVariable())) {
      miActivityBehavior.setCollectionElementVariable(loopCharacteristics.getElementVariable());
    }

    // activiti:elementIndexVariable
    if (StringUtils.isNotEmpty(loopCharacteristics.getElementIndexVariable())) {
      miActivityBehavior.setCollectionElementIndexVariable(loopCharacteristics.getElementIndexVariable());
    }

    if (StringUtils.isNotEmpty(loopCharacteristics.getLoopDataOutputRef())) {
        miActivityBehavior.setLoopDataOutputRef(loopCharacteristics.getLoopDataOutputRef());
    }

    if (StringUtils.isNotEmpty(loopCharacteristics.getOutputDataItem())) {
        miActivityBehavior.setOutputDataItem(loopCharacteristics.getOutputDataItem());
    }

  }

    private MultiInstanceActivityBehavior createMultiInstanceActivityBehavior(BpmnParse bpmnParse,
        Activity modelActivity, MultiInstanceLoopCharacteristics loopCharacteristics) {
        MultiInstanceActivityBehavior miActivityBehavior = null;

        if (loopCharacteristics.isSequential()) {
          miActivityBehavior = bpmnParse.getActivityBehaviorFactory().createSequentialMultiInstanceBehavior(modelActivity, (AbstractBpmnActivityBehavior) modelActivity.getBehavior());
        } else {
          miActivityBehavior = bpmnParse.getActivityBehaviorFactory().createParallelMultiInstanceBehavior(modelActivity, (AbstractBpmnActivityBehavior) modelActivity.getBehavior());
        }
        return miActivityBehavior;
    }
}
