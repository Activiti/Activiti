package org.activiti.layout.flow;


public class HandleGatewayVertex implements FlowCommand {
	
	private FlowControl flowControl;
	
	public HandleGatewayVertex(FlowControl flowControl) {
		this.flowControl = flowControl;
	}

	public void execute() {
		flowControl.createGatewayVertex();
	}	
}
