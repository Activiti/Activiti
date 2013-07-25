package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.junit.Test;

public class TwoPoolsConverterTest extends AbstractConverterTest {

	@Test
	public void connvertXMLToModel() throws Exception {
		BpmnModel bpmnModel = readXMLFile();
		validateModel(bpmnModel);
	}

	@Test
	public void convertModelToXML() throws Exception {
		BpmnModel bpmnModel = readXMLFile();
		BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
		validateModel(parsedModel);
		deployProcess(parsedModel);
	}

	protected String getResource() {
		return "twopools.bpmn";
	}

	private void validateModel(BpmnModel model) {
		assertEquals(2, model.getPools().size());
		
		Pool pool1 = model.getPools().get(0);
		assertEquals("Portal", pool1.getName());
		Pool pool2 = model.getPools().get(1);
		assertEquals("ECM", pool2.getName());
		
		Process process1 = model.getProcess(pool1.getId());
		assertNotNull(process1);
		assertEquals(2, process1.getLanes().size());
		
		Process process2 = model.getProcess(pool2.getId());
		assertNotNull(process2);
		assertEquals(1, process2.getLanes().size());

		// test lanes and flow elements in process1
		Lane laneAuthor = process1.getLanes().get(0);
		assertEquals("Author", laneAuthor.getName());
		Lane laneReviewer = process1.getLanes().get(1);
		assertEquals("Reviewer", laneReviewer.getName());
		
		assertEquals(2, laneAuthor.getFlowReferences().size());
		assertEquals(5, laneReviewer.getFlowReferences().size());
		
		int shapeCount = 0;
		int sequenceFlowCount = 0;
		
		for (FlowElement flowElement : process1.getFlowElements()) {
			if (flowElement instanceof SequenceFlow) {
				sequenceFlowCount++;
			} else {
				shapeCount++;
				
				if (flowElement instanceof ExclusiveGateway) {
					// exclusive gateway should have 2 outgoing flows
				
					ExclusiveGateway exclusiveGateway = (ExclusiveGateway)flowElement;
					assertEquals(2, exclusiveGateway.getOutgoingFlows().size());
					FlowElement flow1 = exclusiveGateway.getOutgoingFlows().get(0);
					FlowElement flow2 = exclusiveGateway.getOutgoingFlows().get(1);
					assertTrue(flow1 instanceof SequenceFlow);
					assertTrue(flow2 instanceof SequenceFlow);
				}
			}
		}
		assertEquals(7, shapeCount);
		assertEquals(7, sequenceFlowCount);
		
		// total process flows: 2 in Author lane + 5 in Reviewer lane + 7 sequence flows
		assertEquals(14, process1.getFlowElements().size());
		
		// test lanes and flow elements in process2
		Lane lane = process2.getLanes().get(0);
		assertEquals(4, lane.getFlowReferences().size());
		// 4 shapes + 3 sequence flows
		assertEquals(7, process2.getFlowElements().size());
		
		// now - test message flows
		assertEquals(2, model.getMessageFlows().size());
	}
}
