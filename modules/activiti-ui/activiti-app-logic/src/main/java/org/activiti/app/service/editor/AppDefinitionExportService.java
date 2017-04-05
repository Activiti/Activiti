package org.activiti.app.service.editor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.domain.editor.AppModelDefinition;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.model.editor.AppDefinitionRepresentation;
import org.activiti.app.repository.editor.ModelRepository;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AppDefinitionExportService {

  private static final Logger logger = LoggerFactory.getLogger(AppDefinitionExportService.class);

  @Autowired
  protected ModelService modelService;

  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ObjectMapper objectMapper;
  
  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  public void exportAppDefinition(HttpServletResponse response, String modelId) throws IOException {

    if (modelId == null) {
      throw new BadRequestException("No application definition id provided");
    }

    Model appModel = modelService.getModel(modelId);
    AppDefinitionRepresentation appRepresentation = createAppDefinitionRepresentation(appModel);

    createAppDefinitionZip(response, appModel, appRepresentation, SecurityUtils.getCurrentUserObject());
  }

  protected void createAppDefinitionZip(HttpServletResponse response, Model appModel, AppDefinitionRepresentation appDefinition, User user) {
    response.setHeader("Content-Disposition", "attachment; filename=" + appDefinition.getName() + ".zip");
    try {
      ServletOutputStream servletOutputStream = response.getOutputStream();
      response.setContentType("application/zip");

      ZipOutputStream zipOutputStream = new ZipOutputStream(servletOutputStream);

      createZipEntry(zipOutputStream, appModel.getName() + ".json", createModelEntryJson(appModel));

      List<AppModelDefinition> modelDefinitions = appDefinition.getDefinition().getModels();
      if (CollectionUtils.isNotEmpty(modelDefinitions)) {
        Map<String, Model> formMap = new HashMap<String, Model>();
        Map<String, Model> decisionTableMap = new HashMap<String, Model>();

        for (AppModelDefinition modelDef : modelDefinitions) {
          Model model = modelService.getModel(modelDef.getId());

          List<Model> referencedModels = modelRepository.findModelsByParentModelId(model.getId());
          for (Model childModel : referencedModels) {
            if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
              formMap.put(childModel.getId(), childModel);

            } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
              decisionTableMap.put(childModel.getId(), childModel);
            }
          }

          createZipEntries(model, "bpmn-models", zipOutputStream);
        }

        for (Model formModel : formMap.values()) {
          createZipEntries(formModel, "form-models", zipOutputStream);
        }
        
        for (Model decisionTableModel : decisionTableMap.values()) {
          createZipEntries(decisionTableModel, "decision-table-models", zipOutputStream);
        }
      }

      zipOutputStream.close();

      // Flush and close stream
      servletOutputStream.flush();
      servletOutputStream.close();

    } catch (Exception e) {
      logger.error("Could not generate app definition zip archive", e);
      throw new InternalServerErrorException("Could not generate app definition zip archive");
    }
  }
  
  protected void createZipEntries(Model model, String directoryName, ZipOutputStream zipOutputStream) throws Exception {
    createZipEntry(zipOutputStream, directoryName + "/" + model.getKey() + ".json", createModelEntryJson(model));

    if (model.getThumbnail() != null) {
      createZipEntry(zipOutputStream, directoryName + "/" + model.getKey() + ".png", model.getThumbnail());
    }
  }

  protected String createModelEntryJson(Model model) {
    ObjectNode modelJson = objectMapper.createObjectNode();
    
    modelJson.put("id", model.getId());
    modelJson.put("name", model.getName());
    modelJson.put("key", model.getKey());
    modelJson.put("description", model.getDescription());
    
    try {
      modelJson.put("editorJson", objectMapper.readTree(model.getModelEditorJson()));
    } catch (Exception e) {
      logger.error("Error exporting model json for id " + model.getId(), e);
      throw new InternalServerErrorException("Error exporting model json for id " + model.getId());
    }
    
    return modelJson.toString();
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

  protected void createZipEntry(ZipOutputStream zipOutputStream, String filename, String content) throws Exception {
    createZipEntry(zipOutputStream, filename, content.getBytes(Charset.forName("UTF-8")));
  }

  protected void createZipEntry(ZipOutputStream zipOutputStream, String filename, byte[] content) throws Exception {
    ZipEntry entry = new ZipEntry(filename);
    zipOutputStream.putNextEntry(entry);
    zipOutputStream.write(content);
    zipOutputStream.closeEntry();
  }
}
