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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Yvo Swillens
 */
public class DmnJsonConverter {

    public final static String MODEL_NAMESPACE = "http://activiti.com/dmn";
    public final static String URI_JSON = "http://www.ecma-international.org/ecma-404/";

    protected ObjectMapper objectMapper = new ObjectMapper();

    public DmnDefinition convertToDmn(JsonNode modelNode, String modelId, int modelVersion, Date lastUpdated) {

        DmnDefinition definition = new DmnDefinition();

        definition.setId("definition_"+modelId);
        definition.setName(DmnJsonConverterUtil.getValueAsString("name", modelNode));
        definition.setNamespace(MODEL_NAMESPACE);
        definition.setTypeLanguage(URI_JSON);

        // decision
        //
        Decision decision = new Decision();
        decision.setId(DmnJsonConverterUtil.getValueAsString("key", modelNode));
        decision.setName(DmnJsonConverterUtil.getValueAsString("name", modelNode));
        decision.setDescription(DmnJsonConverterUtil.getValueAsString("description", modelNode));

        definition.addDecision(decision);

        // decision table
        //
        DecisionTable decisionTable = new DecisionTable();
        decisionTable.setId("decisionTable_"+DmnJsonConverterUtil.getValueAsString("id", modelNode));

        if (modelNode.has("hitIndicator")) {
            decisionTable.setHitPolicy(HitPolicy.valueOf(DmnJsonConverterUtil.getValueAsString("hitIndicator", modelNode)));
        } else {
            decisionTable.setHitPolicy(HitPolicy.FIRST);
        }

        // default orientation
        decisionTable.setPreferredOrientation(DecisionTableOrientation.RULE_AS_ROW);

        decision.setExpression(decisionTable);

        // inputs
        processDecisionTable(modelNode, definition, decisionTable);

        return definition;
    }

    public ObjectNode convertToJson(DmnDefinition definition) {

        ObjectNode modelNode = objectMapper.createObjectNode();
        Decision firstDecision = definition.getDecisions().get(0);
        DecisionTable decisionTable = (DecisionTable) firstDecision.getExpression();

        modelNode.put("id", definition.getId());
        modelNode.put("key", firstDecision.getId());
        modelNode.put("name", definition.getName());
        modelNode.put("description", definition.getDescription());
        modelNode.put("hitIndicator", decisionTable.getHitPolicy().name());

        // input expressions
        Map<String, InputClause> inputClauseMap = new HashMap<>();
        ArrayNode inputExpressionsNode = objectMapper.createArrayNode();

        for (InputClause clause : decisionTable.getInputs()) {

            LiteralExpression inputExpression = clause.getInputExpression();
            inputClauseMap.put(inputExpression.getId(), clause);

            ObjectNode inputExpressionNode = objectMapper.createObjectNode();
            inputExpressionNode.put("id", inputExpression.getId());
            inputExpressionNode.put("type", inputExpression.getTypeRef());
            inputExpressionNode.put("label", clause.getLabel());
            inputExpressionNode.put("variableId", inputExpression.getText());

            inputExpressionsNode.add(inputExpressionNode);
        }

        modelNode.put("inputExpressions", inputExpressionsNode);

        // output expressions
        Map<String, OutputClause> outputClauseMap = new HashMap<>();
        ArrayNode outputExpressionsNode = objectMapper.createArrayNode();

        for (OutputClause clause : decisionTable.getOutputs()) {

            outputClauseMap.put(clause.getId(), clause);

            ObjectNode outputExpressionNode = objectMapper.createObjectNode();
            outputExpressionNode.put("id", clause.getId());
            outputExpressionNode.put("type", clause.getTypeRef());
            outputExpressionNode.put("label", clause.getLabel());
            outputExpressionNode.put("variableId", clause.getName());

            outputExpressionsNode.add(outputExpressionNode);
        }

        modelNode.put("outputExpressions", outputExpressionsNode);

        // rules
        ArrayNode rulesNode = objectMapper.createArrayNode();
        for (DecisionRule rule : decisionTable.getRules()) {

            ObjectNode ruleNode = objectMapper.createObjectNode();

            for (RuleInputClauseContainer ruleClauseContainer : rule.getInputEntries()) {
                InputClause inputClause = ruleClauseContainer.getInputClause();
                UnaryTests inputEntry = ruleClauseContainer.getInputEntry();
                ruleNode.put(inputClause.getInputExpression().getId(), inputEntry.getText());
            }

            for (RuleOutputClauseContainer ruleClauseContainer : rule.getOutputEntries()) {
                OutputClause outputClause = ruleClauseContainer.getOutputClause();
                LiteralExpression outputEntry = ruleClauseContainer.getOutputEntry();
                ruleNode.put(outputClause.getId(), outputEntry.getText());
            }

            rulesNode.add(ruleNode);
        }

        modelNode.put("rules", rulesNode);

        return modelNode;
    }

