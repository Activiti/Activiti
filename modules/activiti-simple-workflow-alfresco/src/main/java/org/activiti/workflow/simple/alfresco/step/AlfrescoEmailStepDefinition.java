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
package org.activiti.workflow.simple.alfresco.step;

import java.util.HashMap;

import org.activiti.workflow.simple.definition.AbstractNamedStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

public class AlfrescoEmailStepDefinition extends AbstractNamedStepDefinition {

  private static final long serialVersionUID = 1L;
  
	protected String from;
	protected String to;
	protected String cc;
	protected String subject;
	protected String body;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
  public StepDefinition clone() {
		AlfrescoEmailStepDefinition clone = new AlfrescoEmailStepDefinition();
		clone.setValues(this);
	  return clone;
  }

	@Override
  public void setValues(StepDefinition otherDefinition) {
		if(!(otherDefinition instanceof AlfrescoEmailStepDefinition)) {
			throw new SimpleWorkflowException("An instance of AlfrescoEmailStepDefinition is required to set values");
		}
		AlfrescoEmailStepDefinition other = (AlfrescoEmailStepDefinition) otherDefinition;
		
	  setBody(other.getBody());
	  setCc(other.getCc());
	  setDescription(other.getDescription());
	  setFrom(other.getFrom());
	  setId(other.getId());
	  setName(other.getName());
	  setSubject(other.getSubject());
	  setTo(other.getTo());
	  
	  if(other.getParameters() != null) {
	  	setParameters(new HashMap<String, Object>(other.getParameters()));
	  }
	  
	  
  }

}
