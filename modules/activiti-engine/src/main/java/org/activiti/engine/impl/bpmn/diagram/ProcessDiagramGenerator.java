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

package org.activiti.engine.impl.bpmn.diagram;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;

/**
 * Class to generate an image based the diagram interchange information in a
 * BPMN 2.0 process.
 * 
 * @author Joram Barrez
 */
public class ProcessDiagramGenerator {
  
  protected static final Map<String, DrawInstruction> activityDrawInstructions = new HashMap<String, DrawInstruction>();
  
  // The instructions on how to draw a certain construct is 
  // created statically and stored in a map for performance.
  static {
    // start event
    activityDrawInstructions.put("startEvent", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawNoneStartEvent(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // end event
    activityDrawInstructions.put("endEvent", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawNoneEndEvent(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // error end event
    activityDrawInstructions.put("errorEndEvent", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawErrorEndEvent(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    
    // task
    activityDrawInstructions.put("task", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // user task
    activityDrawInstructions.put("userTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawUserTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // script task
    activityDrawInstructions.put("scriptTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawScriptTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // service task
    activityDrawInstructions.put("serviceTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawServiceTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // receive task
    activityDrawInstructions.put("receiveTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawReceiveTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // send task
    activityDrawInstructions.put("sendTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawSendTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // manual task
    activityDrawInstructions.put("manualTask", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawManualTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // exclusive gateway
    activityDrawInstructions.put("exclusiveGateway", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawExclusiveGateway(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // parallel gateway
    activityDrawInstructions.put("parallelGateway", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawParallelGateway(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // Boundary timer
    activityDrawInstructions.put("boundaryTimer", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCatchingTimerEvent(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // Boundary catch error
    activityDrawInstructions.put("boundaryError", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCatchingErroEvent(activityImpl.getX(), activityImpl.getY(), 
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
    // subprocess
    activityDrawInstructions.put("subProcess", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        Boolean isExpanded = (Boolean) activityImpl.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
        if (isExpanded != null && isExpanded == false) {
          processDiagramCreator.drawCollapsedSubProcess((String) activityImpl.getProperty("name"), 
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
        } else {
          processDiagramCreator.drawExpandedSubProcess((String) activityImpl.getProperty("name"), 
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
        }
      }
    });
    
    // call activity
    activityDrawInstructions.put("callActivity", new DrawInstruction() {
      public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
        //Boolean isExpanded = (Boolean) activityImpl.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
        processDiagramCreator.drawCollapsedCallActivity((String) activityImpl.getProperty("name"), 
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });
    
  }
  
  /**
   *  Generates a PNG diagram image of the given process definition,
   *  using the diagram interchange information of the process.
   */
  public static InputStream generatePngDiagram(ProcessDefinitionEntity processDefinition) {
    return generateDiagram(processDefinition, "png", Collections.<String>emptyList());
  }
  
  /**
   *  Generates a JPG diagram image of the given process definition,
   *  using the diagram interchange information of the process.
   */
  public static InputStream generateJpgDiagram(ProcessDefinitionEntity processDefinition) {
    return generateDiagram(processDefinition, "jpg", Collections.<String>emptyList());
  }

  protected static ProcessDiagramCanvas generateDiagram(ProcessDefinitionEntity processDefinition, List<String> highLightedActivities) {
    ProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(processDefinition);
    for (ActivityImpl activity : processDefinition.getActivities()) {
     drawActivity(processDiagramCanvas, activity, highLightedActivities);

    }
    return processDiagramCanvas;
  }
    
  public static InputStream generateDiagram(ProcessDefinitionEntity processDefinition, String imageType, List<String> highLightedActivities) {
    return generateDiagram(processDefinition, highLightedActivities).generateImage(imageType);
  }
  
  protected static void drawActivity(ProcessDiagramCanvas processDiagramCanvas, ActivityImpl activity, List<String> highLightedActivities) {
    DrawInstruction drawInstruction = activityDrawInstructions.get((String) activity.getProperty("type"));
    if (drawInstruction != null) {
      drawInstruction.draw(processDiagramCanvas, activity);
      if (highLightedActivities.contains(activity.getId())) {
          drawHighLight(processDiagramCanvas, activity);
      }
    }
    for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
      List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
      for (int i=2; i < waypoints.size(); i+=2) { // waypoints.size() minimally 4: x1, y1, x2, y2 
        boolean drawConditionalIndicator = (i == 2) 
          && sequenceFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null
          && !((String) activity.getProperty("type")).toLowerCase().contains("gateway");
        if (i < waypoints.size() - 2) {
          processDiagramCanvas.drawSequenceflowWithoutArrow(waypoints.get(i-2), waypoints.get(i-1), 
                waypoints.get(i), waypoints.get(i+1), drawConditionalIndicator);
        } else {
          processDiagramCanvas.drawSequenceflow(waypoints.get(i-2), waypoints.get(i-1), 
                  waypoints.get(i), waypoints.get(i+1), drawConditionalIndicator);
        }
      }
    }
    for (ActivityImpl nestedActivity : activity.getActivities()) {
      drawActivity(processDiagramCanvas, nestedActivity, highLightedActivities);
    }
  }

  private static void drawHighLight(ProcessDiagramCanvas processDiagramCanvas, ActivityImpl activity) {
      processDiagramCanvas.drawHighLight(activity.getX(), activity.getY(), activity.getWidth(), activity.getHeight());

  }

    protected static ProcessDiagramCanvas initProcessDiagramCanvas(ProcessDefinitionEntity processDefinition) {
    int minX = Integer.MAX_VALUE;
    int maxX = 0;
    int minY = Integer.MAX_VALUE;
    int maxY = 0;
    
    for (ActivityImpl activity : processDefinition.getActivities()) {
      
      // width
      if (activity.getX() + activity.getWidth() > maxX) {
        maxX = activity.getX() + activity.getWidth();
      }
      if (activity.getX() < minX) {
        minX = activity.getX();
      }
      // height
      if (activity.getY() + activity.getHeight() > maxY) {
        maxY = activity.getY() + activity.getHeight();
      }
      if (activity.getY() < minY) {
        minY = activity.getY();
      }
      
      for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
        List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
        for (int i=0; i < waypoints.size(); i+=2) {
          // width
          if (waypoints.get(i) > maxX) {
            maxX = waypoints.get(i);
          }
          // height
          if (waypoints.get(i+1) > maxY) {
            maxY = waypoints.get(i+1);
          }
        }
      }
    }
    return new ProcessDiagramCanvas(maxX + 10, maxY + 10, minX, minY);
  }
  
  protected interface DrawInstruction {
    
    void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl);
    
  }
  
}
