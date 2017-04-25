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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.editor.DecisionTableSaveRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.model.editor.decisiontable.DecisionTableRepresentation;
import org.activiti.app.service.editor.ActivitiDecisionTableService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yvoswillens
 * @author erikwinlof
 */
@RestController
@RequestMapping("/rest/decision-table-models")
public class DecisionTableResource {

  private static final Logger logger = LoggerFactory.getLogger(DecisionTableResource.class);

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected ActivitiDecisionTableService decisionTableService;

  @RequestMapping(value = "/values", method = RequestMethod.GET, produces = "application/json")
  public List<DecisionTableRepresentation> getDecisionTables(HttpServletRequest request) {
    String[] decisionTableIds = request.getParameterValues("decisionTableId");
    if (decisionTableIds == null || decisionTableIds.length == 0) {
      throw new BadRequestException("No decisionTableId parameter(s) provided in the request");
    }
    return decisionTableService.getDecisionTables(decisionTableIds);
  }

  @RequestMapping(value = "/{decisionTableId}", method = RequestMethod.GET, produces = "application/json")
  public DecisionTableRepresentation getDecisionTable(@PathVariable String decisionTableId) {
    return decisionTableService.getDecisionTable(decisionTableId);
  }

  @RequestMapping(value = "/{decisionTableId}/export", method = RequestMethod.GET)
  public void exportDecisionTable(HttpServletResponse response, @PathVariable String decisionTableId) {
    decisionTableService.exportDecisionTable(response, decisionTableId);
  }

  @RequestMapping(value = "/import-decision-table", method = RequestMethod.POST, produces = "application/json")
  public ModelRepresentation importDecisionTable(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    return decisionTableService.importDecisionTable(request, file);
  }

  @RequestMapping(value = "/import-decision-table-text", method = RequestMethod.POST, produces = "application/json")
  public String importDecisionTableText(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    ModelRepresentation decisionTableRepresentation = decisionTableService.importDecisionTable(request, file);
    String json = null;
    try {
      json = objectMapper.writeValueAsString(decisionTableRepresentation);
    } catch (Exception e) {
      logger.error("Error writing imported decision table json", e);
      throw new InternalServerErrorException("Error writing imported decision table representation json");
    }
    return json;
  }

  @RequestMapping(value = "/history/{historyModelId}", method = RequestMethod.GET, produces = "application/json")
  public DecisionTableRepresentation getHistoricDecisionTable(@PathVariable String historyModelId) {
    return decisionTableService.getHistoricDecisionTable(historyModelId);
  }

  @RequestMapping(value = "/history/{historyModelId}/export", method = RequestMethod.GET)
  public void exportHistoricDecisionTable(HttpServletResponse response, @PathVariable String historyModelId) {
    decisionTableService.exportHistoricDecisionTable(response, historyModelId);
  }

  @RequestMapping(value = "/{decisionTableId}", method = RequestMethod.PUT, produces = "application/json")
  public DecisionTableRepresentation saveDecisionTable(@PathVariable String decisionTableId, @RequestBody DecisionTableSaveRepresentation saveRepresentation) {
    return decisionTableService.saveDecisionTable(decisionTableId, saveRepresentation);
  }
}
