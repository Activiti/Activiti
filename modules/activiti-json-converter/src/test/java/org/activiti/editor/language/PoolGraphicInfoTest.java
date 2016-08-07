package org.activiti.editor.language;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by David Pardo on 8/07/2016.
 */
public class PoolGraphicInfoTest extends AbstractConverterTest {
	Logger logger = LoggerFactory.getLogger(PoolGraphicInfoTest.class);

	private final String POOL_ID = "sid-5EDCE3C2-3422-48B2-89CB-7F2ABAF06883";
	private final String LANE_CUSTOMER_ID = "sid-F2C34B34-2217-49D4-AC9A-5AD3933A5F57";
	private final String LANE_CUSTOMER_SERVICE_ID = "sid-97F370BE-F9F1-45B1-A5A8-0FE4344EB256";
	private final String QUESTION_TASK_ID = "sid-304094DF-220E-435D-825A-33BCB3A60A4E";
	private final String SEQUENCEFLOW_QUESTION_TO_ANWSER_ID= "sid-C30FD08E-219F-4C3E-B777-CB0F2083C5A4";

	@Override
	protected String getResource() {
		return "test.poolflowlocationslist.json";
	}

	@Test
	public void graphicInfoShouldNotBeLost() throws Exception {
		BpmnModel bpmnModel = readJsonFile();
		validate(bpmnModel);
		logger.info("Initial validation completed now reversing revalidating!");
		bpmnModel = convertToJsonAndBack(bpmnModel);
		validate(bpmnModel);
	}

	private void validate(BpmnModel bpmnModel){
		GraphicInfo giPool = bpmnModel.getGraphicInfo(POOL_ID);
		assertThat(giPool.getX(),is(180.0));
		assertThat(giPool.getY(),is(45.0));
		assertThat(giPool.getWidth(),is(600.0));
		assertThat(giPool.getHeight(),is(500.0));

		GraphicInfo giCustomerLane = bpmnModel.getGraphicInfo(LANE_CUSTOMER_ID);
		assertThat(giCustomerLane.getX(),is(210.0));
		assertThat(giCustomerLane.getY(),is(45.0));
		assertThat(giCustomerLane.getWidth(),is(570.0));
		assertThat(giCustomerLane.getHeight(),is(250.0));

		GraphicInfo giCustomerServiceLane = bpmnModel.getGraphicInfo(LANE_CUSTOMER_SERVICE_ID);
		assertThat(giCustomerServiceLane.getX(),is(210.0));
		assertThat(giCustomerServiceLane.getY(),is(295.0));
		assertThat(giCustomerServiceLane.getWidth(),is(570.0));
		assertThat(giCustomerServiceLane.getHeight(),is(250.0));

		GraphicInfo giQuestionUserTask = bpmnModel.getGraphicInfo(QUESTION_TASK_ID);
		assertThat(giQuestionUserTask.getX(),is(352.0));
		assertThat(giQuestionUserTask.getY(),is(130.0));
		assertThat(giQuestionUserTask.getWidth(),is(100.0));
		assertThat(giQuestionUserTask.getHeight(),is(80.0));

		List<GraphicInfo> sequenceFlow = bpmnModel.getFlowLocationGraphicInfo(SEQUENCEFLOW_QUESTION_TO_ANWSER_ID);
		GraphicInfo giSequenceFlowStart = sequenceFlow.get(0);
		assertThat(giSequenceFlowStart.getX(),is(402.0));
		assertThat(giSequenceFlowStart.getY(),is(210.0));

		GraphicInfo giSequenceFlowEnd = sequenceFlow.get(1);
		assertThat(giSequenceFlowEnd.getX(),is(402.0));
		assertThat(giSequenceFlowEnd.getY(),is(380.0));
	}
}
