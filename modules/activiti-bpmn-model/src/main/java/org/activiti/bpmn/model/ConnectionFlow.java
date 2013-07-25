package org.activiti.bpmn.model;

public class ConnectionFlow extends FlowElement {

	protected String sourceRef;
	protected String targetRef;

	public ConnectionFlow() {
		super();
	}

	public String getSourceRef() {
	    return sourceRef;
	  }

	public void setSourceRef(String sourceRef) {
	    this.sourceRef = sourceRef;
	  }

	public String getTargetRef() {
	    return targetRef;
	  }

	public void setTargetRef(String targetRef) {
	    this.targetRef = targetRef;
	  }

	public String toString() {
	    return sourceRef + " --> " + targetRef;
	  }

}