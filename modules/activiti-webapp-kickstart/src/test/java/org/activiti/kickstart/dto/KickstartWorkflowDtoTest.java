package org.activiti.kickstart.dto;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.kickstart.bpmn20.model.Definitions;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.model.activity.type.ServiceTask;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;
import org.activiti.kickstart.bpmn20.model.bpmndi.BPMNPlane;
import org.activiti.kickstart.bpmn20.model.extension.activiti.ActivitFieldExtensionElement;
import org.junit.Test;


public class KickstartWorkflowDtoTest {

    @Test
    public void testSimpleUserTask() throws Exception {
        KickstartWorkflowDto dto = new KickstartWorkflowDto();
        dto.setName("One User Task Workflow");
        dto.setDescription("Simple workflow definition containing one user task");

        BaseTaskDto task = new UserTaskDto();
        task.setId("myId");
        task.setName("My First User task");
        task.setDescription("Desc first User task");
        dto.addTask(task);
        
        Definitions def = dto.toBpmn20Xml();
        BPMNPlane bpmnPlane = def.getDiagram().get(0).getBPMNPlane();
        List<FlowElement> flowElements = ((Process) bpmnPlane.getBpmnElement()).getFlowElement();
        
        assertEquals(5, flowElements.size());
        
        int numberOfUserTasks = 0;
        
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                numberOfUserTasks++;
            }
        }
        assertEquals("Should contain exactly one user task", 1, numberOfUserTasks);
    }
    
    @Test
    public void testSimpleServiceTask() throws Exception {
        KickstartWorkflowDto dto = new KickstartWorkflowDto();
        dto.setName("One Service Task Workflow");
        dto.setDescription("Simple workflow definition containing one service task");

        ServiceTaskDto task = new ServiceTaskDto();
        task.setId("myId");
        task.setName("My First Service task");
        task.setDescription("Desc first Service task");
        task.setDelegateExpression("#{myDelegateExpression}");
        task.setClassName("de.test.MyClass");
        task.setExpression("#{my.favorite.expression}");
        dto.addTask(task);
        
        Definitions def = dto.toBpmn20Xml();
        BPMNPlane bpmnPlane = def.getDiagram().get(0).getBPMNPlane();
        List<FlowElement> flowElements = ((Process) bpmnPlane.getBpmnElement()).getFlowElement();
        
        assertEquals(5, flowElements.size());
        
        int numberOfServiceTasks = 0;
        ServiceTask serviceTask = null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof ServiceTask) {
                numberOfServiceTasks++;
                serviceTask = (ServiceTask) flowElement;
            }
        }
        assertEquals("Should contain exactly one service task", 1, numberOfServiceTasks);
        assertEquals("#{myDelegateExpression}", serviceTask.getDelegateExpression());
        assertEquals("de.test.MyClass", serviceTask.getClassName());
        assertEquals("#{my.favorite.expression}", serviceTask.getExpression());
    }
    
    
    @Test
    public void testSimpleEmailTask() throws Exception {
        KickstartWorkflowDto dto = new KickstartWorkflowDto();
        dto.setName("One Service Task Workflow");
        dto.setDescription("Simple workflow definition containing one service task");

        MailTaskDto task = new MailTaskDto();
        task.setId("myId");
        task.setName("My First Mail task");
        task.setDescription("Desc first Mail task");
        task.getTo().setStringValue("my favorite recipient");
        dto.addTask(task);
        
        Definitions def = dto.toBpmn20Xml();
        BPMNPlane bpmnPlane = def.getDiagram().get(0).getBPMNPlane();
        List<FlowElement> flowElements = ((Process) bpmnPlane.getBpmnElement()).getFlowElement();
        
        assertEquals(5, flowElements.size());
        
        int numberOfServiceTasks = 0;
        ServiceTask serviceTask = null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof ServiceTask) {
                numberOfServiceTasks++;
                serviceTask = (ServiceTask) flowElement;
            }
        }
        assertEquals("Should contain exactly one service task with type mail", 1, numberOfServiceTasks);
        assertEquals("mail", serviceTask.getType());
        assertEquals("Should have extenstion elements", 
                1, serviceTask.getExtensionElements().getAny().size());
        assertEquals("Should have extenstion element 'to'", "to", 
                ((ActivitFieldExtensionElement)serviceTask.getExtensionElements().getAny().get(0)).getName());
        assertEquals("Extenstion element 'to' should contain string value", 
                "my favorite recipient", 
                ((ActivitFieldExtensionElement)serviceTask.getExtensionElements().getAny().get(0)).getStringValue());
    }
    
    @Test
    public void testTwoParallelServiceTasks() throws Exception {
        KickstartWorkflowDto dto = new KickstartWorkflowDto();
        dto.setName("One Service Task Workflow");
        dto.setDescription("Simple workflow definition containing one service task");

        ServiceTaskDto task1 = new ServiceTaskDto();
        task1.setId("myFirstId");
        task1.setName("My First Service task");
        task1.setDescription("Desc first Service task");
        task1.setDelegateExpression("#{myFirstDelegateExpression}");
        task1.setClassName("de.test.MyFirstClass");
        task1.setExpression("#{my.favorite.first.expression}");
        dto.addTask(task1);
        
        ServiceTaskDto task2 = new ServiceTaskDto();
        task2.setId("mySecondId");
        task2.setName("My Second Service task");
        task2.setDescription("Desc Second Service task");
        task2.setDelegateExpression("#{mySecondDelegateExpression}");
        task2.setClassName("de.test.MySecondClass");
        task2.setExpression("#{my.favorite.second.expression}");
        task2.setStartWithPrevious(true);
        dto.addTask(task2);
        
        Definitions def = dto.toBpmn20Xml();
        BPMNPlane bpmnPlane = def.getDiagram().get(0).getBPMNPlane();
        List<FlowElement> flowElements = ((Process) bpmnPlane.getBpmnElement()).getFlowElement();
        
        assertEquals(12, flowElements.size());
        
        int numberOfServiceTasks = 0;
        ServiceTask serviceTask1 = null;
        ServiceTask serviceTask2 = null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof ServiceTask) {
                numberOfServiceTasks++;
                if (serviceTask1 == null)  {
                    serviceTask1 = (ServiceTask) flowElement;
                } else if (serviceTask2 == null) {
                    serviceTask2 = (ServiceTask) flowElement;
                }
            }
        }
        assertEquals("Should contain exactly one service task", 2, numberOfServiceTasks);
        assertEquals("#{myFirstDelegateExpression}", serviceTask1.getDelegateExpression());
        assertEquals("de.test.MyFirstClass", serviceTask1.getClassName());
        assertEquals("#{my.favorite.first.expression}", serviceTask1.getExpression());
        
        assertEquals("#{mySecondDelegateExpression}", serviceTask2.getDelegateExpression());
        assertEquals("de.test.MySecondClass", serviceTask2.getClassName());
        assertEquals("#{my.favorite.second.expression}", serviceTask2.getExpression());
    }
    
    @Test
    public void testParallelServiceAndUserTasks() throws Exception {
        KickstartWorkflowDto dto = new KickstartWorkflowDto();
        dto.setName("One Service Task Workflow");
        dto.setDescription("Simple workflow definition containing one service task");

        ServiceTaskDto task1 = new ServiceTaskDto();
        task1.setId("myFirstId");
        task1.setName("My First Service task");
        task1.setDescription("Desc first Service task");
        task1.setDelegateExpression("#{myFirstDelegateExpression}");
        task1.setClassName("de.test.MyFirstClass");
        task1.setExpression("#{my.favorite.first.expression}");
        dto.addTask(task1);
        
        UserTaskDto task2 = new UserTaskDto();
        task2.setId("mySecondId");
        task2.setName("My Second Service task");
        task2.setDescription("Desc Second Service task");
        task2.setAssignee("myAssignee");
        task2.setStartWithPrevious(true);
        dto.addTask(task2);
        
        Definitions def = dto.toBpmn20Xml();
        BPMNPlane bpmnPlane = def.getDiagram().get(0).getBPMNPlane();
        List<FlowElement> flowElements = ((Process) bpmnPlane.getBpmnElement()).getFlowElement();
        
        assertEquals(12, flowElements.size());
        
        ServiceTask serviceTask = null;
        UserTask userTask = null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof ServiceTask) {
                    serviceTask = (ServiceTask) flowElement;
            } else if (flowElement instanceof UserTask) {
                userTask = (UserTask) flowElement;
            }
        }
        assertEquals("#{myFirstDelegateExpression}", serviceTask.getDelegateExpression());
        assertEquals("de.test.MyFirstClass", serviceTask.getClassName());
        assertEquals("#{my.favorite.first.expression}", serviceTask.getExpression());
        
        assertEquals("myAssignee", userTask.getActivityResource().get(0).getResourceAssignmentExpression().getExpression().getContent().get(0));
    }
}
