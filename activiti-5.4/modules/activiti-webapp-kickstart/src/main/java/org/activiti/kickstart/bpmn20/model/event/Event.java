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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.activiti.kickstart.bpmn20.model.FlowNode;



/**
 * <p>Java class for tEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tFlowNode">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tEvent")
@XmlSeeAlso({
    ThrowEvent.class,
    CatchEvent.class
})
public abstract class Event
    extends FlowNode
{
	@XmlElementRefs({
		@XmlElementRef(type = MessageEventDefinition.class),
		@XmlElementRef(type = TimerEventDefinition.class),
		@XmlElementRef(type = CancelEventDefinition.class),
		@XmlElementRef(type = CompensateEventDefinition.class),
		@XmlElementRef(type = ConditionalEventDefinition.class),
		@XmlElementRef(type = ErrorEventDefinition.class),
		@XmlElementRef(type = EscalationEventDefinition.class),
		@XmlElementRef(type = LinkEventDefinition.class),
		@XmlElementRef(type = SignalEventDefinition.class),
		@XmlElementRef(type = TerminateEventDefinition.class)
	})
	List<EventDefinition> eventDefinition;
	
	@XmlElement
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	String eventDefinitionRef;
	
	/* Constructors */
	
	public Event() {}
	
	public Event(Event endEvent) {
		super(endEvent);
		this.getEventDefinition().addAll(endEvent.getEventDefinition());
		this.setEventDefinitionRef(endEvent.getEventDefinitionRef());
	}
	
	/**
	 * Helper for the import, see {@link FlowElement#isElementWithFixedSize().
	 */
    // @Override
    public boolean isElementWithFixedSize() {
		return true;
	}
    
    /**
     * For the fixed-size shape, return the fixed width.
     */
    public double getStandardWidth(){
    	return 28.0;
    }
    
    /**
     * For the fixed-size shape, return the fixed height.
     */
    public double getStandardHeight(){
    	return 28.0;
    }
	
	/**
	 * 
	 * @param type
	 * 		The {@link EventDefinition} type.
	 * @return
	 * 		The first occurrence of an {@link EventDefinition} where the type fits.
	 * 		Or null if no {@link EventDefinition} of this type exists.
	 */
	public EventDefinition getEventDefinitionOfType(Class<? extends EventDefinition> type) {
		for(EventDefinition evDef : this.getEventDefinition()) {
			if(evDef.getClass().equals(type))
				return evDef;
		}
		return null;
	}
	
	public boolean isSignalEvent() {
		return getEventDefinitionOfType(SignalEventDefinition.class) != null;
	}
	
	/* Getter & Setter */
	
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
     * {@link CompensateEventDefinition }
     * {@link TMessageEventDefinition }
     * {@link ErrorEventDefinition }
     * {@link TTimerEventDefinition }
     * {@link EventDefinition }
     * {@link ConditionalEventDefinition }
     * {@link LinkEventDefinition }
     * {@link CancelEventDefinition }
     * {@link TEscalationEventDefinition }
     * {@link SignalEventDefinition }
     * {@link TTerminateEventDefinition }
     * 
	 * @return the eventDefinition
	 */
	public List<EventDefinition> getEventDefinition() {
		if(this.eventDefinition == null) {
			this.eventDefinition = new ArrayList<EventDefinition>();
		}
		return this.eventDefinition;
	}

	/**
	 * @return the eventDefinitionRef
	 */
	public String getEventDefinitionRef() {
		return eventDefinitionRef;
	}

	/**
	 * @param eventDefinitionRef the eventDefinitionRef to set
	 */
	public void setEventDefinitionRef(String eventDefinitionRef) {
		this.eventDefinitionRef = eventDefinitionRef;
	}

}
