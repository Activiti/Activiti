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

package org.activiti.kickstart.bpmn20.model.data_object;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.BaseElement;


/**
 * <p>Java class for tInputOutputSpecification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tInputOutputSpecification">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}dataInput" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}dataOutput" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}inputSet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}outputSet" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tInputOutputSpecification", propOrder = {
    "dataInput",
    "dataOutput",
    "inputSet",
    "outputSet"
})
public class InputOutputSpecification
    extends BaseElement
{
	
    protected List<DataInput> dataInput;
    protected List<DataOutput> dataOutput;
    protected List<InputSet> inputSet;
    protected List<OutputSet> outputSet;

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
    public List<DataInput> getDataInput() {
        if (dataInput == null) {
            dataInput = new ArrayList<DataInput>();
        }
        return this.dataInput;
    }

    /**
     * Gets the value of the dataOutput property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataOutput property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataOutput().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataOutput }
     * 
     * 
     */
    public List<DataOutput> getDataOutput() {
        if (dataOutput == null) {
            dataOutput = new ArrayList<DataOutput>();
        }
        return this.dataOutput;
    }

    /**
     * Gets the value of the inputSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inputSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInputSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TInputSet }
     * 
     * 
     */
    public List<InputSet> getInputSet() {
        if (inputSet == null) {
            inputSet = new ArrayList<InputSet>();
        }
        return this.inputSet;
    }

    /**
     * Gets the value of the outputSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outputSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutputSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TOutputSet }
     * 
     * 
     */
    public List<OutputSet> getOutputSet() {
        if (outputSet == null) {
            outputSet = new ArrayList<OutputSet>();
        }
        return this.outputSet;
    }

}
