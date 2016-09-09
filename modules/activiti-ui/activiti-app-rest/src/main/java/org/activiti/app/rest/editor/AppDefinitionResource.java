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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.model.editor.AppDefinitionPublishRepresentation;
import org.activiti.app.model.editor.AppDefinitionRepresentation;
import org.activiti.app.model.editor.AppDefinitionSaveRepresentation;
import org.activiti.app.model.editor.AppDefinitionUpdateResultRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.editor.AppDefinitionExportService;
import org.activiti.app.service.editor.AppDefinitionImportService;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class AppDefinitionResource {
  
  @Autowired
  protected AppDefinitionExportService appDefinitionExportService;
  
  @Autowired
  protected AppDefinitionImportService appDefinitionImportService;
  
  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected ObjectMapper objectMapper;

  private static final Logger logger = LoggerFactory.getLogger(AppDefinitionResource.class);

  @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.GET, produces = "application/json")
  public AppDefinitionRepresentation getAppDefinition(@PathVariable("modelId") Long modelId) {
    Model model = modelService.getModel(modelId);
    return createAppDefinitionRepresentation(model);
  }

  @RequestMapping(value = "/rest/app-definitions/{modelId}/history/{modelHistoryId}", method = RequestMethod.GET, produces = "application/json")
  public AppDefinitionRepresentation getAppDefinitionHistory(@PathVariable Long modelId, @PathVariable Long modelHistoryId) {
    ModelHistory model = modelService.getModelHistory(modelId, modelHistoryId);
    return createAppDefinitionRepresentation(model);
  }

  @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.PUT, produces = "application/json")
  public AppDefinitionUpdateResultRepresentation updateAppDefinition(@PathVariable("modelId") Long modelId, @RequestBody AppDefinitionSaveRepresentation updatedModel) {

    AppDefinitionUpdateResultRepresentation result = new AppDefinitionUpdateResultRepresentation();

    User user = SecurityUtils.getCurrentUserObject();

    Model model = modelService.getModel(modelId);

    model.setName(updatedModel.getAppDefinition().getName());
    model.setKey(updatedModel.getAppDefinition().getKey());
    model.setDescription(updatedModel.getAppDefinition().getDescription());
    String editorJson = null;
    try {
      editorJson = objectMapper.writeValueAsString(updatedModel.getAppDefinition().getDefinition());
    } catch (Exception e) {
      logger.error("Error while processing app definition json " + modelId, e);
      throw new InternalServerErrorException("App definition could not be saved " + modelId);
    }

    model = modelService.saveModel(model, editorJson, null, false, null, user);

    if (updatedModel.isPublish()) {
      return appDefinitionImportService.publishAppDefinition(modelId, new AppDefinitionPublishRepresentation(null, updatedModel.getForce()));
    } else {
      AppDefinitionRepresentation appDefinition = new AppDefinitionRepresentation(model);
      appDefinition.setDefinition(updatedModel.getAppDefinition().getDefinition());
      result.setAppDefinition(appDefinition);
      return result;
    }
  }

  @RequestMapping(value = "/rest/app-definitions/{modelId}/publish", method = RequestMethod.POST, produces = "application/json")
  public AppDefinitionUpdateResultRepresentation publishAppDefinition(@PathVariable("modelId") Long modelId, @RequestBody AppDefinitionPublishRepresentation publishModel) {
    return appDefinitionImportService.publishAppDefinition(modelId, publishModel);
  }

  @RequestMapping(value = "/rest/app-definitions/{modelId}/export", method = RequestMethod.GET)
  public void exportAppDefinition(HttpServletResponse response, @PathVariable Long modelId) throws IOException {
    appDefinitionExportService.exportAppDefinition(response, modelId);
  }

  @Transactional
  @RequestMapping(value = "/rest/app-definitions/{modelId}/import", method = RequestMethod.POST, produces = "application/json")
  public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @PathVariable Long modelId, @RequestParam("file") MultipartFile file) {
    return appDefinitionImportService.importAppDefinitionNewVersion(request, file, modelId);
  }

  @Transactional
  @RequestMapping(value = "/rest/app-definitions/{modelId}/text/import", method = RequestMethod.POST)
  public String importAppDefinitionText(HttpServletRequest request, @PathVariable Long modelId, @RequestParam("file") MultipartFile file) {

    AppDefinitionRepresentation appDefinitionRepresentation = appDefinitionImportService.importAppDefinitionNewVersion(request, file, modelId);
    String appDefinitionRepresentationJson = null;
    try {
      appDefinitionRepresentationJson = objectMapper.writeValueAsString(appDefinitionRepresentation);
    } catch (Exception e) {
      logger.error("Error while App Definition representation json", e);
      throw new InternalServerErrorException("App definition could not be saved");
    }

    return appDefinitionRepresentationJson;
  }

  @Transactional
  @RequestMapping(value = "/rest/app-definitions/import", method = RequestMethod.POST, produces = "application/json")
  public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    return appDefinitionImportService.importAppDefinition(request, file);
  }

  @Transactional
  @RequestMapping(value = "/rest/app-definitions/text/import", method = RequestMethod.POST)
  public String importAppDefinitionText(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    AppDefinitionRepresentation appDefinitionRepresentation = appDefinitionImportService.importAppDefinition(request, file);
    String appDefinitionRepresentationJson = null;
    try {
      appDefinitionRepresentationJson = objectMapper.writeValueAsString(appDefinitionRepresentation);
    } catch (Exception e) {
      logger.error("Error while App Definition representation json", e);
      throw new InternalServerErrorException("App definition could not be saved");
    }

    return appDefinitionRepresentationJson;
  }
  
  protected AppDefinitionRepresentation createAppDefinitionRepresentation(AbstractModel model) {
    AppDefinition appDefinition = null;
    try {
      appDefinition = objectMapper.readValue(model.getModelEditorJson(), AppDefinition.class);
    } catch (Exception e) {
      logger.error("Error deserializing app " + model.getId(), e);
      throw new InternalServerErrorException("Could not deserialize app definition");
    }
    AppDefinitionRepresentation result = new AppDefinitionRepresentation(model);
    result.setDefinition(appDefinition);
    return result;
  }
}
