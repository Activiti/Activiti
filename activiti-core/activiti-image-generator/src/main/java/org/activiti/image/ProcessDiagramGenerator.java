/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.image;

import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;

/**
 * This interface declares methods to generate process diagram
 */
public interface ProcessDiagramGenerator {

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process.
     * If there is no interchange information available, an ActivitiInterchangeInfoNotFoundException is thrown.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     * @param activityFontName override the default activity font
     * @param labelFontName override the default label font
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                List<String> highLightedActivities,
                                List<String> highLightedFlows,
                                String activityFontName,
                                String labelFontName,
                                String annotationFontName);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process,
     * or the default diagram image, if generateDefaultDiagram param is true.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     * @param activityFontName override the default activity font
     * @param labelFontName override the default label font
     * @param generateDefaultDiagram true if a default diagram should be generated if there is no graphic info available
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                List<String> highLightedActivities,
                                List<String> highLightedFlows,
                                String activityFontName,
                                String labelFontName,
                                String annotationFontName,
                                boolean generateDefaultDiagram);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process,
     * or the default diagram image, if generateDefaultDiagram param is true.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     * @param activityFontName override the default activity font
     * @param labelFontName override the default label font
     * @param generateDefaultDiagram true if a default diagram should be generated if there is no graphic info available
     * @param defaultDiagramImageFileName override the default diagram image file name
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                List<String> highLightedActivities,
                                List<String> highLightedFlows,
                                String activityFontName,
                                String labelFontName,
                                String annotationFontName,
                                boolean generateDefaultDiagram,
                                String defaultDiagramImageFileName);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process.
     * If there is no interchange information available, an ActivitiInterchangeInfoNotFoundException is thrown.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                List<String> highLightedActivities,
                                List<String> highLightedFlows);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process.
     * If there is no interchange information available, an ActivitiInterchangeInfoNotFoundException is thrown.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                List<String> highLightedActivities);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process.
     * If there is no interchange information available, an ActivitiInterchangeInfoNotFoundException is thrown.
     * @param bpmnModel bpmn model to get diagram for
     */
    InputStream generateDiagram(BpmnModel bpmnModel,
                                String activityFontName,
                                String labelFontName,
                                String annotationFontName);

    String getDefaultActivityFontName();

    String getDefaultLabelFontName();

    String getDefaultAnnotationFontName();

    String getDefaultDiagramImageFileName();
}