/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.core.model.commands;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StartProcessInstanceCmd extends AbstractCommand {

    private String processDefinitionId;
    private Map<String, Object> variables;

    @JsonCreator
    public StartProcessInstanceCmd(@JsonProperty("processDefinitionId") String processDefinitionId,
                                   @JsonProperty("variables") Map<String, Object> variables) {
        super();
        this.processDefinitionId = processDefinitionId;
        this.variables = variables;
    }

    public StartProcessInstanceCmd(String processDefinitionId) {
        super();
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
