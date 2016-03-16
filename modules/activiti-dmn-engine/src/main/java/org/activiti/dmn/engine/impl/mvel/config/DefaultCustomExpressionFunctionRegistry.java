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
package org.activiti.dmn.engine.impl.mvel.config;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.engine.CustomExpressionFunctionRegistry;
import org.activiti.dmn.engine.impl.mvel.extension.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class DefaultCustomExpressionFunctionRegistry implements CustomExpressionFunctionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCustomExpressionFunctionRegistry.class);

    protected static Map<String, Method> customFunctionConfigurations = new HashMap<String, Method>();

    static {
        addCustomFunction("fn_date", getMethod(DateUtil.class, "toDate", String.class));
        addCustomFunction("fn_addDate", getMethod(DateUtil.class, "addDate", Date.class, Integer.class, Integer.class, Integer.class));
        addCustomFunction("fn_subtractDate", getMethod(DateUtil.class, "subtractDate", Date.class, Integer.class, Integer.class, Integer.class));
        addCustomFunction("fn_now", getMethod(DateUtil.class, "getCurrentDate"));
    }

    @Override
    public Map<String, Method> getCustomExpressionMethods() {
        return customFunctionConfigurations;
    }

    protected static void addCustomFunction(String methodName, Method methodRef) {
        customFunctionConfigurations.put(methodName, methodRef);
    }

    protected static Method getMethod(Class classRef, String methodName, Class... methodParam) {
        try {
            logger.debug("adding method to MVEL: "+classRef.getName() + " " +methodName+ " with " +methodParam.length + " parameters");
            return classRef.getMethod(methodName, methodParam);
        } catch (NoSuchMethodException nsme) {
            logger.error("Could not find method for name: "+methodName, nsme);
        }

        return null;
    }
}
