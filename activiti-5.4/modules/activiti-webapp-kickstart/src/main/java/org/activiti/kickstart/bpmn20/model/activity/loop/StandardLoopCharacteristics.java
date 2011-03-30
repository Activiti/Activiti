/**
 * Copyright (c) 20010
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

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.FormalExpression;


/**
 * <p>Java class for tStandardLoopCharacteristics complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tStandardLoopCharacteristics">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tLoopCharacteristics">
 *       &lt;sequence>
 *         &lt;element name="loopCondition" type="{http://www.omg.org/bpmn20}tExpression" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="testBefore" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="loopMaximum" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tStandardLoopCharacteristics", propOrder = {
    "loopCondition"
})
public class StandardLoopCharacteristics
    extends LoopCharacteristics
{
	
	@XmlElement(name = "loopCondition", type = FormalExpression.class)
    protected FormalExpression loopCondition;
    @XmlAttribute
    protected Boolean testBefore;
    @XmlAttribute
    protected BigInteger loopMaximum;

    /**
     * Gets the value of the loopCondition property.
     * 
     * @return
     *     possible object is
     *     {@link FormalExpression }
     *     
     */
    public FormalExpression getLoopCondition() {
        return loopCondition;
    }

    /**
     * Sets the value of the loopCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link FormalExpression }
     *     
     */
    public void setLoopCondition(FormalExpression value) {
        this.loopCondition = value;
    }

    /**
     * Gets the value of the testBefore property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTestBefore() {
        if (testBefore == null) {
            return false;
        } else {
            return testBefore;
        }
    }

    /**
     * Sets the value of the testBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTestBefore(Boolean value) {
        this.testBefore = value;
    }

    /**
     * Gets the value of the loopMaximum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLoopMaximum() {
        return loopMaximum;
    }

    /**
     * Sets the value of the loopMaximum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLoopMaximum(BigInteger value) {
        this.loopMaximum = value;
    }

}
