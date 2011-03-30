/**
 * Copyright (c) 2010
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

package org.activiti.kickstart.bpmn20.model.misc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.activiti.kickstart.bpmn20.diagram.UUIDGenerator;
import org.activiti.kickstart.bpmn20.model.RootElement;
import org.activiti.kickstart.bpmn20.util.EscapingStringAdapter;


/**
 * <p>Java class for tError complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tError">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tRootElement">
 *       &lt;attribute name="structureRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tError")
public class Error
    extends RootElement
{

    @XmlElement
    protected ItemDefinition structureRef;
    @XmlAttribute
    @XmlJavaTypeAdapter(EscapingStringAdapter.class)
    protected String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(EscapingStringAdapter.class)
    protected String errorCode;
    
    /* Constructor */
    public Error() {
    	super();
    	setId(UUIDGenerator.generate());
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

	public String getName() {
		return name;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
