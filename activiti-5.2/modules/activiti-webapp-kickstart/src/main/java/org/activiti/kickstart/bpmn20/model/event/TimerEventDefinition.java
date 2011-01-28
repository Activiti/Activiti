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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.Expression;


/**
 * <p>Java class for tTimerEventDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tTimerEventDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tEventDefinition">
 *       &lt;choice>
 *         &lt;element name="timeDate" type="{http://www.omg.org/bpmn20}tExpression" minOccurs="0"/>
 *         &lt;element name="timeCycle" type="{http://www.omg.org/bpmn20}tExpression" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tTimerEventDefinition", propOrder = {
    "timeDate",
    "timeCycle",
    "timeDuration"
})
public class TimerEventDefinition
    extends EventDefinition
{

	/* Attributes */
	
    protected Expression timeDate;
    protected Expression timeCycle;
    protected Expression timeDuration;
    
    /* Constructors */
    
    /**
     * Default constructor
     */
    public TimerEventDefinition() {}

    /**
     * Copy constructor based on {@link TimerEventDefinition}
     * 
     * @param timerEventDefinition
     */
    public TimerEventDefinition(TimerEventDefinition timerEventDefinition) {
		super(timerEventDefinition);
		
		this.setTimeDate(timerEventDefinition.getTimeDate());
		this.setTimeCycle(timerEventDefinition.getTimeCycle());
	}
    
    /* Getter & Setter */

	/**
     * Gets the value of the timeDate property.
     * 
     * @return
     *     possible object is
     *     {@link Expression }
     *     
     */
    public Expression getTimeDate() {
        return timeDate;
    }

    /**
     * Sets the value of the timeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expression }
     *     
     */
    public void setTimeDate(Expression value) {
        this.timeDate = value;
    }

    /**
     * Gets the value of the timeCycle property.
     * 
     * @return
     *     possible object is
     *     {@link Expression }
     *     
     */
    public Expression getTimeCycle() {
        return timeCycle;
    }

    /**
     * Sets the value of the timeCycle property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expression }
     *     
     */
    public void setTimeCycle(Expression value) {
        this.timeCycle = value;
    }

	public Expression getTimeDuration() {
		return timeDuration;
	}

	public void setTimeDuration(Expression timeDuration) {
		this.timeDuration = timeDuration;
	}

}
