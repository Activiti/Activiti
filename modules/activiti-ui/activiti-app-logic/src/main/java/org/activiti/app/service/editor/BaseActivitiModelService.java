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
package org.activiti.app.service.editor;

import java.util.ArrayList;
import java.util.List;

import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.repository.editor.ModelHistoryRepository;
import org.activiti.app.repository.editor.ModelRepository;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseActivitiModelService {

  private static final Logger logger = LoggerFactory.getLogger(BaseActivitiModelService.class);

  protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";

  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ModelHistoryRepository modelHistoryRepository;

  @Autowired
  protected IdentityService identityService;

  protected Model getModel(Long modelId, boolean checkRead, boolean checkEdit) {
    Model model = modelRepository.findOne(modelId);

    if (model == null) {
      NotFoundException processNotFound = new NotFoundException("No model found with the given id: " + modelId);
      processNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
      throw processNotFound;
    }

    return model;
  }

  protected ModelHistory getModelHistory(Long modelId, Long modelHistoryId, boolean checkRead, boolean checkEdit) {
    // Check if the user has read-rights on the process-model in order to fetch history
    Model model = getModel(modelId, checkRead, checkEdit);
    ModelHistory modelHistory = modelHistoryRepository.findOne(modelHistoryId);

    // Check if history corresponds to the current model and is not deleted
    if (modelHistory == null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
      throw new NotFoundException("Model history not found: " + modelHistoryId);
    }
    return modelHistory;
  }
  
  protected List<String> getGroupIds(String userId) {
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

}
