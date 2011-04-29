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

package org.activiti.kickstart.bpmn20.model.conversation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.annotations.CallingElement;
import org.activiti.kickstart.bpmn20.annotations.ContainerElement;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.artifacts.Artifact;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.DiagramElement;
import org.activiti.kickstart.bpmn20.model.connector.Association;
import org.activiti.kickstart.bpmn20.model.connector.MessageFlow;

/**
 * <p>
 * Java class for tSubConversation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="tSubConversation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tConversationNode">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}conversationNode" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}artifact" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="correlationKeyRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tSubConversation", propOrder = { 
		"conversationNode",
		"artifact", 
		"correlationKeyRef",
		"messageFlow",
		"association",
		"conversationLink"})
public class SubConversation extends ConversationNode implements ContainerElement, CallingElement{

	@XmlElementRefs( { @XmlElementRef(type = CallConversation.class),
			@XmlElementRef(type = Conversation.class),
			@XmlElementRef(type = SubConversation.class) })
	protected List<ConversationNode> conversationNode;

	@XmlElementRef()
	protected List<Artifact> artifact;

	protected List<MessageFlow> messageFlow;
	@XmlElementRef
	protected List<Association> association;

	protected List<ConversationLink> conversationLink;

	@XmlTransient
	public List<DiagramElement> _diagramElements = new ArrayList<DiagramElement>();

	// @XmlAttribute
	protected CorrelationKey correlationKeyRef;

	public List<BaseElement> getChilds() {
		List<BaseElement> childs = super.getChilds();

		childs.addAll(this.getParticipantRef());
		childs.addAll(this.getMessageFlow());
		childs.addAll(this.getArtifact());
		childs.addAll(this.getConversationNode());
		childs.addAll(this.getConversationLink());
		childs.addAll(this.getAssociation());

		return childs;
	}

	/* Getter & Setter */

	/**
	 * Gets the value of the conversationNode property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the conversationNode property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getConversationNode().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link SubConversation }{@code >}
	 * {@link JAXBElement }{@code <}{@link CallConversation }{@code >}
	 * {@link JAXBElement }{@code <}{@link Communication }{@code >}
	 * {@link JAXBElement }{@code <}{@link ConversationNode }{@code >}
	 * 
	 * 
	 */
	public List<ConversationNode> getConversationNode() {
		if (conversationNode == null) {
			conversationNode = new ArrayList<ConversationNode>();
		}
		return this.conversationNode;
	}

	/**
	 * Gets the value of the artifact property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the artifact property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getArtifact().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link TArtifact }{@code >}
	 * {@link JAXBElement }{@code <}{@link TAssociation }{@code >}
	 * {@link JAXBElement }{@code <}{@link TGroup }{@code >} {@link JAXBElement }
	 * {@code <}{@link TTextAnnotation }{@code >}
	 * 
	 * 
	 */
	public List<Artifact> getArtifact() {
		if (artifact == null) {
			artifact = new ArrayList<Artifact>();
		}
		return this.artifact;
	}

	public List<MessageFlow> getMessageFlow() {
		if (messageFlow == null) {
			messageFlow = new ArrayList<MessageFlow>();
		}
		return messageFlow;
	}

	public List<Association> getAssociation() {
		if (association == null) {
			association = new ArrayList<Association>();
		}

		return association;
	}

	public List<ConversationLink> getConversationLink() {
		if (conversationLink == null) {
			conversationLink = new ArrayList<ConversationLink>();
		}
		return conversationLink;
	}

	/**
	 * Gets the value of the correlationKeyRef property.
	 * 
	 * @return possible object is {@link CorrelationKey }
	 * 
	 */
	public CorrelationKey getCorrelationKeyRef() {
		return correlationKeyRef;
	}

	/**
	 * Sets the value of the correlationKeyRef property.
	 * 
	 * @param value
	 *            allowed object is {@link CorrelationKey }
	 * 
	 */
	public void setCorrelationKeyRef(CorrelationKey value) {
		this.correlationKeyRef = value;
	}

	public List<DiagramElement> _getDiagramElements() {
		return _diagramElements;
	}

	public List<FlowElement> getFlowElement() {
		List<FlowElement> flowElements = new ArrayList<FlowElement>();
		
		flowElements.addAll(getConversationNode());
		flowElements.addAll(getParticipantRef());
		flowElements.addAll(getArtifact());
		flowElements.addAll(getConversationLink());
		flowElements.addAll(getMessageFlow());
		flowElements.addAll(getAssociation());
		
		return flowElements;
	}

	public List<BaseElement> getCalledElements() {
		List<BaseElement> calledElements = new ArrayList<BaseElement>();
		
		for(ConversationNode node : getConversationNode()) {
			if(node instanceof CallingElement) {
				calledElements.addAll(((CallingElement) node).getCalledElements());
			}
		}
		
		return calledElements;
	}

}
