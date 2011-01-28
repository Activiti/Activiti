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

package org.activiti.kickstart.bpmn20.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.activiti.kickstart.bpmn20.model.activity.SubProcess;
import org.activiti.kickstart.bpmn20.model.choreography.SubChoreography;
import org.activiti.kickstart.bpmn20.model.connector.Edge;
import org.activiti.kickstart.bpmn20.model.data_object.DataStoreReference;
import org.activiti.kickstart.bpmn20.model.misc.Auditing;
import org.activiti.kickstart.bpmn20.model.misc.Monitoring;
import org.activiti.kickstart.bpmn20.model.participant.Lane;
import org.activiti.kickstart.bpmn20.util.EscapingStringAdapter;

/**
 * <p>
 * Java class for tFlowElement complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="tFlowElement">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}auditing" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}monitoring" minOccurs="0"/>
 *         &lt;element name="categoryValue" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tFlowElement", propOrder = { "auditing", "monitoring",
		"categoryValue"
// "incoming",
// "outgoing"
// "process"
})
@XmlSeeAlso({
// SequenceFlow.class,
// DataObject.class,
// DataStore.class,
		Lane.class, FlowNode.class, DataStoreReference.class })
public abstract class FlowElement extends BaseElement {

	protected Auditing auditing;
	protected Monitoring monitoring;
	protected List<QName> categoryValue;

	@XmlAttribute
	@XmlJavaTypeAdapter(EscapingStringAdapter.class)
	protected String name;

	// @XmlIDREF
	// @XmlSchemaType(name = "IDREF")
	// @XmlElement(name = "incoming", type = Edge.class)
	@XmlTransient
	protected List<Edge> incoming;

	// @XmlIDREF
	// @XmlSchemaType(name = "IDREF")
	// @XmlElement(name = "outgoing", type = Edge.class)
	@XmlTransient
	protected List<Edge> outgoing;

	/* The process the element belongs to */
	// @XmlIDREF
	// @XmlAttribute
	// @XmlSchemaType(name = "IDREF")
	@XmlTransient
	protected Process process;

	@XmlTransient
	protected SubProcess subProcess;
	@XmlTransient
	protected SubChoreography subChoreography;

	/**
	 * Default constructor
	 */
	public FlowElement() {

	}

	/**
	 * Copy constructor
	 */
	public FlowElement(FlowElement flowEl) {
		super(flowEl);

		if (flowEl.getCategoryValue().size() > 0)
			this.getCategoryValue().addAll(flowEl.getCategoryValue());

		if (flowEl.getIncoming().size() > 0)
			this.getIncoming().addAll(flowEl.getIncoming());

		if (flowEl.getOutgoing().size() > 0)
			this.getOutgoing().addAll(flowEl.getOutgoing());

		this.setAuditing(flowEl.getAuditing());
		this.setMonitoring(flowEl.getMonitoring());

		this.setProcess(flowEl.getProcess());
		this.setName(flowEl.getName());
	}

	//TODO move to visitor?
	/**
	 * Another helper for the import. If the element is of fixed size, then it
	 * may have to be adjusted after import from other tools.
	 */
	public boolean isElementWithFixedSize() {
		return false;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parent != null && parent instanceof SubProcess) {
			this.subProcess = (SubProcess) parent;
		}

		if (parent != null && parent instanceof SubChoreography) {
			this.subChoreography = (SubChoreography) parent;
		}
	}

	/* Getter & Setter */

	/**
	 * Gets the value of the incoming property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the incoming property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getIncoming().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QName }
	 * 
	 * 
	 */
	public List<Edge> getIncoming() {
		if (incoming == null) {
			incoming = new ArrayList<Edge>();
		}
		return this.incoming;
	}

	/**
	 * Gets the value of the outgoing property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the outgoing property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getOutgoing().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QName }
	 * 
	 * 
	 */
	public List<Edge> getOutgoing() {
		if (outgoing == null) {
			outgoing = new ArrayList<Edge>();
		}
		return this.outgoing;
	}

	/**
	 * Gets the value of the auditing property.
	 * 
	 * @return possible object is {@link Auditing }
	 * 
	 */
	public Auditing getAuditing() {
		return auditing;
	}

	/**
	 * Sets the value of the auditing property.
	 * 
	 * @param value
	 *            allowed object is {@link Auditing }
	 * 
	 */
	public void setAuditing(Auditing value) {
		this.auditing = value;
	}

	/**
	 * Gets the value of the monitoring property.
	 * 
	 * @return possible object is {@link Monitoring }
	 * 
	 */
	public Monitoring getMonitoring() {
		return monitoring;
	}

	/**
	 * Sets the value of the monitoring property.
	 * 
	 * @param value
	 *            allowed object is {@link Monitoring }
	 * 
	 */
	public void setMonitoring(Monitoring value) {
		this.monitoring = value;
	}

	/**
	 * Gets the value of the categoryValue property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the categoryValue property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCategoryValue().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QName }
	 * 
	 * 
	 */
	public List<QName> getCategoryValue() {
		if (categoryValue == null) {
			categoryValue = new ArrayList<QName>();
		}
		return this.categoryValue;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @param process
	 *            the process to set
	 */
	public void setProcess(Process process) {
		this.process = process;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	public SubProcess getSubProcess() {
		return subProcess;
	}

	public void setSubProcess(SubProcess subProcess) {
		this.subProcess = subProcess;
	}

	public SubChoreography getSubChoreography() {
		return subChoreography;
	}

	public void setSubChoreography(SubChoreography subChoreography) {
		this.subChoreography = subChoreography;
	}
}
