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

package org.activiti.kickstart.bpmn20.model.participant;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.activiti.kickstart.bpmn20.diagram.UUIDGenerator;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.FlowNode;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.model.conversation.ConversationElement;


/**
 * <p>Java class for tParticipant complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tParticipant">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *       &lt;sequence>
 *         &lt;element name="interfaceRef" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="endPointRef" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}participantMultiplicity" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="partnerRoleRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="partnerEntityRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="processRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tParticipant", propOrder = {
    "interfaceRef",
    "endPointRef",
    "participantMultiplicity"
})
public class Participant
    extends FlowNode implements ConversationElement
{

    @XmlElement
	protected List<QName> interfaceRef;
    @XmlElement
    protected List<QName> endPointRef;
    @XmlElement(type = ParticipantMultiplicity.class)
    protected ParticipantMultiplicity participantMultiplicity;
    
    @XmlAttribute
    @XmlIDREF
    protected Process processRef;
    
    @XmlAttribute
    protected QName partnerRoleRef;
    @XmlAttribute
    protected QName partnerEntityRef;
    
    @XmlTransient
    protected boolean isInitiating;
    
    @XmlTransient
    private LaneSet laneSet;
    
    @XmlTransient
	public String _processType;
	@XmlTransient
	public String _isClosed;
	@XmlTransient
	public String _isExecutable;
	@XmlTransient
	public boolean _isChoreographyParticipant = false;
    
    /*
     * Constructors
     */
	
	/**
	 * Default constructor
	 */
	public Participant() {
		super();
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param p 
	 * 		template {@link Participant}
	 */
    public Participant(Participant p) {
		super(p);
		
		this.getInterfaceRef().addAll(p.getInterfaceRef());
		this.getEndPointRef().addAll(p.getEndPointRef());
		
		this.setParticipantMultiplicity(p.getParticipantMultiplicity());
		this.setProcessRef(p.getProcessRef());
		
		this.setPartnerRoleRef(p.getPartnerRoleRef());
		this.setPartnerEntityRef(p.getPartnerEntityRef());
		
		this.setInitiating(p.isInitiating());
		this.setLaneSet(p.getLaneSet());
		
		this._processType = p._processType;
		this._isClosed = p._isClosed;
		this._isExecutable = p._isExecutable;
		this._isChoreographyParticipant = p._isChoreographyParticipant;
	}
    
    /* Business logic methods */

	// @Override
    public void addChild(BaseElement child) {
    	if(child instanceof Lane) {
    		if(laneSet == null) {
    			laneSet = new LaneSet();
    			laneSet.setId(UUIDGenerator.generate());
    		}
    		
    		getLaneSet().addChild(child);
    	}
    }
    
    /* Getter & Setter */
    /**
     * Gets the value of the interfaceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interfaceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterfaceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getInterfaceRef() {
        if (interfaceRef == null) {
            interfaceRef = new ArrayList<QName>();
        }
        return this.interfaceRef;
    }

    /**
	 * @return the processRef
	 */
	public Process getProcessRef() {
		return this.processRef;
	}

	/**
	 * @param processRef the processRef to set
	 */
	public void setProcessRef(Process processRef) {
		this.processRef = processRef;
	}

	/**
     * Gets the value of the endPointRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the endPointRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEndPointRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getEndPointRef() {
        if (endPointRef == null) {
            endPointRef = new ArrayList<QName>();
        }
        return this.endPointRef;
    }

    /**
     * Gets the value of the participantMultiplicity property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantMultiplicity }
     *     
     */
    public ParticipantMultiplicity getParticipantMultiplicity() {
        return participantMultiplicity;
    }

    /**
     * Sets the value of the participantMultiplicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantMultiplicity }
     *     
     */
    public void setParticipantMultiplicity(ParticipantMultiplicity value) {
        this.participantMultiplicity = value;
    }

    /**
	 * @return the isInitiating
	 */
	public boolean isInitiating() {
		return isInitiating;
	}

	/**
	 * @param isInitiating the isInitiating to set
	 */
	public void setInitiating(boolean isInitiating) {
		this.isInitiating = isInitiating;
	}

    /**
     * Gets the value of the partnerRoleRef property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getPartnerRoleRef() {
        return partnerRoleRef;
    }

    /**
     * Sets the value of the partnerRoleRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setPartnerRoleRef(QName value) {
        this.partnerRoleRef = value;
    }

    /**
     * Gets the value of the partnerEntityRef property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getPartnerEntityRef() {
        return partnerEntityRef;
    }

    /**
     * Sets the value of the partnerEntityRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setPartnerEntityRef(QName value) {
        this.partnerEntityRef = value;
    }

	public LaneSet getLaneSet() {
		return laneSet;
	}

	public void setLaneSet(LaneSet laneSet) {
		this.laneSet = laneSet;
	}
}
