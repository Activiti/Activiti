/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.language.bpmn.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.CallActivity;
import org.activiti.editor.language.bpmn.model.IOParameter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class CallActivityParser extends BaseBpmnElementParser {
  
  public CallActivityParser() {
  	InParameterParser inParameterParser = new InParameterParser();
    childElementParsers.put(inParameterParser.getElementName(), inParameterParser);
    OutParameterParser outParameterParser = new OutParameterParser();
    childElementParsers.put(outParameterParser.getElementName(), outParameterParser);
  }
  
  public static String getElementName() {
    return "userTask";
  }

  protected BaseElement parseElement() {
  	CallActivity callActivity = new CallActivity();
		callActivity.setCalledElement(xtr.getAttributeValue(null, "calledElement"));
		parseChildElements(getElementName(), callActivity);
		return callActivity;
  }
  
  public class InParameterParser extends BaseChildElementParser {

    public String getElementName() {
      return "in";
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    	String source = xtr.getAttributeValue(null, "source");
    	String sourceExpression = xtr.getAttributeValue(null, "sourceExpression");
    	String target = xtr.getAttributeValue(null, "target");
    	String targetExpression = xtr.getAttributeValue(null, "targetExpression");
    	if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && 
    			(StringUtils.isNotEmpty(target) || StringUtils.isNotEmpty(targetExpression))) {
    		
      	IOParameter parameter = new IOParameter();
      	if(StringUtils.isNotEmpty(sourceExpression)) {
      		parameter.setSourceExpression(sourceExpression);
      	} else {
      		parameter.setSource(source);
      	}
      	
      	if(StringUtils.isNotEmpty(targetExpression)) {
      		parameter.setTargetExpression(targetExpression);
      	} else {
      		parameter.setTarget(target);
      	}
        ((CallActivity) parentElement).getInParameters().add(parameter);
    	}
    }
  }
  
  public class OutParameterParser extends BaseChildElementParser {

    public String getElementName() {
      return "out";
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    	String source = xtr.getAttributeValue(null, "source");
    	String sourceExpression = xtr.getAttributeValue(null, "sourceExpression");
    	String target = xtr.getAttributeValue(null, "target");
    	String targetExpression = xtr.getAttributeValue(null, "targetExpression");
    	if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && 
    			(StringUtils.isNotEmpty(target) || StringUtils.isNotEmpty(targetExpression))) {
    		
      	IOParameter parameter = new IOParameter();
      	if(StringUtils.isNotEmpty(sourceExpression)) {
      		parameter.setSourceExpression(sourceExpression);
      	} else {
      		parameter.setSource(source);
      	}
      	
      	if(StringUtils.isNotEmpty(targetExpression)) {
      		parameter.setTargetExpression(targetExpression);
      	} else {
      		parameter.setTarget(target);
      	}
      	((CallActivity) parentElement).getOutParameters().add(parameter);
    	}
    }
  }
}
