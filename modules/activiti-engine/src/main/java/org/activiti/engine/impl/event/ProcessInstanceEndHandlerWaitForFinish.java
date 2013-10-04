package org.activiti.engine.impl.event;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

/**
 * @author Saeid Mirzaei
 */

public class ProcessInstanceEndHandlerWaitForFinish extends AbstractBpmnParseHandler<Process> {

  protected Class< ? extends BaseElement> getHandledType() {
    return Process.class;
  }
  protected void executeParse(BpmnParse bpmnParse, Process element) {
    bpmnParse.getCurrentProcessDefinition().addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, new ProcessInstanceEndHandlerWaitForFinishHelper());
    
  }

}
