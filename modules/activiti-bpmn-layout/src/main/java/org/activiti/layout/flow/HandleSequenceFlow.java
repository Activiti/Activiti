package org.activiti.layout.flow;


public class HandleSequenceFlow implements FlowCommand {
	
	FlowControl flowControl;
	
	public HandleSequenceFlow(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public void execute() {
		flowControl.handleSequenceFlow();
	}
	
}
