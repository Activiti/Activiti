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
package org.activiti.dmn.engine.test;

import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yvo Swillens
 */
public class RuntimeTest extends AbstractActivitiDmnTest {

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
    public void executeDecision_multiple_conclusions() {
        Map<String, Object> processVariablesInput = new HashMap<String, Object>();
        processVariablesInput.put("input1", 10);
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test3");
        Assert.assertSame(result.getResultVariables().get("output2").getClass(), Double.class);
        Assert.assertEquals(result.getResultVariables().get("output2"), 3D);
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/dates_1.dmn")
    public void executeDecision_static_dates() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("input1", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/dates_2.dmn")
    public void executeDecision_dynamic_dates_add() {
        Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("input1", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/dates_3.dmn")
    public void executeDecision_dynamic_dates_subtract() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("input1", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/strings_1.dmn")
    public void executeDecision_String_on_input() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "testString");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(String.class, result.getResultVariables().get("output1").getClass());
        Assert.assertEquals(result.getResultVariables().get("output1"), "test1");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/strings_2.dmn")
    public void executeDecision_empty_strings() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "");
        processVariablesInput.put("input2", "This is a sentence containing foobar words.");

        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(String.class, result.getResultVariables().get("output1").getClass());
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_1.dmn")
    public void executeDecision_conlusion_expression_double() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "blablatest");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(Double.class, result.getResultVariables().get("output1").getClass());
        Assert.assertEquals(result.getResultVariables().get("output1"), 5D);
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_2.dmn")
    public void executeDecision_conclusion_expression_cast_exception() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "blablatest");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getAuditTrail().getRuleExecutions().get(1).getConclusionResults().get(0).getException());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_2.dmn")
    public void executeDecision_failed_state_missing_input_variable() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(true, result.getAuditTrail().isFailed());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_3.dmn")
    public void executeDecision_missing_input_variable_boolean() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(false, result.getAuditTrail().isFailed());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_4.dmn")
    public void executeDecision_failed_state_unknown_function_outcome_expression() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "blablatest");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(true, result.getAuditTrail().isFailed());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/outcome_expression_2.dmn")
    public void executeDecision_failed_state_could_not_create_outcome() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "blablatest");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(true, result.getAuditTrail().isFailed());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/empty_expressions.dmn")
    public void executeDecision_empty_expressions() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "testblabla");
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(false, result.getAuditTrail().isFailed());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/dates_4.dmn")
    public void executeDecision_input_null() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();
        processVariablesInput.put("input1", null);
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/reservered_word.dmn")
    public void executeDecision_reserved_word() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = dateTimeFormatter.parseLocalDate("2015-09-18");

        processVariablesInput.put("date", localDate.toDate());
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), String.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), "test2");
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/stack_update.dmn")
    public void executeDecision_stack_update() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", 5);
        processVariablesInput.put("output1", 0);
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("decision", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertSame(result.getResultVariables().get("output1").getClass(), Double.class);
        Assert.assertEquals(result.getResultVariables().get("output1"), 30D);
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/refer_new_var.dmn")
    public void executeDecision_variable_references() {
        
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("ordersize", 1000);
        
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("OrderCalculation", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getResultVariables().get("totalordersum"), 100000D);
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/null_value_variables.dmn")
    public void executeDecision_outcome_null_variables() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "low");
        processVariablesInput.put("input2", "blue");
        processVariablesInput.put("addedPerc", null);
        processVariablesInput.put("amountDue", null);
        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("RiskAssessmentUpdated", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getResultVariables().get("addedPerc"), 15D);
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/null_value_variables.dmn")
    public void executeDecision_outcome_null_variables_not_on_execution_context() {
      Map<String, Object> processVariablesInput = new HashMap<String, Object>();

        processVariablesInput.put("input1", "very high");
        processVariablesInput.put("input2", null);

        RuleEngineExecutionResult result = ruleService.executeDecisionByKey("RiskAssessmentUpdated", processVariablesInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getResultVariables().size());
    }
}