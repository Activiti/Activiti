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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.activiti.kickstart.bpmn20.diagram.UUIDGenerator;
import org.activiti.kickstart.bpmn20.model.activity.resource.ActivityResource;
import org.activiti.kickstart.bpmn20.model.activity.resource.ResourceRole;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.DiagramElement;
import org.activiti.kickstart.bpmn20.model.data_object.DataInput;
import org.activiti.kickstart.bpmn20.model.data_object.DataOutput;
import org.activiti.kickstart.bpmn20.model.extension.ExtensionElements;
import org.activiti.kickstart.bpmn20.model.participant.Lane;
import org.w3c.dom.Element;

/**
 * <p>
 * Java class for tBaseElement complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;tBaseElement&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{http://www.omg.org/bpmn20}documentation&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;any/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;id&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}ID&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tBaseElement", propOrder = {
// "documentation",
// "any"
})
@XmlSeeAlso( {
// TOperation.class,
// TResourceAssignmentExpression.class,
// TMonitoring.class,
// Participant.class,
// ParticipantMultiplicity.class,
// TInputSet.class,
// TOutputSet.class,
// TRelationship.class,
// TAssignment.class,
// MessageFlow.class,
// TInputOutputBinding.class,
// TResourceParameter.class,
// TProperty.class,
 DataInput.class,
 ResourceRole.class,
// TComplexBehaviorDefinition.class,
// MessageFlowAssociation.class,
// DataAssociation.class,
// TParticipantAssociation.class,
// CategoryValue.class,
// TLoopCharacteristics.class,
// TCorrelationPropertyBinding.class,
 ActivityResource.class,
 Expression.class,
//	Lane.class,
// TCorrelationPropertyRetrievalExpression.class,
// TDataState.class,
// LaneSet.class,
// TConversationAssociation.class,
// TInputOutputSpecification.class,
// ConversationNode.class,
// CorrelationKey.class,
// TResourceParameterBinding.class,
// TRendering.class,
// FlowElement.class,
// RootElement.class,
// TAuditing.class,
// Artifact.class,
DataOutput.class
})
public abstract class BaseElement {
	
	@XmlElement
	protected List<Documentation> documentation;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
	@XmlAttribute
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<QName, String>();
	@XmlElement
	private ExtensionElements extensionElements;
	
	@XmlTransient
	private Lane lane;
    
	@XmlTransient
    private Process processRef;
	
	@XmlTransient
	public DiagramElement _diagramElement;
	
	
	/**
	 * Default constructor
	 */
	public BaseElement() {
		super();
		setId(UUIDGenerator.generate());
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param base
	 * 		The {@link BaseElement} to copy.
	 */
	public BaseElement(BaseElement base) {
		if(base.getDocumentation().size() > 0)
			this.getDocumentation().addAll(base.getDocumentation());
		
		if(base.getAny().size() > 0)
			this.getAny().addAll(base.getAny());
		
		if(base.getOtherAttributes().size() > 0)
			this.getOtherAttributes().putAll(base.getOtherAttributes());
		
		this.setId(base.getId());
		this.setLane(base.getLane());
		this.setProcessRef(base.getProcessRef());
		
		if(base.getExtensionElements() != null && !base.getExtensionElements().getAny().isEmpty()) {
			this.setExtensionElements(base.getExtensionElements());
		}
	}
    
	/**
	 * Adds a child element to the current BPMN element if possible. This method
	 * should be implemented by the concrete sub class, if it can contain child
	 * elements.
	 * 
	 * @param child
	 *            The child element to add
	 */
	public void addChild(BaseElement child) {
	}

	/**
	 * Another helper for the import. If the element is of fixed size, then it
	 * may have to be adjusted after import from other tools.
	 */
	public boolean isElementWithFixedSize(){
		return false;
	}

   /**
     * For a fixed-size shape, return the fixed width.
     */
    public double getStandardWidth(){
    	return 0;
    }
    
    /**
     * For a fixed-size shape, return the fixed height.
     */
    public double getStandardHeight(){
    	return 0;
    }
    
    /**
     * Returns a list of all child elements of the current element.
     * 
     * @return
     */
    public List<BaseElement> getChilds() {
		return new ArrayList<BaseElement>();
	}

	/**
	 * @return The pool of the element, if available. Wrapper for
	 *         {@link #getHighestParentLane()}
	 */
	public Lane getPool() {
		return this.getHighestParentLane();
	}

	/**
	 * Retrieves the highest lane in the elements tree
	 * 
	 * @return
	 */
	public Lane getHighestParentLane() {
		if(this.getLane() == null) 
			return null;
		
		Lane lane = (Lane) this.getLane();
		while(lane != null && lane != lane.getLane()) {
			lane = lane.getLane();
		}
		
		return lane;
	}

	public ExtensionElements getOrCreateExtensionElements() {
		if(extensionElements == null) {
			extensionElements = new ExtensionElements();
		}
		
		return extensionElements;
	}
	
	
	/* Getter & Setter */

	/**
	 * Gets the value of the documentation property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the documentation property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDocumentation().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Documentation }
	 * 
	 * 
	 */
	 public List<Documentation> getDocumentation() {
	 if (documentation == null) {
	 documentation = new ArrayList<Documentation>();
	 }
	 return this.documentation;
	 }
	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * {@link Element }
	 * 
	 * 
	 */
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
	}
	
    /**
	 * @return the processRef
	 */
	public Process getProcessRef() {
		return processRef;
	}

	/**
	 * @param processRef the processRef to set
	 */
	public void setProcessRef(Process processRef) {
		this.processRef = processRef;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed
	 * property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string
	 * value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by
	 * updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

	/**
	 * @param lane
	 *            the lane to set
	 */
	public void setLane(Lane lane) {
		this.lane = lane;
	}

	/**
	 * 
	 * Returns the lane that contains this element.
	 * 
	 * @return the lane
	 */
	public Lane getLane() {
		return lane;
	}

	public void setExtensionElements(ExtensionElements extensionElements) {
		this.extensionElements = extensionElements;
	}

	public ExtensionElements getExtensionElements() {
		return extensionElements;
	}

}
