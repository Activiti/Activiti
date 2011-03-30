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

package org.activiti.kickstart.bpmn20.model.activity.misc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.data_object.Message;
import org.activiti.kickstart.bpmn20.model.misc.Error;
import org.activiti.kickstart.bpmn20.util.EscapingStringAdapter;


/**
 * <p>Java class for tOperation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tOperation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *       &lt;sequence>
 *         &lt;element name="inMessageRef" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="outMessageRef" type="{http://www.w3.org/2001/XMLSchema}QName" minOccurs="0"/>
 *         &lt;element name="errorRef" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tOperation", propOrder = {
    "inMessageRef",
    "outMessageRef",
    "errorRef"
})
public class Operation
    extends BaseElement
{

    @XmlElement(required = true)
    protected Message inMessageRef;
    @XmlElement
    protected Message outMessageRef;
    @XmlElement
    protected List<Error> errorRef;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(EscapingStringAdapter.class)
    protected String name;

    /**
     * Gets the value of the inMessageRef property.
     * 
     * @return
     *     possible object is
     *     {@link Message }
     *     
     */
    public Message getInMessageRef() {
        return inMessageRef;
    }

    /**
     * Sets the value of the inMessageRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message }
     *     
     */
    public void setInMessageRef(Message value) {
        this.inMessageRef = value;
    }

    /**
     * Gets the value of the outMessageRef property.
     * 
     * @return
     *     possible object is
     *     {@link Message }
     *     
     */
    public Message getOutMessageRef() {
        return outMessageRef;
    }

    /**
     * Sets the value of the outMessageRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message }
     *     
     */
    public void setOutMessageRef(Message value) {
        this.outMessageRef = value;
    }

    /**
     * Gets the value of the errorRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Error }
     * 
     * 
     */
    public List<Error> getErrorRef() {
        if (errorRef == null) {
            errorRef = new ArrayList<Error>();
        }
        return this.errorRef;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
