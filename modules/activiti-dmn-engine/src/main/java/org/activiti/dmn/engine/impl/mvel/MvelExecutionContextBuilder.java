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
package org.activiti.dmn.engine.impl.mvel;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.audit.DecisionExecutionAuditUtil;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.OutputClause;
import org.mvel2.ParserContext;
import org.mvel2.integration.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class MvelExecutionContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MvelExecutionContextBuilder.class);

    public static MvelExecutionContext build(DmnDefinition definition, Map<String, Object> inputVariables, DmnEngineConfiguration dmnEngineConfiguration) {

        MvelExecutionContext executionContext = new MvelExecutionContext();

        // initialize audit trail
        executionContext.setAuditContainer(DecisionExecutionAuditUtil.initializeRuleExecutionAudit(definition, inputVariables));

        ParserContext parserContext = new ParserContext();

        // add custom functions to context
        Map<String, Method> customExpressionFunctions = dmnEngineConfiguration.getCustomExpressionFunctions();
        if (customExpressionFunctions != null && !customExpressionFunctions.isEmpty()) {
            for (Map.Entry<String, Method> config : customExpressionFunctions.entrySet()) {
                parserContext.addImport(config.getKey(), config.getValue());
            }
        }

        executionContext.setParserContext(parserContext);
        
        Map<Class<?>, PropertyHandler> propertyHandlers = dmnEngineConfiguration.getCustomPropertyHandlers();
        if (propertyHandlers != null) {
            for (Class<?> variableClass : propertyHandlers.keySet()) {
                executionContext.addPropertyHandler(variableClass, propertyHandlers.get(variableClass));
            }
        }

        preProcessInputVariables(definition, inputVariables);

        executionContext.setStackVariables(inputVariables);

        logger.debug("Execution Context created");

        return executionContext;
    }

    protected static void preProcessInputVariables(DmnDefinition definition, Map<String, Object> inputVariables) {

        if (inputVariables == null) {
            inputVariables = new HashMap<String, Object>();
        }

        // check if there are input expressions that refer to none existing input variables
        // that need special handling
        for (InputClause inputClause : definition.getCurrentDecisionTable().getInputs()) {

            if (!inputVariables.containsKey(inputClause.getInputExpression().getText()) &&
                    "boolean".equals(inputClause.getInputExpression().getTypeRef())) {

                inputVariables.put(inputClause.getInputExpression().getText(), Boolean.FALSE);
            }
        }

        // check if there are output expressions that refer to none existing input variables
        // in that case create them with default values
        for (OutputClause outputClause : definition.getCurrentDecisionTable().getOutputs()) {

            if (!inputVariables.containsKey(outputClause.getName())
                    || inputVariables.get(outputClause.getName()) == null) {

                if ("number".equals(outputClause.getTypeRef())) {
                    inputVariables.put(outputClause.getName(), 0D);
                } else if ("date".equals(outputClause.getTypeRef())) {
                    inputVariables.put(outputClause.getName(), new Date());
                } else {
                    inputVariables.put(outputClause.getName(), "");
                }
            }
        }
    }
}
