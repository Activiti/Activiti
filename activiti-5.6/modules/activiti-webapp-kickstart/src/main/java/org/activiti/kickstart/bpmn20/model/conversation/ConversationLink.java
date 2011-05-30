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

package org.activiti.kickstart.bpmn20.model.conversation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.connector.Edge;

/**
 * Class representing a conversation link.
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConversationLink extends Edge implements ConversationElement {
	
	
	/**
	 * Ensures that the target element is of the type conversation element and 
	 * returns it.
	 */
	public FlowElement getTargetRef() {
//		if(!(super.getTargetRef() instanceof ConversationElement))
//			return null;
		return super.getTargetRef();
	}
	
	public void setTargetRef(ConversationElement targetEle) {
		if(targetEle instanceof FlowElement) {
			super.setTargetRef((FlowElement) targetEle);
		}
	}
	
	/**
	 * Ensures that the source element is of the type conversation element and 
	 * returns it.
	 */
	public FlowElement getSourceRef() {
//		if(!(super.getSourceRef() instanceof ConversationElement))
//			return null;
		return super.getSourceRef();
	}
	
	public void setSourceRef(ConversationElement sourceEle) {
		if(sourceEle instanceof FlowElement) {
			super.setSourceRef((FlowElement) sourceEle);
		}
	}
	
}
