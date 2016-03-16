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

import java.util.Date;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class ExecutionVariableFactory {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionVariableFactory.class);

    public static Object getExecutionVariable(String type, Object expressionResult) {

        if (type == null || expressionResult == null) {
            logger.error("could not create result variable: type {} expresion result {}", type, expressionResult);
            throw new ActivitiDmnException("could not create result variable");
        }

        Object executionVariable = null;

        try {
            if (StringUtils.equals("boolean", type)) {
                if (expressionResult instanceof Boolean) {
                    executionVariable = expressionResult;
                } else {
                    executionVariable = new Boolean(expressionResult.toString());
                }
            } else if (StringUtils.equals("string", type)) {
                if (expressionResult instanceof String) {
                    executionVariable = expressionResult;
                } else {
                    executionVariable = expressionResult.toString();
                }
            } else if (StringUtils.equals("number", type)) {
                if (expressionResult instanceof Double) {
                    executionVariable = expressionResult;
                } else {
                    executionVariable = Double.valueOf(expressionResult.toString());
                }
            } else if (StringUtils.equals("date", type)) {
                if (expressionResult instanceof Date) {
                    executionVariable = expressionResult;
                } else {
                    executionVariable = new DateTime(expressionResult.toString()).toDate();
                }
            } else {
                logger.error("could not create result variable: unrecognized mapping type");
                throw new ActivitiDmnException("could not create result variable: unrecognized mapping type");
            }
        } catch (Exception e) {
            logger.error("could not create result variable", e);
            throw new ActivitiDmnException("Could not create execution variable", e);
        }

        return executionVariable;
    }
}
