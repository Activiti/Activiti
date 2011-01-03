/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.activiti.kickstart.bpmn20.model.data_object;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.FlowNode;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.model.connector.Edge;

/**
 * The AbstractDataObject abstracts from data related elements, like
 * {@link DataObject}, {@link DataInput}, {@link DataOutput}, {@link DataStore}.
 * 
 * @author Sven Wagner-Boysen
 */
@XmlSeeAlso( { 
	DataObject.class, 
	DataInput.class, 
	DataOutput.class,
	DataStoreReference.class, 
	DataObjectReference.class
})
public abstract class AbstractDataObject extends FlowNode {

	/* Common attributes of data objects */
	protected DataState dataState;
	@XmlAttribute
	protected Boolean isCollection;
    
	// @XmlTransient
	// private Boolean isRequiredForStart;
	// @XmlTransient
	// private Boolean isRequiredForCompletion;

	/* Getter & Setter */

	/**
	 * Gets the value of the dataState property.
	 * 
	 * @return possible object is {@link TDataState }
	 * 
	 */
	public DataState getDataState() {
		return dataState;
	}

	/**
	 * Sets the value of the dataState property.
	 * 
	 * @param value
	 *            allowed object is {@link DataState }
	 * 
	 */
	public void setDataState(DataState value) {
		this.dataState = value;
	}

	/**
	 * Gets the value of the isCollection property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isIsCollection() {
		if (isCollection == null) {
			return false;
		} else {
			return isCollection;
		}
	}

	/**
	 * Sets the value of the isCollection property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsCollection(Boolean value) {
		this.isCollection = value;
	}

	// /**
	// * @return the isRequiredForStart
	// */
	// public Boolean getIsRequiredForStart() {
	// if(this.isRequiredForStart == null)
	// return false;
	// return isRequiredForStart;
	// }
	//    
	// /**
	// * @param isRequiredForStart the isRequiredForStart to set
	// */
	// public void setIsRequiredForStart(Boolean isRequiredForStart) {
	// this.isRequiredForStart = isRequiredForStart;
	// }
	//    
	// /**
	// * @return the isRequiredForCompletion
	// */
	// public Boolean getIsRequiredForCompletion() {
	// if(this.isRequiredForCompletion == null)
	// return false;
	// return isRequiredForCompletion;
	// }
	//    
	// /**
	// * @param isRequiredForCompletion the isRequiredForCompletion to set
	// */
	// public void setIsRequiredForCompletion(Boolean isRequiredForCompletion) {
	// this.isRequiredForCompletion = isRequiredForCompletion;
	// }

	/* Business logic methodes */

	/**
	 * List of elements already traversed in the graph.
	 */
	@XmlTransient
	private List<FlowElement> processedElements;

	/**
	 * Find an appropriate {@link Process} container for the data object.
	 * 
	 * The algorithm checks the source and target neighborhood nodes of the data
	 * object and the takes the referenced process of one of the neighbors.
	 * 
	 * Navigates into both directions.
	 */
	public void findRelatedProcess() {
		this.processedElements = new ArrayList<FlowElement>();
		Process process = this.findRelatedProcessRecursivly(this);
		if (process != null) {
			this.setProcess(process);
			process.addChild(this);
		}
	}

	/**
	 * Navigates into both directions.
	 * 
	 * @param flowElement
	 *            The {@link FlowElement} to investigate.
	 */
	private Process findRelatedProcessRecursivly(FlowElement flowElement) {
		if (flowElement == null)
			return null;

		/* Check if element is processed already */
		if (this.processedElements.contains(flowElement))
			return null;

		this.processedElements.add(flowElement);

		/*
		 * Check if one of the neighbors is assigned to a Process, otherwise
		 * continue with the after next.
		 */

		for (Edge edge : flowElement.getIncoming()) {
			FlowElement sourceRef = edge.getSourceRef();
			if (sourceRef == null)
				continue;
			Process process = sourceRef.getProcess();
			if (process != null)
				return process;
		}

		for (Edge edge : flowElement.getOutgoing()) {
			FlowElement targetRef = edge.getTargetRef();
			if (targetRef == null)
				continue;
			Process process = targetRef.getProcess();
			if (process != null)
				return process;
		}

		/* Continue with the after next nodes */

		for (Edge edge : flowElement.getIncoming()) {
			Process process = this.findRelatedProcessRecursivly(edge
					.getSourceRef());
			if (process != null)
				return process;
		}

		for (Edge edge : flowElement.getOutgoing()) {
			Process process = this.findRelatedProcessRecursivly(edge
					.getTargetRef());
			if (process != null)
				return process;
		}

		return null;
	}
}
