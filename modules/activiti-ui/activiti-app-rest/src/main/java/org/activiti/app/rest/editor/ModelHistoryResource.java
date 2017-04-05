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

import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.model.common.BaseRestActionRepresentation;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.model.editor.ReviveModelResultRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModelHistoryResource extends AbstractModelHistoryResource {

  @RequestMapping(value = "/rest/models/{modelId}/history", method = RequestMethod.GET, produces = "application/json")
  public ResultListDataRepresentation getModelHistoryCollection(@PathVariable String modelId, @RequestParam(value = "includeLatestVersion", required = false) Boolean includeLatestVersion) {
    return super.getModelHistoryCollection(modelId, includeLatestVersion);
  }

  @RequestMapping(value = "/rest/models/{modelId}/history/{modelHistoryId}", method = RequestMethod.GET, produces = "application/json")
  public ModelRepresentation getProcessModelHistory(@PathVariable String modelId, @PathVariable String modelHistoryId) {
    return super.getProcessModelHistory(modelId, modelHistoryId);
  }

  @RequestMapping(value = "/rest/models/{modelId}/history/{modelHistoryId}", method = RequestMethod.POST, produces = "application/json")
  public ReviveModelResultRepresentation executeProcessModelHistoryAction(@PathVariable String modelId, @PathVariable String modelHistoryId,
      @RequestBody(required = true) BaseRestActionRepresentation action) {

    // In order to execute actions on a historic process model, write permission is needed
    ModelHistory modelHistory = modelService.getModelHistory(modelId, modelHistoryId);

    if ("useAsNewVersion".equals(action.getAction())) {
      return modelService.reviveProcessModelHistory(modelHistory, SecurityUtils.getCurrentUserObject(), action.getComment());
    } else {
      throw new BadRequestException("Invalid action to execute on model history " + modelHistoryId + ": " + action.getAction());
    }
  }
}
