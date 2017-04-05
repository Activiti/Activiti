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
package org.activiti.editor.dmn.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionRule;
import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DecisionTableOrientation;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.HitPolicy;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.LiteralExpression;
import org.activiti.dmn.model.OutputClause;
import org.activiti.dmn.model.RuleInputClauseContainer;
import org.activiti.dmn.model.RuleOutputClauseContainer;
import org.activiti.dmn.model.UnaryTests;
import org.activiti.editor.dmn.converter.DmnJsonConverter;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Yvo Swillens
 */
public class DmnJsonConverterTest {

    private static final Logger logger = LoggerFactory.getLogger(DmnJsonConverterTest.class);

    private static final String JSON_RESOURCE_1 = "org/activiti/editor/dmn/converter/decisiontable_1.json";
    private static final String JSON_RESOURCE_2 = "org/activiti/editor/dmn/converter/decisiontable_no_rules.json";
    private static final String JSON_RESOURCE_3 = "org/activiti/editor/dmn/converter/decisiontable_2.json";
    private static final String JSON_RESOURCE_4 = "org/activiti/editor/dmn/converter/decisiontable_empty_expressions.json";
    private static final String JSON_RESOURCE_5 = "org/activiti/editor/dmn/converter/decisiontable_order.json";


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testConvertJsonToDmn_OK() throws Exception {

        JsonNode testJsonResource = parseJson(JSON_RESOURCE_1);
        DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(testJsonResource, "xyz", 1, new Date());

        Assert.assertNotNull(dmnDefinition);
        Assert.assertEquals(DmnJsonConverter.MODEL_NAMESPACE, dmnDefinition.getNamespace());
        Assert.assertEquals(DmnJsonConverter.URI_JSON, dmnDefinition.getTypeLanguage());
        Assert.assertEquals("definition_xyz", dmnDefinition.getId());
        Assert.assertEquals("decisionTableRule1", dmnDefinition.getName());

        Assert.assertNotNull(dmnDefinition.getDecisions());
        Assert.assertEquals(1, dmnDefinition.getDecisions().size());

        Decision decision = dmnDefinition.getDecisions().get(0);
        Assert.assertNotNull(decision);
        Assert.assertEquals("decTable1", decision.getId());


        DecisionTable decisionTable = (DecisionTable) decision.getExpression();
        Assert.assertNotNull(decisionTable);
        Assert.assertEquals("decisionTable_11", decisionTable.getId());
        Assert.assertEquals(HitPolicy.ANY, decisionTable.getHitPolicy());
        Assert.assertEquals(DecisionTableOrientation.RULE_AS_ROW, decisionTable.getPreferredOrientation());

        List<InputClause> inputClauses = decisionTable.getInputs();
        Assert.assertNotNull(inputClauses);
        Assert.assertEquals(2, inputClauses.size());

        List<OutputClause> outputClauses = decisionTable.getOutputs();
        Assert.assertNotNull(outputClauses);
        Assert.assertEquals(1, outputClauses.size());

        // Condition 1
        InputClause condition1 = inputClauses.get(0);
        Assert.assertNotNull(condition1.getInputExpression());

        LiteralExpression inputExpression11 = condition1.getInputExpression();
        Assert.assertNotNull(inputExpression11);
        Assert.assertEquals("Order Size", inputExpression11.getLabel());
        Assert.assertEquals("inputExpression_input1", inputExpression11.getId());

        // Condition 2
        InputClause condition2 = inputClauses.get(1);
        Assert.assertNotNull(condition2.getInputExpression());

        LiteralExpression inputExpression21 = condition2.getInputExpression();
        Assert.assertNotNull(inputExpression21);
        Assert.assertEquals("Registered On", inputExpression21.getLabel());
        Assert.assertEquals("inputExpression_input2", inputExpression21.getId());

        // Conclusion 1
        OutputClause conclusion1 = outputClauses.get(0);
        Assert.assertNotNull(conclusion1);

        Assert.assertEquals("Has discount", conclusion1.getLabel());
        Assert.assertEquals("outputExpression_output1", conclusion1.getId());
        Assert.assertEquals("boolean", conclusion1.getTypeRef());
        Assert.assertEquals("newVariable1", conclusion1.getName());

        // Rule 1
        Assert.assertNotNull(decisionTable.getRules());
        Assert.assertEquals(2, decisionTable.getRules().size());

        List<DecisionRule> rules = decisionTable.getRules();

        Assert.assertEquals(2, rules.get(0).getInputEntries().size());

        // input expression 1
        RuleInputClauseContainer ruleClauseContainer11 = rules.get(0).getInputEntries().get(0);
        UnaryTests inputEntry11 = ruleClauseContainer11.getInputEntry();
        Assert.assertNotNull(inputEntry11);
        Assert.assertEquals("< 10", inputEntry11.getText());
        Assert.assertSame(condition1, ruleClauseContainer11.getInputClause());

        // input expression 2
        RuleInputClauseContainer ruleClauseContainer12 = rules.get(0).getInputEntries().get(1);
        UnaryTests inputEntry12 = ruleClauseContainer12.getInputEntry();
        Assert.assertNotNull(inputEntry12);
        Assert.assertEquals("<= 18-09-1977", inputEntry12.getText());
        Assert.assertSame(condition2, ruleClauseContainer12.getInputClause());

        // output expression 1
        Assert.assertEquals(1, rules.get(0).getOutputEntries().size());
        RuleOutputClauseContainer ruleClauseContainer13 = rules.get(0).getOutputEntries().get(0);
        LiteralExpression outputEntry13 = ruleClauseContainer13.getOutputEntry();
        Assert.assertNotNull(outputEntry13);
        Assert.assertEquals("false", outputEntry13.getText());
        Assert.assertSame(conclusion1, ruleClauseContainer13.getOutputClause());

        // input expression 1
        RuleInputClauseContainer ruleClauseContainer21 = rules.get(1).getInputEntries().get(0);
        UnaryTests inputEntry21 = ruleClauseContainer21.getInputEntry();
        Assert.assertNotNull(inputEntry21);
        Assert.assertEquals("> 10", inputEntry21.getText());
        Assert.assertSame(condition1, ruleClauseContainer21.getInputClause());

        // input expression 2
        RuleInputClauseContainer ruleClauseContainer22 = rules.get(1).getInputEntries().get(1);
        UnaryTests inputEntry22 = ruleClauseContainer22.getInputEntry();
        Assert.assertNotNull(inputEntry22);
        Assert.assertEquals("> 18-09-1977", inputEntry22.getText());
        Assert.assertSame(condition2, ruleClauseContainer22.getInputClause());

        // output expression 1
        Assert.assertEquals(1, rules.get(1).getOutputEntries().size());
        RuleOutputClauseContainer ruleClauseContainer23 = rules.get(1).getOutputEntries().get(0);
        LiteralExpression outputEntry23 = ruleClauseContainer23.getOutputEntry();
        Assert.assertNotNull(outputEntry23);
        Assert.assertEquals("true", outputEntry23.getText());
        Assert.assertSame(conclusion1, ruleClauseContainer23.getOutputClause());

    }

