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

package org.activiti.kickstart.bpmn20.model.gateway;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;

import org.activiti.kickstart.bpmn20.model.connector.SequenceFlow;

/**
 * This class summarizes {@link Gateway} that associate a default
 * {@link SequenceFlow}.
 * 
 * @author Sven Wagner-Boysen
 * 
 */
public abstract class GatewayWithDefaultFlow extends Gateway {
	
	@XmlAttribute(name = "default")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected SequenceFlow defaultSequenceFlow;
    
    /**
     * Determines the default {@link SequenceFlow}
     * 
     * @return The default {@link SequenceFlow} or null
     */
    public SequenceFlow findDefaultSequenceFlow() {
		for(SequenceFlow seqFlow : this.getOutgoingSequenceFlows()) {
			/* A default sequence flow should not have an condition expression. */
			if(seqFlow.isDefaultSequenceFlow()) {
				this.setDefault(seqFlow);
				return seqFlow;
			}
		}
		
		return null;
	}
    
    /* Getter & Setter */
    
    /**
     * Gets the default {@link SequenceFlow} of the {@link ExclusiveGateway} or
     * null if no default flow is set.
     * 
     * @return
     *     possible object is
     *     {@link SequenceFlow }
     *     
     */
    public SequenceFlow getDefault() {
        return defaultSequenceFlow;
    }

    /**
     * Sets default {@link SequenceFlow}.
     * 
     * @param value
     *     allowed object is
     *     {@link SequenceFlow }
     *     
     */
    public void setDefault(SequenceFlow value) {
        this.defaultSequenceFlow = value;
    }
}
