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

package org.activiti.kickstart.bpmn20.model.activity.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.activity.Task;
import org.activiti.kickstart.bpmn20.model.activity.misc.BusinessRuleTaskImplementation;


/**
 * <p>Java class for tBusinessRuleTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tBusinessRuleTask">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tTask">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tBusinessRuleTask")
public class BusinessRuleTask
    extends Task
{
	@XmlAttribute
	BusinessRuleTaskImplementation implementation;
	
	/* Constructors */
	
	/**
	 * Default constructor
	 */
	public BusinessRuleTask() {	}
	
	/**
	 * Copy constructor
	 * 
	 * @param brTask
	 * 		Template {@link BusinessRuleTask}
	 */
	public BusinessRuleTask(BusinessRuleTask brTask) {
		super(brTask);
		this.setImplementation(brTask.getImplementation());
	}
	

	/* Getter & Setter */
	
	/**
	 * @return the implementation
	 */
	public BusinessRuleTaskImplementation getImplementation() {
		return implementation;
	}

	/**
	 * @param implementation the implementation to set
	 */
	public void setImplementation(BusinessRuleTaskImplementation implementation) {
		this.implementation = implementation;
	}

}
