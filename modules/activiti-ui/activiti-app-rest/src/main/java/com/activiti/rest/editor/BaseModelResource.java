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

import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.repository.editor.ModelHistoryRepository;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;

/**
 * @author Frederik Heremans
 */
public class BaseModelResource {

  protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";

  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ModelHistoryRepository historyRepository;

  protected Model getModel(Long modelId, boolean checkRead, boolean checkEdit) {
    Model model = modelRepository.findOne(modelId);

    if (model == null) {
      NotFoundException modelNotFound = new NotFoundException("No model found with the given id: " + modelId);
      modelNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
      throw modelNotFound;
    }

    return model;
  }

  protected Model getProcessModelForOwner(Long processModelId) {
    Model model = modelRepository.findOne(processModelId);

    if (model == null) {
      throw new NotFoundException("No process model found with the given id: " + processModelId);
    }

    User currentUser = SecurityUtils.getCurrentUserObject();

    if (!model.getCreatedBy().equals(currentUser)) {
      throw new NotPermittedException("You are not permitted to access this process model");
    }

    return model;
  }

  protected ModelHistory getModelHistory(Long modelId, Long modelHistoryId, boolean checkRead, boolean checkEdit) {
    // Check if the user has read-rights on the process-model in order to fetch history
    Model model = getModel(modelId, checkRead, checkEdit);
    ModelHistory modelHistory = historyRepository.findOne(modelHistoryId);

    // Check if history corresponds to the current model and is not deleted
    if (modelHistory == null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
      throw new NotFoundException("Process model history not found: " + modelHistoryId);
    }
    return modelHistory;
  }

  protected Model getParentModel(Long parentModelId) {
    Model model = modelRepository.findOne(parentModelId);
    if (model.getReferenceId() != null) {
      return getParentModel(model.getReferenceId());
    }
    return model;
  }

}
