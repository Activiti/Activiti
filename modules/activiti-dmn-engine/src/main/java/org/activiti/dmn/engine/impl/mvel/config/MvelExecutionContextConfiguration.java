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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class MvelExecutionContextConfiguration {

    protected Map<String, Method> parserConfiguration = new HashMap<String, Method>();
    protected Map<String, Object> contextConfigurationVariables = new HashMap<String, Object>();

    public Map<String, Method> getParserConfiguration() {
        return  parserConfiguration;
    }

    public Map<String, Object> getContextConfigurationVariables() {
        return contextConfigurationVariables;
    }

    public void addParserConfiguration(String methodPlaceHolder, Method targetMethod) {
        this. parserConfiguration.put(methodPlaceHolder, targetMethod);
    }

    public void setParserConfiguration(Map<String, Method> parserConfiguration) {
        this.parserConfiguration = parserConfiguration;
    }

    public void addContextConfigurationVariables(String key, Object value) {
        this.contextConfigurationVariables.put(key, value);
    }
}
