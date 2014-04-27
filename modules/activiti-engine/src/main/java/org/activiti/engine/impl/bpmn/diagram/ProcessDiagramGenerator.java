package org.activiti.engine.impl.bpmn.diagram;

import org.activiti.bpmn.model.BpmnModel;

import java.io.InputStream;
import java.util.List;

/**
 * This interface declares methods to generate process diagram
 *
 * @author martin.grofcik
 */
public interface ProcessDiagramGenerator {
  /**
   * Generates a diagram of the given process definition, using the
   * diagram interchange information of the process.
   * @param bpmnModel bpmn model to get diagram for
   * @param imageType type of the image to generate.
   * @param highLightedActivities activities to highlight
   * @param highLightedFlows flows to highlight
   */
  InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities, List<String> highLightedFlows);

  public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities);

  public InputStream generatePngDiagram(BpmnModel bpmnModel);

  public InputStream generateJpgDiagram(BpmnModel bpmnModel);

}
