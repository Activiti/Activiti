package org.activiti.layout.flow;


public class HandleActivity implements FlowCommand {
	
	private FlowControl flowControl;
	
	public HandleActivity(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public void execute() {
		flowControl.createGatewayVertex();
	}	
}