    protected void processDecisionTable(JsonNode modelNode, DmnDefinition definition, DecisionTable decisionTable) {

        if (definition == null || decisionTable == null) {
            return;
        }

        Map<String, InputClause> ruleInputContainerMap = new LinkedHashMap<>();
        Map<String, OutputClause> ruleOutputContainerMap = new LinkedHashMap<>();

        // input expressions
        JsonNode inputExpressions = modelNode.get("inputExpressions");

        if (inputExpressions != null && inputExpressions.isNull() == false) {

            for (JsonNode inputExpressionNode : inputExpressions) {

                InputClause inputClause = new InputClause();
                inputClause.setLabel(DmnJsonConverterUtil.getValueAsString("label", inputExpressionNode));

                String inputExpressionId = DmnJsonConverterUtil.getValueAsString("id", inputExpressionNode);

                LiteralExpression inputExpression = new LiteralExpression();
                inputExpression.setId("inputExpression_" + inputExpressionId);
                inputExpression.setTypeRef(DmnJsonConverterUtil.getValueAsString("type", inputExpressionNode));
                inputExpression.setLabel(DmnJsonConverterUtil.getValueAsString("label", inputExpressionNode));
                inputExpression.setText(DmnJsonConverterUtil.getValueAsString("variableId", inputExpressionNode));

                // add to clause
                inputClause.setInputExpression(inputExpression);

                // add to map
                ruleInputContainerMap.put(inputExpressionId, inputClause);

                decisionTable.addInput(inputClause);
            }
        }

        // output expressions
        JsonNode outputExpressions = modelNode.get("outputExpressions");

        if (outputExpressions != null && outputExpressions.isNull() == false) {

            for (JsonNode outputExpressionNode : outputExpressions) {

                OutputClause outputClause = new OutputClause();

                String outputExpressionId = DmnJsonConverterUtil.getValueAsString("id", outputExpressionNode);

                outputClause.setId("outputExpression_" + outputExpressionId);
                outputClause.setLabel(DmnJsonConverterUtil.getValueAsString("label", outputExpressionNode));
                outputClause.setName(DmnJsonConverterUtil.getValueAsString("variableId", outputExpressionNode));
                outputClause.setTypeRef(DmnJsonConverterUtil.getValueAsString("type", outputExpressionNode));

                // add to map
                ruleOutputContainerMap.put(outputExpressionId, outputClause);

                decisionTable.addOutput(outputClause);
            }
        }

        // rules
        JsonNode rules = modelNode.get("rules");

        if (rules != null && rules.isNull() == false) {

            int ruleCounter = 1;

            for (JsonNode ruleNode : rules) {
                // Make sure the rules are added in the same order that they are defined in the input/output clauses
                DecisionRule rule = new DecisionRule();
                for (String id : ruleInputContainerMap.keySet()) {
                    if (ruleNode.has(id)) {
                        RuleInputClauseContainer ruleInputClauseContainer = new RuleInputClauseContainer();
                        ruleInputClauseContainer.setInputClause(ruleInputContainerMap.get(id));

                        UnaryTests inputEntry = new UnaryTests();
                        inputEntry.setId("inputEntry_" + id + "_" + ruleCounter);
                        inputEntry.setText(ruleNode.get(id).asText());

                        ruleInputClauseContainer.setInputEntry(inputEntry);

                        rule.addInputEntry(ruleInputClauseContainer);
                    }
                }
                for (String id : ruleOutputContainerMap.keySet()) {
                    if (ruleNode.has(id)) {
                        RuleOutputClauseContainer ruleOutputClauseContainer = new RuleOutputClauseContainer();
                        ruleOutputClauseContainer.setOutputClause(ruleOutputContainerMap.get(id));

                        LiteralExpression outputEntry = new LiteralExpression();
                        outputEntry.setId("outputEntry_" + id + "_" + ruleCounter);
                        outputEntry.setText(ruleNode.get(id).asText());

                        ruleOutputClauseContainer.setOutputEntry(outputEntry);

                        rule.addOutputEntry(ruleOutputClauseContainer);
                    }
                }
                ruleCounter++;
                decisionTable.addRule(rule);
            }
        }
    }

    private class RuleMapping {

        private String expressionValue;
        private String expressionId;
        private boolean isCondition;

        public RuleMapping(String expressionValue) {
            this.expressionValue = expressionValue;
        }

        public String getExpressionValue() {
            return expressionValue;
        }

        public String getExpressionId() {
            return expressionId;
        }

        public void setExpressionId(String expressionId) {
            this.expressionId = expressionId;
        }

        public boolean isCondition() {
            return isCondition;
        }

        public void setIsCondition(boolean isCondition) {
            this.isCondition = isCondition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RuleMapping that = (RuleMapping) o;

            return expressionValue.equals(that.expressionValue);

        }

        @Override
        public int hashCode() {
            return expressionValue.hashCode();
        }
    }
}