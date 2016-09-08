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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.editor.DecisionTableSaveRepresentation;
import org.activiti.app.model.editor.ModelKeyRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.model.editor.decisiontable.DecisionTableDefinitionRepresentation;
import org.activiti.app.model.editor.decisiontable.DecisionTableRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.util.XmlUtil;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.xml.converter.DmnXMLConverter;
import org.activiti.editor.dmn.converter.DmnJsonConverter;
import org.activiti.engine.identity.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author erikwinlof
 */
@Service
public class ActivitiDecisionTableService extends BaseActivitiModelService {

  private static final Logger logger = LoggerFactory.getLogger(ActivitiDecisionTableService.class);

  protected static final int MIN_FILTER_LENGTH = 1;

  @Autowired
  protected ModelService modelService;

  @Autowired
  protected ObjectMapper objectMapper;

  protected DmnJsonConverter dmnJsonConverter = new DmnJsonConverter();
  protected DmnXMLConverter dmnXmlConverter = new DmnXMLConverter();

  public List<DecisionTableRepresentation> getDecisionTables(String[] decisionTableIds) {
    List<DecisionTableRepresentation> decisionTableRepresentations = new ArrayList<>();
    for (String decisionTableId : decisionTableIds) {
      Model model = getModel(Long.valueOf(decisionTableId), true, false);
      DecisionTableRepresentation decisionTableRepresentation = createDecisionTableRepresentation(model);
      decisionTableRepresentations.add(decisionTableRepresentation);
    }
    return decisionTableRepresentations;
  }

  public ResultListDataRepresentation getDecisionTables(String filter) {
    String validFilter = makeValidFilterText(filter);

    List<Model> models = null;
    
    if (validFilter != null) {
      models = modelRepository.findModelsByModelType(AbstractModel.MODEL_TYPE_DECISION_TABLE, validFilter);

    } else {
      models = modelRepository.findModelsByModelType(AbstractModel.MODEL_TYPE_DECISION_TABLE);
    }

    List<DecisionTableRepresentation> reps = new ArrayList<DecisionTableRepresentation>();

    for (Model model : models) {
      reps.add(new DecisionTableRepresentation(model));
    }

    ResultListDataRepresentation result = new ResultListDataRepresentation(reps);
    result.setTotal(Long.valueOf(models.size()));
    return result;
  }

  public void exportDecisionTable(HttpServletResponse response, Long decisionTableId) {
    exportDecisionTable(response, getModel(decisionTableId, true, false));
  }

  public void exportHistoricDecisionTable(HttpServletResponse response, Long modelHistoryId) {
    // Get the historic model
    ModelHistory modelHistory = modelHistoryRepository.findOne(modelHistoryId);

    // Load model and check we have read rights
    getModel(modelHistory.getModelId(), true, false);

    exportDecisionTable(response, modelHistory);
  }

  public void exportDecisionTableHistory(HttpServletResponse response, Long decisionTableId) {
    exportDecisionTable(response, getModel(decisionTableId, true, false));
  }

  protected void exportDecisionTable(HttpServletResponse response, AbstractModel decisionTableModel) {
    DecisionTableRepresentation decisionTableRepresentation = getDecisionTableRepresentation(decisionTableModel);

    // TODO Validate

    try {

      JsonNode editorJsonNode = objectMapper.readTree(decisionTableModel.getModelEditorJson());

      // URLEncoder.encode will replace spaces with '+', to keep the actual name replacing '+' to '%20'
      String fileName = URLEncoder.encode(decisionTableRepresentation.getName(), "UTF-8").replaceAll("\\+", "%20") + ".dmn";
      response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

      ServletOutputStream servletOutputStream = response.getOutputStream();
      response.setContentType("application/xml");

      DmnDefinition dmnDefinition = dmnJsonConverter.convertToDmn(editorJsonNode, decisionTableModel.getId(), decisionTableModel.getVersion(), decisionTableModel.getLastUpdated());
      byte[] xmlBytes = dmnXmlConverter.convertToXML(dmnDefinition);

      BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));

      byte[] buffer = new byte[8096];
      while (true) {
        int count = in.read(buffer);
        if (count == -1)
          break;
        servletOutputStream.write(buffer, 0, count);
      }

