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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.annotations.CallingElement;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.Collaboration;
import org.activiti.kickstart.bpmn20.model.bpmndi.di.DiagramElement;
import org.activiti.kickstart.bpmn20.model.callable.GlobalConversation;


/**
 * <p>Java class for tCallConversation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tCallConversation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tConversationNode">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}participantAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="calledElementRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tCallConversation", propOrder = {
//    "participantAssociation"
})
public class CallConversation
    extends ConversationNode implements CallingElement
{

//    protected List<TParticipantAssociation> participantAssociation;
    @XmlIDREF
	@XmlAttribute
    protected Collaboration calledElementRef;
    
    @XmlTransient
    public List<DiagramElement> _diagramElements = new ArrayList<DiagramElement>();

    /*
     * Constructors
     */
    
    public CallConversation() {
    	super();
    }
    
	public CallConversation(ConversationNode node) {
		super(node);
		
		if(node instanceof Conversation) {
			setCalledElementRef(new GlobalConversation());
		}
	}

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
     * {@link TParticipantAssociation }
     * 
     * 
     */
//    public List<TParticipantAssociation> getParticipantAssociation() {
//        if (participantAssociation == null) {
//            participantAssociation = new ArrayList<TParticipantAssociation>();
//        }
//        return this.participantAssociation;
//    }

    /**
     * Gets the value of the calledElementRef property.
     * 
     * @return
     *     possible object is
     *     {@link Collaboration }
     *     
     */
    public Collaboration getCalledElementRef() {
        return calledElementRef;
    }

    /**
     * Sets the value of the calledElementRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Collaboration }
     *     
     */
    public void setCalledElementRef(Collaboration value) {
        this.calledElementRef = value;
    }


	public List<BaseElement> getCalledElements() {
		List<BaseElement> calledElements = new ArrayList<BaseElement>();
		
		if(calledElementRef != null) {
			calledElements.add(getCalledElementRef());
		}
		
		return calledElements;
	}

}
