package org.activiti.editor.language;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Created by alireza on 06/11/2016.
 */
public class ThrowCompensationEventTest extends AbstractConverterTest {

    @Override
    protected String getResource() {
        return "test.throwevent.json";
    }


    @Test
    public void throwCompensateEventInJsonShouldGetConvertedToThrowEventWithProperEventDefinition() throws Exception{
        modelShouldHaveAThrowEventContainingCompensationEventDefinition(readJsonFile());
    }

    @Test
    public void ThrowEventContainingCompensationEventDefinitionShouldGetConvertedToThrowCompensateEventInJson() throws Exception{
        modelShouldHaveAThrowEventContainingCompensationEventDefinition(convertToJsonAndBack(readJsonFile()));
    }

    private void modelShouldHaveAThrowEventContainingCompensationEventDefinition(BpmnModel model){
        FlowElement flowElement = model.getMainProcess().getFlowElement("throwCompensationEvent");
        assertNotNull(flowElement);
        assertThat(flowElement, instanceOf(ThrowEvent.class));
        ThrowEvent throwEvent = (ThrowEvent)flowElement;

        final List<EventDefinition> eventDefinitions = throwEvent.getEventDefinitions();
        assertNotNull(eventDefinitions);
        assertEquals(eventDefinitions.size(), 1);
        assertThat(eventDefinitions.get(0), instanceOf(CompensateEventDefinition.class));
    }
}