      // Flush and close stream
      servletOutputStream.flush();
      servletOutputStream.close();

    } catch (Exception e) {
      logger.error("Could not export decision table model", e);
      throw new InternalServerErrorException("Could not export decision table model");
    }
  }

  public ModelRepresentation importDecisionTable(HttpServletRequest request, MultipartFile file) {

    String fileName = file.getOriginalFilename();
    if (fileName != null && (fileName.endsWith(".dmn") || fileName.endsWith(".xml"))) {
      try {

        XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
        InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);

        DmnDefinition dmnDefinition = dmnXmlConverter.convertToDmnModel(xtr);
        ObjectNode editorJsonNode = dmnJsonConverter.convertToJson(dmnDefinition);

        // remove id to avoid InvalidFormatException when deserializing
        editorJsonNode.remove("id");

        ModelRepresentation modelRepresentation = new ModelRepresentation();
        modelRepresentation.setName(dmnDefinition.getName());
        modelRepresentation.setDescription(dmnDefinition.getDescription());
        modelRepresentation.setModelType(AbstractModel.MODEL_TYPE_DECISION_TABLE);
        Model model = modelService.createModel(modelRepresentation, editorJsonNode.toString(), SecurityUtils.getCurrentUserObject());
        return new ModelRepresentation(model);

      } catch (Exception e) {
        logger.error("Could not import decision table model", e);
        throw new InternalServerErrorException("Could not import decision table model");
      }
    } else {
      throw new BadRequestException("Invalid file name, only .dmn or .xml files are supported not " + fileName);
    }

  }

  protected String makeValidFilterText(String filterText) {
    String validFilter = null;

    if (filterText != null) {
      String trimmed = StringUtils.trim(filterText);
      if (trimmed.length() >= MIN_FILTER_LENGTH) {
        validFilter = "%" + trimmed.toLowerCase() + "%";
      }
    }
    return validFilter;
  }

  public Model getDecisionTableModel(Long decisionTableId) {
    return getModel(decisionTableId, true, false);
  }

  public DecisionTableRepresentation getDecisionTable(Long decisionTableId) {
    return createDecisionTableRepresentation(getDecisionTableModel(decisionTableId));
  }

  public DecisionTableRepresentation getDecisionTableRepresentation(AbstractModel decisionTableModel) {
    return createDecisionTableRepresentation(decisionTableModel);
  }

  public DecisionTableRepresentation getHistoricDecisionTable(Long modelHistoryId) {
    // Get the historic model
    ModelHistory modelHistory = modelHistoryRepository.findOne(modelHistoryId);

    // Load model and check we have read rights
    getModel(modelHistory.getModelId(), true, false);

    return createDecisionTableRepresentation(modelHistory);
  }

  protected DecisionTableRepresentation createDecisionTableRepresentation(AbstractModel model) {
    DecisionTableDefinitionRepresentation decisionTableDefinitionRepresentation = null;
    try {
      decisionTableDefinitionRepresentation = objectMapper.readValue(model.getModelEditorJson(), DecisionTableDefinitionRepresentation.class);
    } catch (Exception e) {
      logger.error("Error deserializing decision table", e);
      throw new InternalServerErrorException("Could not deserialize decision table definition");
    }
    DecisionTableRepresentation result = new DecisionTableRepresentation(model);
    result.setDecisionTableDefinition(decisionTableDefinitionRepresentation);
    return result;
  }

  public DecisionTableRepresentation saveDecisionTable(Long decisionTableId, DecisionTableSaveRepresentation saveRepresentation) {

    User user = SecurityUtils.getCurrentUserObject();
    Model model = getModel(decisionTableId, false, false);
    
    String decisionKey = saveRepresentation.getDecisionTableRepresentation().getKey();
    ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), decisionKey);
    if (modelKeyInfo.isKeyAlreadyExists()) {
      throw new BadRequestException("Provided model key already exists: " + decisionKey);
    }

    model.setName(saveRepresentation.getDecisionTableRepresentation().getName());
    model.setKey(decisionKey);
    model.setDescription(saveRepresentation.getDecisionTableRepresentation().getDescription());

    String editorJson = null;
    try {
      editorJson = objectMapper.writeValueAsString(saveRepresentation.getDecisionTableRepresentation().getDecisionTableDefinition());
    } catch (Exception e) {
      logger.error("Error while processing decision table json", e);
      throw new InternalServerErrorException("Decision table could not be saved " + decisionTableId);
    }

    String filteredImageString = saveRepresentation.getDecisionTableImageBase64().replace("data:image/png;base64,", "");
    byte[] imageBytes = Base64.decodeBase64(filteredImageString);
    model = modelService.saveModel(model, editorJson, imageBytes, saveRepresentation.isNewVersion(), saveRepresentation.getComment(), user);
    DecisionTableRepresentation result = new DecisionTableRepresentation(model);
    result.setDecisionTableDefinition(saveRepresentation.getDecisionTableRepresentation().getDecisionTableDefinition());
    return result;
  }
}
