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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jbarrez
 */
@RestController
public class ModelBpmnResource extends AbstractModelBpmnResource {

  /**
   * GET /rest/models/{modelId}/bpmn -> Get BPMN 2.0 xml
   */
  @RequestMapping(value = "/rest/models/{processModelId}/bpmn20", method = RequestMethod.GET)
  public void getProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String processModelId) throws IOException {
    super.getProcessModelBpmn20Xml(response, processModelId);
  }

  /**
   * GET /rest/models/{modelId}/history/{processModelHistoryId}/bpmn20 -> Get BPMN 2.0 xml
   */
  @RequestMapping(value = "/rest/models/{processModelId}/history/{processModelHistoryId}/bpmn20", method = RequestMethod.GET)
  public void getHistoricProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String processModelId, @PathVariable String processModelHistoryId) throws IOException {
    super.getHistoricProcessModelBpmn20Xml(response, processModelId, processModelHistoryId);
  }

}
