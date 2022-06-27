/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.exception.ActivitiInterchangeInfoNotFoundException;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ProcessDiagramGeneratorTest extends PluggableActivitiTestCase {

    @Override
    protected void initializeProcessEngine() {
        ProcessEngines.destroy();
        processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                                                  .setDatabaseSchemaUpdate("drop-create")
                .setJdbcDriver("org.h2.Driver")
                .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
                .setJdbcPassword("")
                .setJdbcUsername("sa")
                .buildProcessEngine();

        cachedProcessEngine = processEngine;
        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    }

    @Deployment
    public void testHighLighted() throws Exception {
        ProcessDiagramGenerator imageGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName = imageGenerator.getDefaultActivityFontName();
        String labelFontName = imageGenerator.getDefaultLabelFontName();
        String annotationFontName = imageGenerator.getDefaultAnnotationFontName();

        runtimeService.startProcessInstanceByKey("myProcess");
        List<Task> tasks = taskService.createTaskQuery().list();
        for (Task task : tasks) {
            taskService.complete(task.getId());
        }
        Task task = taskService.createTaskQuery().taskDefinitionKey("usertask4").singleResult();
        taskService.complete(task.getId());

        List<String> activityIds = runtimeService.getActiveActivityIds(task.getProcessInstanceId());
        InputStream diagram = imageGenerator
                .generateDiagram(repositoryService.getBpmnModel(task.getProcessDefinitionId()), activityIds);
        assertThat(diagram).isNotNull();

        List<String> highLightedFlows = asList("flow1", "flow2", "flow3", "flow4", "flow5", "flow6");
        diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(task.getProcessDefinitionId()),
                                                 activityIds, highLightedFlows);
        assertThat(diagram).isNotNull();

        diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(task.getProcessDefinitionId()),
                                                 activityIds, highLightedFlows, activityFontName, labelFontName, annotationFontName);
        assertThat(diagram).isNotNull();
    }

    @Deployment
    public void testSmallBoxLabels() throws Exception {
        ProcessDiagramGenerator imageGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName = imageGenerator.getDefaultActivityFontName();
        String labelFontName = imageGenerator.getDefaultLabelFontName();
        String annotationFontName = imageGenerator.getDefaultAnnotationFontName();

        String id = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myProcess").singleResult()
                .getId();

        List<String> activityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        InputStream diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(id),
                                                 activityIds, highLightedFlows);
        assertThat(diagram).isNotNull();

        diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(id),
                                                 activityIds, highLightedFlows, activityFontName, labelFontName, annotationFontName);
        assertThat(diagram).isNotNull();
    }

    @Deployment
    public void testTransactionElements() throws Exception {
        ProcessDiagramGenerator imageGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName = imageGenerator.getDefaultActivityFontName();
        String labelFontName = imageGenerator.getDefaultLabelFontName();
        String annotationFontName = imageGenerator.getDefaultAnnotationFontName();

        String id = repositoryService.createProcessDefinitionQuery().processDefinitionKey("transactionSubRequest").singleResult()
                .getId();

        List<String> activityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        InputStream diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(id),
                                                 activityIds, highLightedFlows);
        assertThat(diagram).isNotNull();

        diagram = imageGenerator.generateDiagram(repositoryService.getBpmnModel(id),
                                                 activityIds, highLightedFlows, activityFontName, labelFontName, annotationFontName);
        assertThat(diagram).isNotNull();
    }

    @Deployment
    public void testAllElements() throws Exception {
        ProcessDiagramGenerator imageGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName = imageGenerator.getDefaultActivityFontName();
        String labelFontName = imageGenerator.getDefaultLabelFontName();
        String annotationFontName = imageGenerator.getDefaultAnnotationFontName();

        String id = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myProcess").singleResult()
                .getId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
        try (final InputStream resourceStream = imageGenerator.generateDiagram(bpmnModel, activityFontName,
                                                                               labelFontName, annotationFontName)) {
            SVGOMDocument svg = parseXml(resourceStream);
            List<String> startEventIdList = asList("startevent1", "errorstartevent1", "signalstartevent1",
                                                          "messagestartevent1", "timerstartevent1");
            checkDiagramElements(startEventIdList, svg);
            List<String> userTaskIdList = asList("usertask1", "usertask2", "usertask3", "usertask4", "usertask5",
                                                        "usertask6", "usertask7", "usertask8", "usertask9", "usertask10", "usertask11", "usertask12",
                                                        "usertask13", "usertask14", "usertask15", "usertask16", "usertask17", "usertask18", "usertask19");
            checkDiagramElements(userTaskIdList, svg);
            List<String> scriptTaskIdList = asList("scripttask1", "scripttask2", "scripttask3");
            checkDiagramElements(scriptTaskIdList, svg);
            List<String> otherTaskIdList = asList("servicetask1", "mailtask1", "manualtask1", "receivetask1",
                    "callactivity1");
            checkDiagramElements(otherTaskIdList, svg);
            List<String> intermediateEvent = asList("timerintermediatecatchevent1",
                                                           "signalintermediatecatchevent1", "messageintermediatecatchevent1", "signalintermediatethrowevent1",
                                                           "compensationintermediatethrowevent1", "noneintermediatethrowevent1");
            checkDiagramElements(intermediateEvent, svg);
            List<String> gatewayIdList = asList("parallelgateway1", "parallelgateway2", "exclusivegateway1",
                                                       "exclusivegateway3", "inclusivegateway1", "inclusivegateway2", "eventgateway1");
            checkDiagramElements(gatewayIdList, svg);
            List<String> containerIdList = asList("subprocess1", "eventsubprocess1", "pool1", "pool2", "pool3",
                                                         "lane1", "lane2", "lane3", "lane4");
            checkDiagramElements(containerIdList, svg);
            List<String> endEventIdList = asList("errorendevent1", "endevent1", "endevent2", "endevent3",
                                                        "endevent4", "endevent5", "endevent6", "endevent7", "endevent8", "endevent9", "endevent10",
                                                        "endevent11", "endevent12");
            checkDiagramElements(endEventIdList, svg);
        }
    }

    /**
     * Test that when the diagram is generated for a model without graphic info
     * then the default diagram image is returned
     * or the ActivitiInterchangeInfoNotFoundException is thrown
     * depending on the value of the generateDefaultDiagram parameter.
     *
     */
    @Deployment
    public void testGenerateDefaultDiagram() throws Exception {
        //GIVEN
        String id = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("fixSystemFailure")
                .singleResult()
                .getId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);

        ProcessDiagramGenerator imageGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName = imageGenerator.getDefaultActivityFontName();
        String labelFontName = imageGenerator.getDefaultLabelFontName();
        String annotationFontName = imageGenerator.getDefaultAnnotationFontName();

        //WHEN
        try (final InputStream resourceStream = imageGenerator.generateDiagram(bpmnModel,
                                                                               emptyList(),
                                                                               emptyList(),
                                                                               activityFontName,
                                                                               labelFontName,
                                                                               annotationFontName,
                                                                               true)) {
            //THEN
            assertThat(resourceStream).isNotNull();
            byte[] diagram = IOUtils.toByteArray(resourceStream);
            assertThat(diagram).isNotNull();

            try (InputStream imageStream = getClass().getResourceAsStream(imageGenerator.getDefaultDiagramImageFileName())) {
                assertThat(diagram).isEqualTo(IOUtils.toByteArray(imageStream));
            }
        }

        //THEN
        assertThatExceptionOfType(ActivitiInterchangeInfoNotFoundException.class).isThrownBy(
            //WHEN
            () -> imageGenerator.generateDiagram(bpmnModel,
                                                 emptyList(),
                                                 emptyList(),
                                                 activityFontName,
                                                 labelFontName,
                                                 annotationFontName,
                                                 false)
        ).withMessage("No interchange information found.");

        //THEN
        assertThatExceptionOfType(ActivitiImageException.class).isThrownBy(
            //WHEN
            () -> imageGenerator.generateDiagram(bpmnModel,
                                                 emptyList(),
                                                 emptyList(),
                                                 emptyList(),
                                                 emptyList(),
                                                 activityFontName,
                                                 labelFontName,
                                                 annotationFontName,
                                                 true,
                                                 "invalid-file-name")
        ).withMessage("Error occurred while getting default diagram image from file: invalid-file-name");
    }

    private void checkDiagramElements(List<String> elementIdList, SVGOMDocument svg) {
        for (String elementId : elementIdList) {
            assertThat(svg.getElementById(elementId)).isNotNull();
        }
    }

    private SVGOMDocument parseXml(InputStream resourceStream) throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        return (SVGOMDocument) factory.createDocument(null, resourceStream);
    }

}
