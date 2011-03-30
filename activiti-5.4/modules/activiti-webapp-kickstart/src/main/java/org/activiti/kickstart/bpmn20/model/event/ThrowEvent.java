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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tThrowEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tThrowEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tEvent">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}dataInput" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}dataInputAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}inputSet" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}eventDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="eventDefinitionRef" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tThrowEvent", propOrder = {
//    "dataInput",
//    "dataInputAssociation",
//    "inputSet",
//    "eventDefinition",
//    "eventDefinitionRef"
})
@XmlSeeAlso({
    IntermediateThrowEvent.class,
    ImplicitThrowEvent.class,
    EndEvent.class
})
public abstract class ThrowEvent
    extends Event
{

	/* Constructors */
	
	public ThrowEvent() {}
	
	public ThrowEvent(ThrowEvent endEvent) {
		super(endEvent);
	}

//    protected List<DataInput> dataInput;
//    protected List<DataInputAssociation> dataInputAssociation;
//    protected TInputSet inputSet;
//    @XmlElementRef(name = "eventDefinition", namespace = "http://www.omg.org/bpmn20", type = JAXBElement.class)
//    protected List<JAXBElement<? extends EventDefinition>> eventDefinition;
//    protected List<QName> eventDefinitionRef;

    /**
     * Gets the value of the dataInput property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataInput property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataInput().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataInput }
     * 
     * 
     */
//    public List<DataInput> getDataInput() {
//        if (dataInput == null) {
//            dataInput = new ArrayList<DataInput>();
//        }
//        return this.dataInput;
//    }

    /**
     * Gets the value of the dataInputAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataInputAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataInputAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataInputAssociation }
     * 
     * 
     */
//    public List<DataInputAssociation> getDataInputAssociation() {
//        if (dataInputAssociation == null) {
//            dataInputAssociation = new ArrayList<DataInputAssociation>();
//        }
//        return this.dataInputAssociation;
//    }

    /**
     * Gets the value of the inputSet property.
     * 
     * @return
     *     possible object is
     *     {@link TInputSet }
     *     
     */
//    public TInputSet getInputSet() {
//        return inputSet;
//    }

    /**
     * Sets the value of the inputSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link TInputSet }
     *     
     */
//    public void setInputSet(TInputSet value) {
//        this.inputSet = value;
//    }

    /**
     * Gets the value of the eventDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CompensateEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TMessageEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TErrorEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TTimerEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link EventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TConditionalEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TLinkEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TCancelEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TEscalationEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TSignalEventDefinition }{@code >}
     * {@link JAXBElement }{@code <}{@link TTerminateEventDefinition }{@code >}
     * 
     * 
     */
//    public List<JAXBElement<? extends EventDefinition>> getEventDefinition() {
//        if (eventDefinition == null) {
//            eventDefinition = new ArrayList<JAXBElement<? extends EventDefinition>>();
//        }
//        return this.eventDefinition;
//    }

    /**
     * Gets the value of the eventDefinitionRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventDefinitionRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventDefinitionRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
//    public List<QName> getEventDefinitionRef() {
//        if (eventDefinitionRef == null) {
//            eventDefinitionRef = new ArrayList<QName>();
//        }
//        return this.eventDefinitionRef;
//    }

}
