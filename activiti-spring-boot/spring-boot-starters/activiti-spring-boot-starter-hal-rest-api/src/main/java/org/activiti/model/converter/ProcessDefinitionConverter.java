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

package org.activiti.model.converter;

import java.util.List;

import org.activiti.client.model.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**

 */
@Component
public class ProcessDefinitionConverter implements ModelConverter<org.activiti.engine.repository.ProcessDefinition, ProcessDefinition> {

    private final ListConverter listConverter;

    @Autowired
    public ProcessDefinitionConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public ProcessDefinition from(org.activiti.engine.repository.ProcessDefinition source) {
        ProcessDefinition processDefinition = null;
        if (source != null) {
            processDefinition = new ProcessDefinition();
            processDefinition.setId(source.getId());
            processDefinition.setName(source.getName());
            processDefinition.setCategory(source.getCategory());
            processDefinition.setVersion(source.getVersion());
            processDefinition.setDeploymentId(source.getDeploymentId());
        }
        return processDefinition;
    }

    @Override
    public List<ProcessDefinition> from(List<org.activiti.engine.repository.ProcessDefinition> processDefinitions) {
        return listConverter.from(processDefinitions, this);
    }

}
