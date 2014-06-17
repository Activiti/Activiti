package org.activiti.layout.flow;


public class HandleSubProcess implements FlowCommand {
	
	FlowControl flowControl;
	
	public HandleSubProcess(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public void execute() {
		flowControl.handleSubProcess();
	}
	
}
