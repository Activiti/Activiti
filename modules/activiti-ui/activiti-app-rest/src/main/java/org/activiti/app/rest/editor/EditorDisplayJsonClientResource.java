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
package org.activiti.app.rest.editor;

import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.editor.BpmnDisplayJsonConverter;
import org.activiti.bpmn.model.GraphicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class EditorDisplayJsonClientResource {

  @Autowired
  protected ModelService modelService;
  
	@Autowired
	protected BpmnDisplayJsonConverter bpmnDisplayJsonConverter;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@RequestMapping(value = "/rest/models/{processModelId}/model-json", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getModelJSON(@PathVariable String processModelId) {
		ObjectNode displayNode = objectMapper.createObjectNode();
		Model model = modelService.getModel(processModelId);
		bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
		return displayNode;
	}
	
	@RequestMapping(value = "/rest/models/{processModelId}/history/{processModelHistoryId}/model-json", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getModelHistoryJSON(@PathVariable String processModelId, @PathVariable String processModelHistoryId) {
	    ObjectNode displayNode = objectMapper.createObjectNode();
        ModelHistory model = modelService.getModelHistory(processModelId, processModelHistoryId);
        bpmnDisplayJsonConverter.processProcessElements(model, displayNode, new GraphicInfo());
        return displayNode;
    }
}
