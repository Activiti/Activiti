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

package org.activiti.kickstart.bpmn20.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.activiti.kickstart.bpmn20.diagram.UUIDGenerator;
import org.activiti.kickstart.bpmn20.util.EscapingStringAdapter;


/**
 * <p>Java class for tDocumentation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tDocumentation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tDocumentation", propOrder = {
		"text"
//		"content"
})
public class Documentation extends BaseElement {
	
	/**
	 * Default constructor
	 */
	public Documentation() {
		
	}
	
	/**
	 * Constructor including documentation's text parameter
	 * @param text
	 */
	public Documentation(String text) {
		this.setText(text);
		this.setId(UUIDGenerator.generate());
	}
	
	@XmlMixed
	@XmlJavaTypeAdapter(EscapingStringAdapter.class)
    protected List<String> text;
    
    /* Getter & Setter */
    
	/**
	 * @return the text
	 */
	public String getText() {
		if(this.text == null)
			return "";
		if(text.size() > 0)
			return text.get(0);
		
		return "";
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
//		this.text = text;
		
		this.text = new ArrayList<String>();
		this.text.add(text);
	}
    
//	@XmlMixed
//    @XmlAnyElement(lax = true)
//    protected List<Object> content;
//
//    /**
//     * Gets the value of the content property.
//     * 
//     * <p>
//     * This accessor method returns a reference to the live list,
//     * not a snapshot. Therefore any modification you make to the
//     * returned list will be present inside the JAXB object.
//     * This is why there is not a <CODE>set</CODE> method for the content property.
//     * 
//     * <p>
//     * For example, to add a new item, do as follows:
//     * <pre>
//     *    getContent().add(newItem);
//     * </pre>
//     * 
//     * 
//     * <p>
//     * Objects of the following type(s) are allowed in the list
//     * {@link Object }
//     * {@link String }
//     * {@link Element }
//     * 
//     * 
//     */
//    public List<Object> getContent() {
//        if (content == null) {
//            content = new ArrayList<Object>();
//        }
//        return this.content;
//    }

}