    @Test
    public void testConvertJsonToDmn_no_rules() throws Exception {

        JsonNode testJsonResource = parseJson(JSON_RESOURCE_2);
        DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(testJsonResource, "xyz", 1, new Date());

        Assert.assertNotNull(dmnDefinition);
        Assert.assertEquals(DmnJsonConverter.MODEL_NAMESPACE, dmnDefinition.getNamespace());
        Assert.assertEquals("definition_xyz", dmnDefinition.getId());
        Assert.assertEquals("decisionTableRule1", dmnDefinition.getName());
        Assert.assertEquals(DmnJsonConverter.URI_JSON, dmnDefinition.getTypeLanguage());

        Assert.assertNotNull(dmnDefinition.getDecisions());
        Assert.assertEquals(1, dmnDefinition.getDecisions().size());

        Decision decision = dmnDefinition.getDecisions().get(0);
        Assert.assertNotNull(decision);
        Assert.assertEquals("decTable1", decision.getId());

        DecisionTable decisionTable = (DecisionTable) decision.getExpression();
        Assert.assertNotNull(decisionTable);

        Assert.assertEquals("decisionTable_11", decisionTable.getId());
        Assert.assertEquals(HitPolicy.ANY, decisionTable.getHitPolicy());
        Assert.assertEquals(DecisionTableOrientation.RULE_AS_ROW, decisionTable.getPreferredOrientation());

        List<InputClause> inputClauses = decisionTable.getInputs();
        Assert.assertNotNull(inputClauses);
        Assert.assertEquals(2, inputClauses.size());

        LiteralExpression inputExpression11 = inputClauses.get(0).getInputExpression();
        Assert.assertNotNull(inputExpression11);
        Assert.assertEquals("Order Size", inputExpression11.getLabel());
        Assert.assertEquals("inputExpression_1", inputExpression11.getId());
        Assert.assertEquals("number", inputExpression11.getTypeRef());
        Assert.assertEquals("ordersize", inputExpression11.getText());

        LiteralExpression inputExpression12 = inputClauses.get(1).getInputExpression();
        Assert.assertNotNull(inputExpression12);
        Assert.assertEquals("Registered On", inputExpression12.getLabel());
        Assert.assertEquals("inputExpression_2", inputExpression12.getId());
        Assert.assertEquals("date", inputExpression12.getTypeRef());
        Assert.assertEquals("registered", inputExpression12.getText());

        List<OutputClause> outputClauses = decisionTable.getOutputs();
        Assert.assertNotNull(outputClauses);
        Assert.assertEquals(1, outputClauses.size());

        // Condition 1
        OutputClause outputClause1 = outputClauses.get(0);
        Assert.assertNotNull(outputClause1);
        Assert.assertEquals("Has discount", outputClause1.getLabel());
        Assert.assertEquals("outputExpression_3", outputClause1.getId());
        Assert.assertEquals("newVariable1", outputClause1.getName());
        Assert.assertEquals("boolean", outputClause1.getTypeRef());
    }

