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

package org.activiti.kickstart.bpmn20.model.choreography;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.annotations.CallingElement;
import org.activiti.kickstart.bpmn20.annotations.ContainerElement;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.DiagramElement;
import org.activiti.kickstart.bpmn20.model.callable.GlobalChoreographyTask;


/**
 * <p>Java class for tCallChoreography complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tCallChoreography">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tChoreographyActivity">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/MODEL}participantAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="calledChoreographyRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tCallChoreography", propOrder = {
    "participantAssociation"
})
public class CallChoreography
    extends ChoreographyActivity implements ContainerElement, CallingElement
{

    protected List<ParticipantAssociation> participantAssociation;
    @XmlAttribute(name = "calledChoreographyRef")
    @XmlIDREF
    protected Choreography calledChoreographyRef;
    
    @XmlTransient
    public List<DiagramElement> _diagramElements = new ArrayList<DiagramElement>();
    
    /*
     * Constructors
     */
    
    public CallChoreography() {
    	super();
    }
    
    public CallChoreography(ChoreographyActivity choreoAct) {
    	super(choreoAct);
    	
    	this.setStartQuantity(null);
    	this.setCompletionQuantity(null);
    	
    	if(choreoAct instanceof ChoreographyTask) {
    		this.setCalledChoreographyRef(new GlobalChoreographyTask());
    	}
    }

    public List<BaseElement> getCalledElements() {
    	List<BaseElement> calledElements = new ArrayList<BaseElement>();
    	
    	/* Global Task */
    	if(calledChoreographyRef instanceof GlobalChoreographyTask) {
    		calledElements.add(calledChoreographyRef);
    	} 
    	
    	/* Calling a sub choreography */
    	else if(calledChoreographyRef instanceof Choreography) {
    		for(FlowElement flowEl : calledChoreographyRef.getFlowElement()) {
    			if(flowEl instanceof CallingElement) {
    				calledElements.addAll(((CallingElement) flowEl).getCalledElements());
    			}
    		}
    	}
    	
    	return calledElements;
    }
    
    /* Getter & Setter */
    
    /**
     * Gets the value of the participantAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participantAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParticipantAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParticipantAssociation }
     * 
     * 
     */
    public List<ParticipantAssociation> getParticipantAssociation() {
        if (participantAssociation == null) {
            participantAssociation = new ArrayList<ParticipantAssociation>();
        }
        return this.participantAssociation;
    }

    /**
     * Gets the value of the calledChoreographyRef property.
     * 
     * @return
     *     possible object is
     *     {@link Choreography }
     *     
     */
    public Choreography getCalledChoreographyRef() {
        return calledChoreographyRef;
    }

    /**
     * Sets the value of the calledChoreographyRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Choreography }
     *     
     */
    public void setCalledChoreographyRef(Choreography value) {
        this.calledChoreographyRef = value;
    }
    
	public List<DiagramElement> _getDiagramElements() {
		return _diagramElements;
	}

	public List<FlowElement> getFlowElement() {
		if(this.getCalledChoreographyRef() != null) {
			return this.getCalledChoreographyRef().getFlowElement();
		}
		
		return new ArrayList<FlowElement>();
	}


}
