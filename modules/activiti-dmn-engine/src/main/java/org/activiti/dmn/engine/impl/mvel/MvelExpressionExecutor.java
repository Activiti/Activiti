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

import java.io.Serializable;

import org.activiti.dmn.engine.ActivitiDmnExpressionException;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.LiteralExpression;
import org.activiti.dmn.model.OutputClause;
import org.activiti.dmn.model.UnaryTests;
import org.mvel2.MVEL;
import org.mvel2.integration.PropertyHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class MvelExpressionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MvelExpressionExecutor.class);

    public static Boolean executeInputExpression(InputClause inputClause, UnaryTests inputEntry, MvelExecutionContext executionContext) {

        if (inputClause == null) {
            throw new IllegalArgumentException("input clause is required");
        }
        if (inputClause.getInputExpression() == null) {
            throw new IllegalArgumentException("input expression is required");
        }
        if (inputEntry == null) {
            throw new IllegalArgumentException("input entry is required");
        }
        
        for (Class<?> variableClass : executionContext.getPropertyHandlers().keySet()) {
            PropertyHandlerFactory.registerPropertyHandler(variableClass, executionContext.getPropertyHandlers().get(variableClass));
        }

        // check if variable is present MVEL execution context
        executionContext.checkExecutionContext(inputClause.getInputExpression().getText());

        // pre parse expression
        String parsedExpression = MvelConditionExpressionPreParser.parse(inputEntry.getText(), inputClause.getInputExpression().getText());

        // compile MVEL expression
        Serializable compiledExpression = MVEL.compileExpression(parsedExpression, executionContext.getParserContext());

        // execute MVEL expression
        Boolean result;

        try {
            result = MVEL.executeExpression(compiledExpression, executionContext.getStackVariables(), Boolean.class);
        } catch (Exception ex) {
            logger.warn("Error while executing input entry: {}", parsedExpression, ex);
            throw new ActivitiDmnExpressionException("error while executing input entry", parsedExpression, ex);
        }

        return result;
    }

    public static Object executeOutputExpression(OutputClause outputClause, LiteralExpression outputEntry, MvelExecutionContext executionContext) {

        if (outputClause == null) {
            throw new IllegalArgumentException("output clause is required");
        }
        if (outputEntry == null) {
            throw new IllegalArgumentException("output entry is required");
        }

        // compile MVEL expression
        Serializable compiledExpression = MVEL.compileExpression(outputEntry.getText(), executionContext.getParserContext());

        // execute MVEL expression
        Object result = null;

        try {
            result = MVEL.executeExpression(compiledExpression, executionContext.getStackVariables());
        } catch (Exception ex) {
            logger.warn("Error while executing output entry: {}", outputEntry.getText(), ex);
            throw new ActivitiDmnExpressionException("error while executing output entry", outputEntry.getText(), ex);
        }

        return result;
    }
}