    @Test
    public void testConvertJsonToDmn2_OK() throws Exception {

        JsonNode testJsonResource = parseJson(JSON_RESOURCE_3);
        DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(testJsonResource, "xyz", 1, new Date());

        Assert.assertNotNull(dmnDefinition);
    }

    @Test
    public void testConvertJsonToDmn_empty_expressions() throws Exception {

        JsonNode testJsonResource = parseJson(JSON_RESOURCE_4);
        DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(testJsonResource, "xyz", 1, new Date());

        Assert.assertNotNull(dmnDefinition);
    }

    @Test
    public void testConvertJsonToDmn_Condition_order() throws Exception {
        // Test that editor json, which contains the rules in the incorrect order in the rule object,
        // is converted to a dmn model where the rule columns are in the same order as the input/output clauses
        JsonNode testJsonResource = parseJson(JSON_RESOURCE_5);
        DmnDefinition dmnDefinition = new DmnJsonConverter().convertToDmn(testJsonResource, "xyz", 1, new Date());

        Assert.assertNotNull(dmnDefinition);

        DecisionTable decisionTable = (DecisionTable) dmnDefinition.getDecisions().get(0).getExpression();
        
        List<DecisionRule> rules = decisionTable.getRules();
        Assert.assertNotNull(rules);
        Assert.assertEquals(rules.size(), 1);
        Assert.assertNotNull(rules.get(0).getOutputEntries());
        Assert.assertEquals(rules.get(0).getOutputEntries().size(), 3);
        Assert.assertEquals(rules.get(0).getOutputEntries().get(0).getOutputClause().getId(), "outputExpression_14");
        Assert.assertEquals(rules.get(0).getOutputEntries().get(1).getOutputClause().getId(), "outputExpression_13");
        Assert.assertEquals(rules.get(0).getOutputEntries().get(2).getOutputClause().getId(), "outputExpression_15");
    }

    /* Helper methods */
    protected String readJsonToString(String resource) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(resource);
            return IOUtils.toString(is);
        } catch (IOException e) {
            Assert.fail("Could not read " + resource + " : " + e.getMessage());
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected JsonNode parseJson(String resource) {
        String jsonString = readJsonToString(resource);
        try {
            return OBJECT_MAPPER.readTree(jsonString);
        } catch (IOException e) {
            Assert.fail("Could not parse " + resource + " : " + e.getMessage());
        }
        return null;
    }
}