package org.activiti.dmn.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionRule;
import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.OutputClause;
import org.junit.Test;

public class MultipleDecisionsConverterTest extends AbstractConverterTest {

	@Test
	public void convertXMLToModel() throws Exception {
		DmnDefinition definition = readXMLFile();
		validateModel(definition);
	}

	@Test
	public void convertModelToXML() throws Exception {
		DmnDefinition bpmnModel = readXMLFile();
		DmnDefinition parsedModel = exportAndReadXMLFile(bpmnModel);
		validateModel(parsedModel);
	}

	@Override
	protected String getResource() {
		return "multiple_dmndecisions.dmn";
	}

	private void validateModel(DmnDefinition model) {
		List<Decision> decisions = model.getDecisions();
		assertEquals(3, decisions.size());

		DecisionTable decisionTable1 = (DecisionTable) decisions.get(0).getExpression();
		assertNotNull(decisionTable1);

		List<InputClause> inputClauses1 = decisionTable1.getInputs();
		assertEquals(2, inputClauses1.size());

		List<OutputClause> outputClauses1 = decisionTable1.getOutputs();
		assertEquals(2, outputClauses1.size());

		List<DecisionRule> rules1 = decisionTable1.getRules();
		assertEquals(2, rules1.size());

		DecisionTable decisionTable2 = (DecisionTable) decisions.get(1).getExpression();
		assertNotNull(decisionTable2);

		List<InputClause> inputClauses2 = decisionTable2.getInputs();
		assertEquals(1, inputClauses2.size());

		List<OutputClause> outputClauses2 = decisionTable2.getOutputs();
		assertEquals(2, outputClauses2.size());

		List<DecisionRule> rules2 = decisionTable2.getRules();
		assertEquals(2, rules2.size());
		
		DecisionTable decisionTable3 = (DecisionTable) decisions.get(2).getExpression();
		assertNotNull(decisionTable3);

		List<InputClause> inputClauses3 = decisionTable3.getInputs();
		assertEquals(2, inputClauses3.size());

		List<OutputClause> outputClauses3 = decisionTable3.getOutputs();
		assertEquals(1, outputClauses3.size());

		List<DecisionRule> rules3 = decisionTable3.getRules();
		assertEquals(2, rules3.size());

	}


}
