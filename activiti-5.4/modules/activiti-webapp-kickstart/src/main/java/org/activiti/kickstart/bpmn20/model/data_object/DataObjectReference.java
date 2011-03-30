/**
 * Copyright (c) 2010
 * Robert Gurol, Sven Wagner-Boysen
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.Process;

/**
 * A DataObjectReference provides a reference to a globally defined {@link DataObject}.
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tDataObjectReference")
public class DataObjectReference extends AbstractDataObject {
	
	
	@XmlAttribute
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	protected DataObject dataObjectRef;
	
	public void setProcess(Process process) {
		super.setProcess(process);
		if(this.dataObjectRef != null)
			this.dataObjectRef.setProcessRef(process);
		
	}
	
	/* Getter & Setter */
	
	/**
     * Gets the value of the dataObjectRef property.
     * 
     * @return
     *     possible object is
     *     {@link DataObject }
     *     
     */
	public DataObject getDataObjectRef() {
		return dataObjectRef;
	}

	/**
     * Sets the value of the dataObjectRef property.
     * 
     * @return
     *     possible object is
     *     {@link DataObject }
     *     
     */
	public void setDataObjectRef(DataObject dataObjectRef) {
		this.dataObjectRef = dataObjectRef;
	}

	
}
