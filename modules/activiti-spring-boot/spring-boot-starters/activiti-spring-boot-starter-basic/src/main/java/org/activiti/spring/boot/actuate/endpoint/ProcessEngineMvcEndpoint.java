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
package org.activiti.spring.boot.actuate.endpoint;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Renders a valid running BPMN process definition as a BPMN diagram.
 *
 * This is duplicative of the functionality in the full REST API implementation.
 *
 * @author Joram Barrez
 * @author Josh Long
 */
public class ProcessEngineMvcEndpoint extends EndpointMvcAdapter {

    private final RepositoryService repositoryService;

    public ProcessEngineMvcEndpoint(ProcessEngineEndpoint processEngineEndpoint, RepositoryService repositoryService) {
        super(processEngineEndpoint);
        this.repositoryService = repositoryService;
    }

    /**
     * Look up the process definition by key. For example,
     * this is <A href="http://localhost:8080/activiti/processes/fulfillmentProcess">process-diagram for</A>
     * a process definition named {@code fulfillmentProcess}.
     */
    @RequestMapping(value = "/processes/{processDefinitionKey:.*}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity processDefinitionDiagram(@PathVariable String processDefinitionKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        if (processDefinition == null) {
            return ResponseEntity.status(NOT_FOUND).body(null);
        }

        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        if (bpmnModel.getLocationMap().size() == 0) {
            BpmnAutoLayout autoLayout = new BpmnAutoLayout(bpmnModel);
            autoLayout.execute();
        }

        InputStream is = processDiagramGenerator.generateJpgDiagram(bpmnModel);
        return ResponseEntity.ok(new InputStreamResource(is));
    }
}
