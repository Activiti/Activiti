/**
 * Copyright (c) 2010
 * Signavio
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
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.diagram.UUIDGenerator;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.Documentation;


/**
 * <p>Java class for tMonitoring complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tMonitoring">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMonitoring")
public class Monitoring
    extends BaseElement
{
	/* Constructors */
	
	public Monitoring() {}
	
	/**
	 * 
	 * @param value
	 */
	public Monitoring(String value) {
		this.setId(UUIDGenerator.generate());
		this.getDocumentation().add(new Documentation(value));
	}
	
	public String toExportString() {
		if(this.getDocumentation().size() == 0)	return null;
		if(this.getDocumentation().get(0) != null)
			return this.getDocumentation().get(0).getText();
		else return null;
	}
}
