/**
 * Copyright (c) 2009
 * Sven Wagner-Boysen
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

package org.activiti.kickstart.bpmn20.model.activity.loop;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.Expression;
import org.activiti.kickstart.bpmn20.model.FormalExpression;
import org.activiti.kickstart.bpmn20.model.data_object.DataInput;
import org.activiti.kickstart.bpmn20.model.data_object.DataOutput;
import org.activiti.kickstart.bpmn20.model.event.EventDefinition;
import org.activiti.kickstart.bpmn20.model.misc.Property;


/**
 * <p>Java class for tMultiInstanceLoopCharacteristics complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMultiInstanceLoopCharacteristics">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tLoopCharacteristics">
 *       &lt;sequence>
 *         &lt;element name="loopCardinality" type="{http://www.omg.org/bpmn20}tExpression" minOccurs="0"/>
 *         &lt;element name="loopDataInput" type="{http://www.omg.org/bpmn20}tDataInput" minOccurs="0"/>
 *         &lt;element name="loopDataOutput" type="{http://www.omg.org/bpmn20}tDataOutput" minOccurs="0"/>
 *         &lt;element name="inputDataItem" type="{http://www.omg.org/bpmn20}tProperty" minOccurs="0"/>
 *         &lt;element name="outputDataItem" type="{http://www.omg.org/bpmn20}tProperty" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}complexBehaviorDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="completionCondition" type="{http://www.omg.org/bpmn20}tExpression" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="isSequential" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="behavior" type="{http://www.omg.org/bpmn20}tMultiInstanceFlowCondition" default="all" />
 *       &lt;attribute name="oneBehaviorEventRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="noneBehaviorEventRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMultiInstanceLoopCharacteristics", propOrder = {
    "loopCardinality",
    "loopDataInput",
    "loopDataOutput",
    "inputDataItem",
    "outputDataItem",
    "complexBehaviorDefinition",
    "completionCondition"
})
public class MultiInstanceLoopCharacteristics
    extends LoopCharacteristics
{
	
	@XmlElements({
		@XmlElement(type = FormalExpression.class),
		@XmlElement(type = Expression.class)
	})
    protected Expression loopCardinality;
    
    protected DataInput loopDataInput;
    protected DataOutput loopDataOutput;
    protected Property inputDataItem;
    protected Property outputDataItem;
    protected List<ComplexBehaviorDefinition> complexBehaviorDefinition;
    
    @XmlElements({
		@XmlElement(type = FormalExpression.class),
		@XmlElement(type = Expression.class)
	})
    protected Expression completionCondition;
    @XmlAttribute
    protected Boolean isSequential;
    @XmlAttribute
    protected MultiInstanceFlowCondition behavior;
    
    @XmlAttribute
    @XmlIDREF
    protected EventDefinition oneBehaviorEventRef;
    @XmlIDREF
    @XmlAttribute
    protected EventDefinition noneBehaviorEventRef;

    /**
     * Gets the value of the loopCardinality property.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public Expression getLoopCardinality() {
        return loopCardinality;
    }

    /**
     * Sets the value of the loopCardinality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expression }
     *     
     */
    public void setLoopCardinality(Expression value) {
        this.loopCardinality = value;
    }

    /**
     * Gets the value of the loopDataInput property.
     * 
     * @return
     *     possible object is
     *     {@link DataInput }
     *     
     */
    public DataInput getLoopDataInput() {
        return loopDataInput;
    }

    /**
     * Sets the value of the loopDataInput property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataInput }
     *     
     */
    public void setLoopDataInput(DataInput value) {
        this.loopDataInput = value;
    }

    /**
     * Gets the value of the loopDataOutput property.
     * 
     * @return
     *     possible object is
     *     {@link DataOutput }
     *     
     */
    public DataOutput getLoopDataOutput() {
        return loopDataOutput;
    }

    /**
     * Sets the value of the loopDataOutput property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataOutput }
     *     
     */
    public void setLoopDataOutput(DataOutput value) {
        this.loopDataOutput = value;
    }

    /**
     * Gets the value of the inputDataItem property.
     * 
     * @return
     *     possible object is
     *     {@link TProperty }
     *     
     */
    public Property getInputDataItem() {
        return inputDataItem;
    }

    /**
     * Sets the value of the inputDataItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link Property }
     *     
     */
    public void setInputDataItem(Property value) {
        this.inputDataItem = value;
    }

    /**
     * Gets the value of the outputDataItem property.
     * 
     * @return
     *     possible object is
     *     {@link Property }
     *     
     */
    public Property getOutputDataItem() {
        return outputDataItem;
    }

    /**
     * Sets the value of the outputDataItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link Property }
     *     
     */
    public void setOutputDataItem(Property value) {
        this.outputDataItem = value;
    }

    /**
     * Gets the value of the complexBehaviorDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the complexBehaviorDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComplexBehaviorDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ComplexBehaviorDefinition }
     * 
     * 
     */
    public List<ComplexBehaviorDefinition> getComplexBehaviorDefinition() {
        if (complexBehaviorDefinition == null) {
            complexBehaviorDefinition = new ArrayList<ComplexBehaviorDefinition>();
        }
        return this.complexBehaviorDefinition;
    }

    /**
     * Gets the value of the completionCondition property.
     * 
     * @return
     *     possible object is
     *     {@link Expression }
     *     
     */
    public Expression getCompletionCondition() {
        return completionCondition;
    }

    /**
     * Sets the value of the completionCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expression }
     *     
     */
    public void setCompletionCondition(Expression value) {
        this.completionCondition = value;
    }

    /**
     * Gets the value of the isSequential property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsSequential() {
        if (isSequential == null) {
            return false;
        } else {
            return isSequential;
        }
    }

    /**
     * Sets the value of the isSequential property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsSequential(Boolean value) {
        this.isSequential = value;
    }

    /**
     * Gets the value of the behavior property.
     * 
     * @return
     *     possible object is
     *     {@link MultiInstanceFlowCondition }
     *     
     */
    public MultiInstanceFlowCondition getBehavior() {
        if (behavior == null) {
            return MultiInstanceFlowCondition.ALL;
        } else {
            return behavior;
        }
    }

    /**
     * Sets the value of the behavior property.
     * 
     * @param value
     *     allowed object is
     *     {@link MultiInstanceFlowCondition }
     *     
     */
    public void setBehavior(MultiInstanceFlowCondition value) {
        this.behavior = value;
    }

    /**
     * Gets the value of the oneBehaviorEventRef property.
     * 
     * @return
     *     possible object is
     *     {@link EventDefinition }
     *     
     */
    public EventDefinition getOneBehaviorEventRef() {
        return oneBehaviorEventRef;
    }

    /**
     * Sets the value of the oneBehaviorEventRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventDefinition }
     *     
     */
    public void setOneBehaviorEventRef(EventDefinition value) {
        this.oneBehaviorEventRef = value;
    }

    /**
     * Gets the value of the noneBehaviorEventRef property.
     * 
     * @return
     *     possible object is
     *     {@link EventDefinition }
     *     
     */
    public EventDefinition getNoneBehaviorEventRef() {
        return noneBehaviorEventRef;
    }

    /**
     * Sets the value of the noneBehaviorEventRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventDefinition }
     *     
     */
    public void setNoneBehaviorEventRef(EventDefinition value) {
        this.noneBehaviorEventRef = value;
    }

}
