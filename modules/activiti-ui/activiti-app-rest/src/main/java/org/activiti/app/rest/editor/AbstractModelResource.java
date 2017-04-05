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

import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.NonJsonResourceNotFoundException;
import org.activiti.app.util.XmlUtil;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AbstractModelResource {

  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected ObjectMapper objectMapper;

  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

  public ModelRepresentation getModel(String modelId) {
    Model model = modelService.getModel(modelId);
    ModelRepresentation result = new ModelRepresentation(model);

    return result;
  }

  public byte[] getModelThumbnail(String modelId) {
    Model model = modelService.getModel(modelId);

    if (model == null) {
      throw new NonJsonResourceNotFoundException();
    }

    return model.getThumbnail();
  }

  public ModelRepresentation importNewVersion(String modelId, MultipartFile file) {

    Model processModel = modelService.getModel(modelId);
    User currentUser = SecurityUtils.getCurrentUserObject();

    String fileName = file.getOriginalFilename();
    if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
      try {
        XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
        InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xtr);
        if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
          throw new BadRequestException("No process found in definition " + fileName);
        }

        if (bpmnModel.getLocationMap().size() == 0) {
          throw new BadRequestException("No required BPMN DI information found in definition " + fileName);
        }

        ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);

        AbstractModel savedModel = modelService.saveModel(modelId, processModel.getName(), processModel.getKey(),
            processModel.getDescription(), modelNode.toString(), true, "Version import via REST service", currentUser);
        return new ModelRepresentation(savedModel);

      } catch (BadRequestException e) {
        throw e;

      } catch (Exception e) {
        throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
      }
    } else {
      throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
    }
  }

}
