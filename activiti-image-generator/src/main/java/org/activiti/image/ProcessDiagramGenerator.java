/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     * @param activityFontName override the default activity font
     * @param labelFontName override the default label font
     */
    public InputStream generateDiagram(BpmnModel bpmnModel,
                                       List<String> highLightedActivities,
                                       List<String> highLightedFlows,
                                       String activityFontName,
                                       String labelFontName,
                                       String annotationFontName);

    /**
     * Generates a diagram of the given process definition, using the diagram interchange information of the process.
     * @param bpmnModel bpmn model to get diagram for
     * @param highLightedActivities activities to highlight
     * @param highLightedFlows flows to highlight
     */
    public InputStream generateDiagram(BpmnModel bpmnModel,
                                       List<String> highLightedActivities,
                                       List<String> highLightedFlows);

    public InputStream generateDiagram(BpmnModel bpmnModel,
                                       List<String> highLightedActivities);

    public InputStream generateDiagram(BpmnModel bpmnModel,
                                       String activityFontName,
                                       String labelFontName,
                                       String annotationFontName);

    public String getDefaultActivityFontName();

    public String getDefaultLabelFontName();

    public String getDefaultAnnotationFontName();
}