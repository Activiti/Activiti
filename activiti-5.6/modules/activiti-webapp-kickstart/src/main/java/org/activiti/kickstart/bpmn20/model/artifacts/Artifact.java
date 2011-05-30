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

package org.activiti.kickstart.bpmn20.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.FlowNode;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.model.connector.Edge;
import org.activiti.kickstart.bpmn20.model.conversation.ConversationElement;


/**
 * <p>Java class for tArtifact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tArtifact">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tArtifact")
@XmlSeeAlso({
//    Association.class,
    Group.class,
    TextAnnotation.class
})
public abstract class Artifact
    extends FlowNode
{
	
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
			
			/* Remove from other containment based process reference */
			if(this.getProcess() != null) {
				this.getProcess().removeChild(this);
			}
			
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
	
	/**
	 * Checks whether the Artifact is contained in an conversation.
	 * 
	 * The algorithm checks the source and target neighborhood nodes of the data
	 * object.
	 * 
	 * Navigates into both directions.
	 */
	public boolean isConverstionRelated() {
		this.processedElements = new ArrayList<FlowElement>();
		return this.isConversationConversationRecursivly(this);
	}
	
	/**
	 * Navigates into both directions.
	 * 
	 * @param flowElement
	 *            The {@link FlowElement} to investigate.
	 */
	private boolean isConversationConversationRecursivly(FlowElement flowElement) {
		if (flowElement == null)
			return false;

		/* Check if element is processed already */
		if (this.processedElements.contains(flowElement))
			return false;

		this.processedElements.add(flowElement);

		/*
		 * Check if one of the neighbors is assigned to a Process, otherwise
		 * continue with the after next.
		 */

		for (Edge edge : flowElement.getIncoming()) {
			FlowElement sourceRef = edge.getSourceRef();
			if (sourceRef == null)
				continue;

			if(sourceRef instanceof ConversationElement) {
				return true;	
			}
		}

		for (Edge edge : flowElement.getOutgoing()) {
			FlowElement targetRef = edge.getTargetRef();
			if (targetRef == null)
				continue;
			if(targetRef instanceof ConversationElement) {
				return true;
			}
		}

		/* Continue with the after next nodes */
		
		for (Edge edge : flowElement.getIncoming()) {
			boolean result = this.isConversationConversationRecursivly(edge
					.getSourceRef());
			if (result)
				return result;
		}

		for (Edge edge : flowElement.getOutgoing()) {
			boolean result = this.isConversationConversationRecursivly(edge
					.getTargetRef());
			if (result)
				return result;
		}

		return false;
	}

}
