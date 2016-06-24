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
package com.activiti.rest.editor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.service.editor.ModelInternalService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class AbstractModelHistoryResource extends BaseModelResource {

	@Inject
	protected ModelInternalService modelService;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
    public ResultListDataRepresentation getModelHistoryCollection(Long modelId, Boolean includeLatestVersion) {

        Model model = getModel(modelId, true, false);
        List<ModelHistory> history = historyRepository.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());
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
        result.setTotal(representations.size());
        result.setStart(0);
        return result;
    }
    
    public ModelRepresentation getProcessModelHistory(Long modelId, Long modelHistoryId) {
        // Check if the user has read-rights on the process-model in order to fetch history
        ModelHistory modelHistory = getModelHistory(modelId, modelHistoryId, true, false);
        return new ModelRepresentation(modelHistory);
    }
    
}
