package org.activiti.layout.flow;


public class HandleEventFlow implements FlowCommand {
	
	private FlowControl flowControl;
	
	public HandleEventFlow(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public void execute() {
		flowControl.handleEvent();
	}	
}
