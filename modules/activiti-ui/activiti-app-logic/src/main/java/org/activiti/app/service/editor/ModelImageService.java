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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.app.domain.editor.Model;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.activiti.image.ImageGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ModelImageService {

  private final Logger log = LoggerFactory.getLogger(ModelImageService.class);

  private static float THUMBNAIL_WIDTH = 300f;

  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  public void generateThumbnailImage(Model model, ObjectNode editorJsonNode) {
    try {

      BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);

      double scaleFactor = 1.0;
      GraphicInfo diagramInfo = calculateDiagramSize(bpmnModel);
      if (diagramInfo.getWidth() > THUMBNAIL_WIDTH) {
        scaleFactor = diagramInfo.getWidth() / THUMBNAIL_WIDTH;
        scaleDiagram(bpmnModel, scaleFactor);
      }

      BufferedImage modelImage = ImageGenerator.createImage(bpmnModel, scaleFactor);
      if (modelImage != null) {
        byte[] thumbnailBytes = ImageGenerator.createByteArrayForImage(modelImage, "png");
        model.setThumbnail(thumbnailBytes);
      }
    } catch (Exception e) {
      log.error("Error creating thumbnail image " + model.getId(), e);
    }
  }

  protected GraphicInfo calculateDiagramSize(BpmnModel bpmnModel) {
    GraphicInfo diagramInfo = new GraphicInfo();

    for (Pool pool : bpmnModel.getPools()) {
      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
      double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
      double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();

      if (elementMaxX > diagramInfo.getWidth()) {
        diagramInfo.setWidth(elementMaxX);
      }
      if (elementMaxY > diagramInfo.getHeight()) {
        diagramInfo.setHeight(elementMaxY);
      }
    }

    for (Process process : bpmnModel.getProcesses()) {
      calculateWidthForFlowElements(process.getFlowElements(), bpmnModel, diagramInfo);
      calculateWidthForArtifacts(process.getArtifacts(), bpmnModel, diagramInfo);
    }
    return diagramInfo;
  }

  protected void scaleDiagram(BpmnModel bpmnModel, double scaleFactor) {
    for (Pool pool : bpmnModel.getPools()) {
      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
      scaleGraphicInfo(graphicInfo, scaleFactor);
    }

    for (Process process : bpmnModel.getProcesses()) {
      scaleFlowElements(process.getFlowElements(), bpmnModel, scaleFactor);
      scaleArtifacts(process.getArtifacts(), bpmnModel, scaleFactor);
      for (Lane lane : process.getLanes()) {
        scaleGraphicInfo(bpmnModel.getGraphicInfo(lane.getId()), scaleFactor);
      }
    }
  }

  protected void calculateWidthForFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
    for (FlowElement flowElement : elementList) {
      List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
      if (flowElement instanceof SequenceFlow) {
        List<GraphicInfo> flowGraphics = bpmnModel.getFlowLocationGraphicInfo(flowElement.getId());
        if (flowGraphics != null && flowGraphics.size() > 0) {
          graphicInfoList.addAll(flowGraphics);
        }
      } else {
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowElement.getId());
        if (graphicInfo != null) {
          graphicInfoList.add(graphicInfo);
        }
      }

      processGraphicInfoList(graphicInfoList, diagramInfo);
    }
  }

  protected void calculateWidthForArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
    for (Artifact artifact : artifactList) {
      List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
      if (artifact instanceof Association) {
        graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(artifact.getId()));
      } else {
        graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
      }

      processGraphicInfoList(graphicInfoList, diagramInfo);
    }
  }

  protected void processGraphicInfoList(List<GraphicInfo> graphicInfoList, GraphicInfo diagramInfo) {
    for (GraphicInfo graphicInfo : graphicInfoList) {
      double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
      double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();

      if (elementMaxX > diagramInfo.getWidth()) {
        diagramInfo.setWidth(elementMaxX);
      }
      if (elementMaxY > diagramInfo.getHeight()) {
        diagramInfo.setHeight(elementMaxY);
      }
    }
  }

  protected void scaleFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, double scaleFactor) {
    for (FlowElement flowElement : elementList) {
      List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
      if (flowElement instanceof SequenceFlow) {
        List<GraphicInfo> flowList = bpmnModel.getFlowLocationGraphicInfo(flowElement.getId());
        if (flowList != null) {
          graphicInfoList.addAll(flowList);
        }
      } else {
        graphicInfoList.add(bpmnModel.getGraphicInfo(flowElement.getId()));
      }

      scaleGraphicInfoList(graphicInfoList, scaleFactor);

      if (flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        scaleFlowElements(subProcess.getFlowElements(), bpmnModel, scaleFactor);
      }
    }
  }

  protected void scaleArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, double scaleFactor) {
    for (Artifact artifact : artifactList) {
      List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
      if (artifact instanceof Association) {
        List<GraphicInfo> flowList = bpmnModel.getFlowLocationGraphicInfo(artifact.getId());
        if (flowList != null) {
          graphicInfoList.addAll(flowList);
        }
      } else {
        graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
      }

      scaleGraphicInfoList(graphicInfoList, scaleFactor);
    }
  }

  protected void scaleGraphicInfoList(List<GraphicInfo> graphicInfoList, double scaleFactor) {
    for (GraphicInfo graphicInfo : graphicInfoList) {
      scaleGraphicInfo(graphicInfo, scaleFactor);
    }
  }

  protected void scaleGraphicInfo(GraphicInfo graphicInfo, double scaleFactor) {
    graphicInfo.setX(graphicInfo.getX() / scaleFactor);
    graphicInfo.setY(graphicInfo.getY() / scaleFactor);
    graphicInfo.setWidth(graphicInfo.getWidth() / scaleFactor);
    graphicInfo.setHeight(graphicInfo.getHeight() / scaleFactor);
  }
}
