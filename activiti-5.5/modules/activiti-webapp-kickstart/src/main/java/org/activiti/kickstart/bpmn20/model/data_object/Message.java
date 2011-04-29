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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.FlowNode;
import org.activiti.kickstart.bpmn20.model.choreography.ChoreographyActivity;
import org.activiti.kickstart.bpmn20.model.connector.Association;
import org.activiti.kickstart.bpmn20.model.misc.ItemDefinition;
import org.activiti.kickstart.bpmn20.model.participant.Participant;


/**
 * <p>Java class for tMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMessage">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tRootElement">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="structureRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMessage")
public class Message
    extends FlowNode
{

    @XmlAttribute
    @XmlIDREF
    protected ItemDefinition structureRef;
    
    @XmlTransient
    private boolean isInitiating;
    
    /**
     * Retrieves the association edge connecting the message object with an 
     * choreography activity or participant.
     * 
     * @return
     */
    public Association getDataConnectingAssociation() {
    	List<Association> associationList = new ArrayList<Association>();
    	
    	for(FlowElement element : this.getIncoming()) {
    		if(element instanceof Association)
    			associationList.add((Association) element);
    	}
    	
    	for(FlowElement element : this.getOutgoing()) {
    		if(element instanceof Association)
    			associationList.add((Association) element);
    	}
    	
    	for(Association msgAssociation : associationList) {
    		if(msgAssociation.getSourceRef() instanceof ChoreographyActivity 
    			|| msgAssociation.getSourceRef() instanceof Participant 
    			|| msgAssociation.getTargetRef() instanceof ChoreographyActivity 
    			|| msgAssociation.getTargetRef() instanceof Participant) {
    			
    			return msgAssociation;
    		}
    	}
    	
    	return null;
    }
    
    /* Getter & Setter */

    /**
     * Gets the value of the structureRef property.
     * 
     * @return
     *     possible object is
     *     {@link ItemDefinition }
     *     
     */
    public ItemDefinition getStructureRef() {
        return structureRef;
    }

    /**
     * Sets the value of the structureRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemDefinition }
     *     
     */
    public void setStructureRef(ItemDefinition value) {
        this.structureRef = value;
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

}
