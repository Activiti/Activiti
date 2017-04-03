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
package org.activiti.dmn.engine.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.ActivitiDmnExpressionException;
import org.activiti.dmn.engine.RuleEngineExecutor;
import org.activiti.dmn.engine.impl.mvel.ExecutionVariableFactory;
import org.activiti.dmn.engine.impl.mvel.MvelExecutionContext;
import org.activiti.dmn.engine.impl.mvel.MvelExecutionContextBuilder;
import org.activiti.dmn.engine.impl.mvel.MvelExpressionExecutor;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionRule;
import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.HitPolicy;
import org.activiti.dmn.model.LiteralExpression;
import org.activiti.dmn.model.RuleInputClauseContainer;
import org.activiti.dmn.model.RuleOutputClauseContainer;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.integration.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class RuleEngineExecutorImpl implements RuleEngineExecutor {

  private static final Logger logger = LoggerFactory.getLogger(RuleEngineExecutorImpl.class);

  /**
   * Executes the given decision table and creates the outcome results
   *
   * @param decision
   *          the DMN decision 
   * @param input
   *          map with input variables
   * @return updated execution variables map
   */
  @Override
  public RuleEngineExecutionResult execute(Decision decision, Map<String, Object> input,
      Map<String, Method> customExpressionFunctions, Map<Class<?>, PropertyHandler> propertyHandlers) {

    if (decision  == null) {
      throw new IllegalArgumentException("no decision provided");
    }

    if (decision.getExpression() == null || !(decision.getExpression() instanceof DecisionTable)) {
      throw new IllegalArgumentException("no decision table present in decision");
    }
    
    DecisionTable currentDecisionTable = (DecisionTable) decision.getExpression();

    // create execution context and audit trail
    MvelExecutionContext executionContext = MvelExecutionContextBuilder.build(decision, input, 
        customExpressionFunctions, propertyHandlers);

    // evaluate decision table
    Map<String, Object> resultVariables = evaluateDecisionTable(currentDecisionTable, executionContext);

    // end audit trail
    executionContext.getAuditContainer().stopAudit(resultVariables);

    // create result container
    RuleEngineExecutionResult executionResult = new RuleEngineExecutionResult(resultVariables, executionContext.getAuditContainer());

    return executionResult;
  }

  protected Map<String, Object> evaluateDecisionTable(DecisionTable decisionTable, MvelExecutionContext executionContext) {

    if (decisionTable == null || decisionTable.getRules().isEmpty()) {
      throw new IllegalArgumentException("no rules present in table");
    }

    if (executionContext == null) {
      throw new ActivitiDmnException("no execution context available");
    }

    logger.debug("Start table evaluation: " + decisionTable.getId());

    Map<String, Object> ruleResults = new HashMap<String, Object>();
    List<ValidRuleOutputEntries> validConclusionsStack = new ArrayList<ValidRuleOutputEntries>();

    // initialize rule counter
    // currently this is the only way to identify the rules
    int ruleRowCounter = 0;

    try {
      // evaluate rule conditions
      for (DecisionRule rule : decisionTable.getRules()) {

        Boolean ruleResult = executeRule(ruleRowCounter, rule, executionContext, validConclusionsStack);

        if (shouldContinueEvaluating(decisionTable.getHitPolicy(), ruleResult) == false) {
          break;
        }

        ruleRowCounter++;
      }

    } catch (ActivitiDmnException ade) {
      logger.error("decision table execution failed", ade);
      executionContext.getAuditContainer().setFailed();
      executionContext.getAuditContainer().setExceptionMessage(getExceptionMessage(ade));
    }

    logger.debug("End table evaluation: " + decisionTable.getId());

    return executionContext.getResultVariables();
  }

  protected Boolean shouldContinueEvaluating(HitPolicy hitPolicy, Boolean ruleResult) {

    Boolean shouldContinue = Boolean.TRUE;

    if (hitPolicy == HitPolicy.FIRST && ruleResult) {
      logger.debug("Stopping execution: rule is valid and Hit Policy is FIRST");
      shouldContinue = Boolean.FALSE;
    }

    return shouldContinue;
  }

  protected Boolean executeRule(int ruleRowIndex, DecisionRule rule, MvelExecutionContext executionContext, 
      List<ValidRuleOutputEntries> validOutputEntriesStack) {

    if (rule == null) {
      throw new ActivitiDmnException("rule cannot be null");
    }

    logger.debug("Start rule evaluation");

    // add audit entry
    executionContext.getAuditContainer().addRuleEntry();

    Boolean conditionResult = Boolean.FALSE;

    // go through conditions
    for (RuleInputClauseContainer conditionContainer : rule.getInputEntries()) {

      // resetting value
      conditionResult = Boolean.FALSE;

      try {

        // if condition is empty condition result is TRUE
        if (StringUtils.isEmpty(conditionContainer.getInputEntry().getText())) {
          conditionResult = Boolean.TRUE;
        } else {
          conditionResult = executeInputExpressionEvaluation(conditionContainer, executionContext);
        }

        // add audit entry
        executionContext.getAuditContainer().addInputEntry(ruleRowIndex, conditionContainer.getInputEntry().getId(), conditionResult);

        logger.debug("input entry {} ( {} {} ): {} ", conditionContainer.getInputEntry().getId(), 
            conditionContainer.getInputClause().getInputExpression().getText(),
            conditionContainer.getInputEntry().getText(), conditionResult);

      } catch (ActivitiDmnExpressionException adee) {

        // add failed audit entry
        executionContext.getAuditContainer().addInputEntry(ruleRowIndex, conditionContainer.getInputEntry().getId(), 
            getExceptionMessage(adee), conditionResult);
        
      } catch (ActivitiDmnException ade) {

        // add failed audit entry and rethrow
        executionContext.getAuditContainer().addInputEntry(ruleRowIndex, conditionContainer.getInputEntry().getId(), 
            getExceptionMessage(ade), null);
        throw ade;
        
      } catch (Exception e) {

        // add failed audit entry and rethrow
        executionContext.getAuditContainer().addInputEntry(ruleRowIndex, conditionContainer.getInputEntry().getId(), 
            getExceptionMessage(e), null);
        throw new ActivitiDmnException(getExceptionMessage(e), e);
      }

      // exit evaluation loop if a condition is evaluated false
      if (conditionResult == false) {
        break;
      }
    }

    // execute conclusion if condition was evaluated true
    if (conditionResult) {
      executeOutputEntryAction(ruleRowIndex, rule.getOutputEntries(), executionContext);
    }

    // mark rule end
    executionContext.getAuditContainer().markRuleEnd(ruleRowIndex);

    logger.debug("End rule evaluation");
    return conditionResult;
  }

  protected Boolean executeInputExpressionEvaluation(RuleInputClauseContainer ruleContainer, MvelExecutionContext executionContext) {

    return MvelExpressionExecutor.executeInputExpression(ruleContainer.getInputClause(), ruleContainer.getInputEntry(), executionContext);
  }

  protected void executeOutputEntryAction(int ruleRowIndex, List<RuleOutputClauseContainer> ruleOutputContainers, MvelExecutionContext executionContext) {

    logger.debug("Start conclusion processing");

    for (RuleOutputClauseContainer clauseContainer : ruleOutputContainers) {

      // skip empty output entries
      if (StringUtils.isNotEmpty(clauseContainer.getOutputEntry().getText())) {
        composeOutputEntryResult(ruleRowIndex, clauseContainer, executionContext);
      }
    }

    logger.debug("End conclusion processing");
  }

  protected void composeOutputEntryResult(int ruleRowIndex, RuleOutputClauseContainer ruleClauseContainer, MvelExecutionContext executionContext) {

    String outputVariableId = ruleClauseContainer.getOutputClause().getName();
    String outputVariableType = ruleClauseContainer.getOutputClause().getTypeRef();

    LiteralExpression outputEntryExpression = ruleClauseContainer.getOutputEntry();

    Object executionVariable = null;
    try {
      Object resultVariable = MvelExpressionExecutor.executeOutputExpression(ruleClauseContainer.getOutputClause(), outputEntryExpression, executionContext);
      executionVariable = ExecutionVariableFactory.getExecutionVariable(outputVariableType, resultVariable);

      // update execution context
      // stack
      executionContext.getStackVariables().put(outputVariableId, executionVariable);

      // result variables
      executionContext.getResultVariables().put(outputVariableId, executionVariable);

      // add audit entry
      executionContext.getAuditContainer().addOutputEntry(ruleRowIndex, outputEntryExpression.getId(), executionVariable);

      if (executionVariable != null) {
        logger.debug("Created conclusion result: {} of type: {} with value {} ", outputVariableId, resultVariable.getClass(), resultVariable.toString());
      } else {
        logger.warn("Could not create conclusion result");
      }
    } catch (ActivitiDmnException ade) {

      // add failed audit entry and rethrow
      executionContext.getAuditContainer().addOutputEntry(ruleRowIndex, outputEntryExpression.getId(), getExceptionMessage(ade), executionVariable);
      throw ade;
      
    } catch (Exception e) {

      // add failed audit entry and rethrow
      executionContext.getAuditContainer().addOutputEntry(ruleRowIndex, outputEntryExpression.getId(), getExceptionMessage(e), executionVariable);
      throw new ActivitiDmnException(getExceptionMessage(e), e);
    }
  }

  protected String getExceptionMessage(Exception exception) {
    String exceptionMessage = null;
    if (exception.getCause() != null && exception.getCause().getMessage() != null) {
      exceptionMessage = exception.getCause().getMessage();
    } else {
      exceptionMessage = exception.getMessage();
    }
    return exceptionMessage;
  }
}
