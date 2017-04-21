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

import java.util.ArrayList;
import java.util.List;

import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.repository.editor.ModelHistoryRepository;
import org.activiti.app.service.api.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class AbstractModelHistoryResource {

  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected ModelHistoryRepository modelHistoryRepository;

  @Autowired
  protected ObjectMapper objectMapper;

  public ResultListDataRepresentation getModelHistoryCollection(String modelId, Boolean includeLatestVersion) {

    Model model = modelService.getModel(modelId);
    List<ModelHistory> history = modelHistoryRepository.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());
    ResultListDataRepresentation result = new ResultListDataRepresentation();

    List<ModelRepresentation> representations = new ArrayList<ModelRepresentation>();

    // Also include the latest version of the model
    if (Boolean.TRUE.equals(includeLatestVersion)) {
      representations.add(new ModelRepresentation(model));
    }
    if (history.size() > 0) {
      for (ModelHistory modelHistory : history) {
        representations.add(new ModelRepresentation(modelHistory));
      }
      result.setData(representations);
    }

    // Set size and total
    result.setSize(representations.size());
    result.setTotal(Long.valueOf(representations.size()));
    result.setStart(0);
    return result;
  }

  public ModelRepresentation getProcessModelHistory(String modelId, String modelHistoryId) {
    // Check if the user has read-rights on the process-model in order to fetch history
    ModelHistory modelHistory = modelService.getModelHistory(modelId, modelHistoryId);
    return new ModelRepresentation(modelHistory);
  }

}
