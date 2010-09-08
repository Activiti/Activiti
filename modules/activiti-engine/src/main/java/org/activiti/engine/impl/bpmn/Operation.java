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
package org.activiti.engine.impl.bpmn;

import java.util.ArrayList;
import java.util.List;


/**
 * An Operation is part of an {@link BpmnInterface} and it defines Messages that are consumed and
 * (optionally) produced when the Operation is called.
 * 
 * @author Joram Barrez
 */
public class Operation {
  
  protected String id;
  
  protected String name;
  
  protected List<String> inArguments;
  
  protected List<String> outArguments;
  
  /**
   * The interface to which this operations belongs
   */
  protected BpmnInterface bpmnInterface;
  
  public Operation() {
    
  }
  
  public Operation(String id, String name, BpmnInterface bpmnInterface) {
    this.inArguments = new ArrayList<String>();
    this.outArguments = new ArrayList<String>();
	    
    setId(id);
    setName(name);
    setInterface(bpmnInterface);
  }
  
	public void addInArgument(String argument) {
		this.inArguments.add(argument);
	}

	public void addOutArgument(String argument) {
		this.outArguments.add(argument);
	}

	public int getInArgumentsSize() {
		return this.inArguments.size();
	}

	public int getOutArgumentsSize() {
		return this.outArguments.size();
	}

	public String getInArgument(int index) {
		return this.inArguments.get(index);
	}

	public String getOutArgument(int index) {
		return this.outArguments.get(index);
	}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public BpmnInterface getInterface() {
    return bpmnInterface;
  }

  public void setInterface(BpmnInterface bpmnInterface) {
    this.bpmnInterface = bpmnInterface;
  }
  
}
