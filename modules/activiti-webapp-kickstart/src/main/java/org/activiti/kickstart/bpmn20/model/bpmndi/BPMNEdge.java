/**
 * Copyright (c) 2010
 * Signavio, Sven Wagner-Boysen
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

package org.activiti.kickstart.bpmn20.model.bpmndi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.DiagramElement;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.LabeledEdge;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.MessageVisibleKind;

/**
 * <p>
 * Java class for BPMNEdge complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="BPMNEdge">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DD/20100524/DI}LabeledEdge">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/DI}BPMNLabel" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bpmnElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="sourceElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="targetElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="messageVisibleKind" type="{http://www.omg.org/spec/BPMN/20100524/DI}MessageVisibleKind" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "BPMNEdge")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BPMNEdge", propOrder = { "bpmnLabel" })
public class BPMNEdge extends LabeledEdge {

	@XmlElement(name = "BPMNLabel")
	protected BPMNLabel bpmnLabel;

	@XmlIDREF
	@XmlAttribute(name = "bpmnElement")
	protected BaseElement bpmnElement;

	@XmlIDREF
	@XmlAttribute(name = "sourceElement")
	protected DiagramElement sourceElement;

	@XmlIDREF
	@XmlAttribute(name = "targetElement")
	protected DiagramElement targetElement;

	@XmlAttribute(name = "messageVisibleKind")
	protected MessageVisibleKind messageVisibleKind;

	/* Getter & Setter */

	/**
	 * Gets the value of the bpmnLabel property.
	 * 
	 * @return possible object is {@link BPMNLabel }
	 * 
	 */
	public BPMNLabel getBPMNLabel() {
		return bpmnLabel;
	}

	/**
	 * Sets the value of the bpmnLabel property.
	 * 
	 * @param value
	 *            allowed object is {@link BPMNLabel }
	 * 
	 */
	public void setBPMNLabel(BPMNLabel value) {
		this.bpmnLabel = value;
	}

	/**
	 * Gets the value of the bpmnElement property.
	 * 
	 * @return possible object is {@link BaseElement }
	 * 
	 */
	public BaseElement getBpmnElement() {
		return bpmnElement;
	}

	/**
	 * Sets the value of the bpmnElement property.
	 * 
	 * @param value
	 *            allowed object is {@link BaseElement }
	 * 
	 */
	public void setBpmnElement(BaseElement value) {
		this.bpmnElement = value;
	}

	/**
	 * Gets the value of the sourceElement property.
	 * 
	 * @return possible object is {@link DiagramElement }
	 * 
	 */
	public DiagramElement getSourceElement() {
		return sourceElement;
	}

	/**
	 * Sets the value of the sourceElement property.
	 * 
	 * @param value
	 *            allowed object is {@link DiagramElement }
	 * 
	 */
	public void setSourceElement(DiagramElement value) {
		this.sourceElement = value;
	}

	/**
	 * Gets the value of the targetElement property.
	 * 
	 * @return possible object is {@link DiagramElement }
	 * 
	 */
	public DiagramElement getTargetElement() {
		return targetElement;
	}

	/**
	 * Sets the value of the targetElement property.
	 * 
	 * @param value
	 *            allowed object is {@link DiagramElement }
	 * 
	 */
	public void setTargetElement(DiagramElement value) {
		this.targetElement = value;
	}

	/**
	 * Gets the value of the messageVisibleKind property.
	 * 
	 * @return possible object is {@link MessageVisibleKind }
	 * 
	 */
	public MessageVisibleKind getMessageVisibleKind() {
		return messageVisibleKind;
	}

	/**
	 * Sets the value of the messageVisibleKind property.
	 * 
	 * @param value
	 *            allowed object is {@link MessageVisibleKind }
	 * 
	 */
	public void setMessageVisibleKind(MessageVisibleKind value) {
		this.messageVisibleKind = value;
	}
}
