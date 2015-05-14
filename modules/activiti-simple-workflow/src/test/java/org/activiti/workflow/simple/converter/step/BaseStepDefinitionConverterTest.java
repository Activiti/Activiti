package org.activiti.workflow.simple.converter.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.form.BooleanPropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;
import org.junit.Test;

/**
 * Unit test for {@link BaseStepDefinitionConverter}. Since {@link BaseStepDefinitionConverter} is abstract, this test
 * uses a simple inner class to test basic functionality.
 * 
 */
public class BaseStepDefinitionConverterTest {

    @Test
    public void testCovertFormPropertiesWithListValues() {
        TestStepDefinitionConverter converter = new TestStepDefinitionConverter();

        // Create a form with two properties, one of which is a ListProperty

        FormDefinition formDefinition = new FormDefinition();

        ListPropertyDefinition approveEnum = new ListPropertyDefinition();
        approveEnum.setName("Approval");
        approveEnum.setType("enum");
        approveEnum.addEntry(new ListPropertyEntry("true", "Approve"));
        approveEnum.addEntry(new ListPropertyEntry("false", "Reject"));
        formDefinition.addFormProperty(approveEnum);

        TextPropertyDefinition reason = new TextPropertyDefinition();
        reason.setName("Reason");
        reason.setType("string");
        formDefinition.addFormProperty(reason);
        
        BooleanPropertyDefinition validate = new BooleanPropertyDefinition();
        validate.setName("Validate");
        validate.setType("boolean");
        formDefinition.addFormProperty(validate);

        List<FormProperty> properties = converter.convertProperties(formDefinition);
        assertTrue(properties.size() == 3);

        FormProperty firstProperty = properties.get(0);
        assertNotNull(firstProperty);
        assertEquals("Approval", firstProperty.getName());
        assertEquals("enum", firstProperty.getType());

        // Assert the values are set
        List<FormValue> values = firstProperty.getFormValues();
        assertTrue(values.size() == 2);

        FormValue firstValue = values.get(0);
        assertEquals("Approve", firstValue.getName());
        assertEquals("true", firstValue.getId());

        FormValue secondValue = values.get(1);
        assertEquals("Reject", secondValue.getName());
        assertEquals("false", secondValue.getId());

        // Now confirm the second property, a non list property, is well formed as well.
        FormProperty secondProperty = properties.get(1);
        assertNotNull(secondProperty);
        assertTrue(secondProperty.getFormValues().isEmpty());
        assertEquals("Reason", secondProperty.getName());
        assertEquals("string", secondProperty.getType());
        
        FormProperty thirdProperty = properties.get(2);
        assertNotNull(thirdProperty);
        assertTrue(thirdProperty.getFormValues().isEmpty());
        assertEquals("Validate", thirdProperty.getName());
        assertEquals("boolean", thirdProperty.getType());
    }

    /**
     * Simple inner class to expose abstract functionality to the unit test.
     * 
     */
    private class TestStepDefinitionConverter extends BaseStepDefinitionConverter<HumanStepDefinition, UserTask> {

        @Override
        public Class<HumanStepDefinition> getHandledClass() {
            // Does nothing for this unit test
            return null;
        }

        @Override
        protected UserTask createProcessArtifact(HumanStepDefinition stepDefinition,
                WorkflowDefinitionConversion conversion) {
            // Does nothing for this unit test
            return null;
        }
    }

}
