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

package org.activiti.kickstart.bpmn20.model.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tEscalationEventDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEscalationEventDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tEventDefinition">
 *       &lt;attribute name="escalationCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="escalationRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEscalationEventDefinition")
public class EscalationEventDefinition
    extends EventDefinition
{

	/* Attributes */
    
    @XmlAttribute
    @XmlIDREF
    protected Escalation escalationRef;
    
    /* Constructors */
    
    /**
     * Default constructor
     */
    public EscalationEventDefinition() {}
    
    /**
     * Copy constructor
     * @param escEventDefinition
     */
    public EscalationEventDefinition(EscalationEventDefinition escEventDefinition) {
    	super(escEventDefinition);
    	
    	this.setEscalationRef(escEventDefinition.getEscalationRef());
    }
    
    /* Getter & Setter */

    /**
     * Gets the value of the escalationRef property.
     * 
     * @return
     *     possible object is
     *     {@link Escalation }
     *     
     */
    public Escalation getEscalationRef() {
        return escalationRef;
    }

    /**
     * Sets the value of the escalationRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Escalation }
     *     
     */
    public void setEscalationRef(Escalation value) {
        this.escalationRef = value;
    }

}
