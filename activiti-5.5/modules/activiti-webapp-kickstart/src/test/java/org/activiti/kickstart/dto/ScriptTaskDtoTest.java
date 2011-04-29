package org.activiti.kickstart.dto;

import static org.junit.Assert.*;

import org.activiti.kickstart.bpmn20.model.activity.type.ScriptTask;
import org.junit.Test;


public class ScriptTaskDtoTest {

    
    @Test
    public void testCreateFlowElement() throws Exception {
//    <scriptTask id="theScriptTask" name="Execute script" scriptFormat="juel" activiti:resultVariableName="myVar">
//      <script>#{echo}</script>
//    </scriptTask>
        
        ScriptTaskDto dto = new ScriptTaskDto();
        dto.setId("theScriptTask");
        dto.setName("Execute script");
        
        dto.setScriptFormat("juel");
        dto.setResultVariableName("myVar");
        dto.setScript("#{echo}");
        
        ScriptTask scriptTask = (ScriptTask) dto.createFlowElement();
        assertEquals("juel", scriptTask.getScriptFormat());
        assertEquals("myVar", scriptTask.getResultVariableName());
        assertEquals("#{echo}", scriptTask.getScript());
    }
}
