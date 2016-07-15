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
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.identity.User;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.model.editor.FormSaveRepresentation;
import com.activiti.model.editor.form.FormRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/rest/form-models")
public class FormResource extends BaseModelResource {

  private static final Logger logger = LoggerFactory.getLogger(FormResource.class);

  @Inject
  protected ModelInternalService modelService;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @RequestMapping(value = "/{formId}", method = RequestMethod.GET, produces = "application/json")
  public FormRepresentation getForm(@PathVariable Long formId) {
    Model model = getFormModel(formId, true, false);
    FormRepresentation form = createFormRepresentation(model);
    return form;
  }

  @RequestMapping(value = "/values", method = RequestMethod.GET, produces = "application/json")
  public List<FormRepresentation> getForms(HttpServletRequest request) {
    List<FormRepresentation> formRepresentations = new ArrayList<FormRepresentation>();

    String[] formIds = request.getParameterValues("formId");

    if (formIds == null || formIds.length == 0) {
      throw new BadRequestException("No formIds provided in the request");
    }

    for (String formId : formIds) {
      Model model = getFormModel(Long.valueOf(formId), true, false);

      FormRepresentation form = createFormRepresentation(model);
      formRepresentations.add(form);
    }

    return formRepresentations;
  }

  @RequestMapping(value = "/{formId}/history/{formHistoryId}", method = RequestMethod.GET, produces = "application/json")
  public FormRepresentation getFormHistory(@PathVariable Long formId, @PathVariable Long formHistoryId) {
    ModelHistory model = getFormModelHistory(formId, formHistoryId, true, false);
    FormRepresentation form = createFormRepresentation(model);
    return form;
  }

  @RequestMapping(value = "/{formId}", method = RequestMethod.PUT, produces = "application/json")
  public FormRepresentation saveForm(@PathVariable Long formId, @RequestBody FormSaveRepresentation saveRepresentation) {

    User user = SecurityUtils.getCurrentUserObject();
    Model model = getFormModel(formId, true, true);

    model.setName(saveRepresentation.getFormRepresentation().getName());
    model.setDescription(saveRepresentation.getFormRepresentation().getDescription());

    String editorJson = null;
    try {
      editorJson = objectMapper.writeValueAsString(saveRepresentation.getFormRepresentation().getFormDefinition());
    } catch (Exception e) {
      logger.error("Error while processing form json", e);
      throw new InternalServerErrorException("Form could not be saved " + formId);
    }

    String filteredImageString = saveRepresentation.getFormImageBase64().replace("data:image/png;base64,", "");
    byte[] imageBytes = Base64.decodeBase64(filteredImageString);
    model = modelService.saveModel(model, editorJson, imageBytes, saveRepresentation.isNewVersion(), saveRepresentation.getComment(), user);
    FormRepresentation result = new FormRepresentation(model);
    result.setKey(saveRepresentation.getFormRepresentation().getFormDefinition().getKey());
    result.setFormDefinition(saveRepresentation.getFormRepresentation().getFormDefinition());
    return result;
  }

  protected FormRepresentation createFormRepresentation(AbstractModel model) {
    FormDefinition formDefinition = null;
    try {
      formDefinition = objectMapper.readValue(model.getModelEditorJson(), FormDefinition.class);
    } catch (Exception e) {
      logger.error("Error deserializing form", e);
      throw new InternalServerErrorException("Could not deserialize form definition");
    }
    
    FormRepresentation result = new FormRepresentation(model);
    result.setKey(formDefinition.getKey());
    result.setFormDefinition(formDefinition);
    return result;
  }

  protected Model getFormModel(Long modelId, boolean checkRead, boolean checkEdit) {
    Model model = modelRepository.findOne(modelId);

    if (model == null) {
      NotFoundException processNotFound = new NotFoundException("No model found with the given id: " + modelId);
      processNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
      throw processNotFound;
    }

    return model;
  }

  protected ModelHistory getFormModelHistory(Long modelId, Long modelHistoryId, boolean checkRead, boolean checkEdit) {
    // Check if the user has read-rights on the process-model in order to
    // fetch history
    Model model = getFormModel(modelId, checkRead, checkEdit);
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
