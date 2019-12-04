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
package org.activiti.bpmn.converter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.export.BPMNDIExport;
import org.activiti.bpmn.converter.export.CollaborationExport;
import org.activiti.bpmn.converter.export.DataStoreExport;
import org.activiti.bpmn.converter.export.DefinitionsRootExport;
import org.activiti.bpmn.converter.export.ProcessExport;
import org.activiti.bpmn.converter.export.SignalAndMessageDefinitionExport;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataObject;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class SubprocessXMLConverter extends BpmnXMLConverter {

  protected static final Logger LOGGER = LoggerFactory.getLogger(SubprocessXMLConverter.class);

  @Override
  public byte[] convertToXML(BpmnModel model, String encoding) {
    try {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      XMLOutputFactory xof = XMLOutputFactory.newInstance();
      OutputStreamWriter out = new OutputStreamWriter(outputStream, encoding);

      XMLStreamWriter writer = xof.createXMLStreamWriter(out);
      XMLStreamWriter xtw = new IndentingXMLStreamWriter(writer);

      DefinitionsRootExport.writeRootElement(model, xtw, encoding);
      CollaborationExport.writePools(model, xtw);
      DataStoreExport.writeDataStores(model, xtw);
      SignalAndMessageDefinitionExport.writeSignalsAndMessages(model, xtw);

      for (Process process : model.getProcesses()) {

        if (process.getFlowElements().isEmpty() && process.getLanes().isEmpty()) {
          // empty process, ignore it
          continue;
        }

        ProcessExport.writeProcess(process, xtw);

        for (FlowElement flowElement : process.getFlowElements()) {
          createXML(flowElement, model, xtw);
        }

        for (Artifact artifact : process.getArtifacts()) {
          createXML(artifact, model, xtw);
        }

        // end process element
        xtw.writeEndElement();
      }

      // refactor each subprocess into a separate Diagram
      List<BpmnModel> subModels = parseSubModels(model);
      for (BpmnModel tempModel : subModels) {
        if (!tempModel.getFlowLocationMap().isEmpty() || !tempModel.getLocationMap().isEmpty()) {
          BPMNDIExport.writeBPMNDI(tempModel, xtw);
        }
      }

      // end definitions root element
      xtw.writeEndElement();
      xtw.writeEndDocument();

      xtw.flush();
      byte[] bytes = outputStream.toByteArray().clone();

      // cleanup
      outputStream.close();
      xtw.close();

      return bytes;
    } catch (Exception e) {
      LOGGER.error("Error writing BPMN XML", e);
      throw new XMLException("Error writing BPMN XML", e);
    }
  }

  protected List<BpmnModel> parseSubModels(BpmnModel model) {
    List<BpmnModel> subModels = new ArrayList<BpmnModel>();

    // find all subprocesses
    Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
    Map<String, GraphicInfo> locations = new HashMap<String, GraphicInfo>();
    Map<String, List<GraphicInfo>> flowLocations = new HashMap<String, List<GraphicInfo>>();
    Map<String, GraphicInfo> labelLocations = new HashMap<String, GraphicInfo>();

    locations.putAll(model.getLocationMap());
    flowLocations.putAll(model.getFlowLocationMap());
    labelLocations.putAll(model.getLabelLocationMap());

    // include main process as separate model
    BpmnModel mainModel = new BpmnModel();
    // set main process in submodel to subprocess
    mainModel.addProcess(model.getMainProcess());

    String elementId = null;
    for (FlowElement element : flowElements) {
      elementId = element.getId();
      if (element instanceof SubProcess) {
        subModels.addAll(parseSubModels(element, locations, flowLocations, labelLocations));
      }
      
      if (element instanceof SequenceFlow && null != flowLocations.get(elementId)) {
        // must be an edge
        mainModel.getFlowLocationMap().put(elementId, flowLocations.get(elementId));
      } else {
        // do not include data objects because they do not have a corresponding shape in the BPMNDI data
        if (!(element instanceof DataObject) && null != locations.get(elementId)) {
          // must be a shape
          mainModel.getLocationMap().put(elementId, locations.get(elementId));
        }
      }
      // also check for any labels
      if (null != labelLocations.get(elementId)) {
        mainModel.getLabelLocationMap().put(elementId, labelLocations.get(elementId));
      }
    }
    // add main process model to list
    subModels.add(mainModel);

    return subModels;
  }

  private List<BpmnModel> parseSubModels(FlowElement subElement, Map<String, GraphicInfo> locations, 
                                         Map<String, List<GraphicInfo>> flowLocations, Map<String, GraphicInfo> labelLocations) {
    List<BpmnModel> subModels = new ArrayList<BpmnModel>();
    BpmnModel subModel = new BpmnModel();
    String elementId = null;

    // find nested subprocess models
    Collection<FlowElement> subFlowElements = ((SubProcess)subElement).getFlowElements();
    // set main process in submodel to subprocess
    Process newMainProcess = new Process();
    newMainProcess.setId(subElement.getId());
    newMainProcess.getFlowElements().addAll(subFlowElements);
    newMainProcess.getArtifacts().addAll(((SubProcess)subElement).getArtifacts());
    subModel.addProcess(newMainProcess);

    for (FlowElement element : subFlowElements) {
      elementId = element.getId();
      if (element instanceof SubProcess) {
        subModels.addAll(parseSubModels(element, locations, flowLocations, labelLocations));
      }
      
      if (element instanceof SequenceFlow && null != flowLocations.get(elementId)) {
        // must be an edge
        subModel.getFlowLocationMap().put(elementId, flowLocations.get(elementId));
      } else {
        // do not include data objects because they do not have a corresponding shape in the BPMNDI data
        if (!(element instanceof DataObject) && null != locations.get(elementId)) {
          // must be a shape
          subModel.getLocationMap().put(elementId, locations.get(elementId));
        }
      }
      // also check for any labels
      if (null != labelLocations.get(elementId)) {
        subModel.getLabelLocationMap().put(elementId, labelLocations.get(elementId));
      }
    }
    subModels.add(subModel);

    return subModels;
  }
}