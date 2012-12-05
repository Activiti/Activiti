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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author Tijs Rademakers
 */
public class BpmnModel {
  
	protected List<Process> processes = new ArrayList<Process>();
	protected Map<String, GraphicInfo> locationMap = new HashMap<String, GraphicInfo>();
	protected Map<String, GraphicInfo> labelLocationMap = new HashMap<String, GraphicInfo>();
	protected Map<String, List<GraphicInfo>> flowLocationMap = new HashMap<String, List<GraphicInfo>>();
	protected Map<String, Signal> signalMap = new HashMap<String, Signal>();
	protected Map<String, Message> messageMap = new HashMap<String, Message>();
	protected List<Pool> pools = new ArrayList<Pool>();
	protected String targetNamespace;

	public Process getMainProcess() {
	  Process process = getProcess(null);
	  return process;
	}

	public Process getProcess(String poolRef) {
	  for (Process process : processes) {
	    boolean foundPool = false;
	    for (Pool pool : pools) {
        if(pool.getProcessRef().equalsIgnoreCase(process.getId())) {
          
          if(poolRef != null) {
            if(pool.getId().equalsIgnoreCase(poolRef)) {
              foundPool = true;
            }
          } else {
            foundPool = true;
          }
        }
      }
	    
	    if(poolRef == null && foundPool == false) {
	      return process;
	    } else if(poolRef != null && foundPool == true) {
	      return process;
	    }
	  }
	  
	  return null;
  }
	
	public List<Process> getProcesses() {
	  return processes;
	}
	
	public void addProcess(Process process) {
	  processes.add(process);
	}
	
	public FlowElement getFlowElement(String id) {
	  FlowElement foundFlowElement = null;
	  for (Process process : processes) {
	    foundFlowElement = process.getFlowElement(id);
	    if (foundFlowElement != null) {
	      break;
	    }
	  }
	  
	  if (foundFlowElement == null) {
	    for (Process process : processes) {
	      for (FlowElement flowElement : process.getFlowElements()) {
	        if (flowElement instanceof SubProcess) {
	          foundFlowElement = getFlowElementInSubProcess(id, (SubProcess) flowElement);
	          if (foundFlowElement != null) {
	            break;
	          }
	        }
	      }
	      if (foundFlowElement != null) {
          break;
        }
	    }
	  }
	  
	  return foundFlowElement;
	}
	
	protected FlowElement getFlowElementInSubProcess(String id, SubProcess subProcess) {
	  FlowElement foundFlowElement = subProcess.getFlowElement(id);
    if (foundFlowElement == null) {
      for (FlowElement flowElement : subProcess.getFlowElements()) {
        if (flowElement instanceof SubProcess) {
          foundFlowElement = getFlowElementInSubProcess(id, (SubProcess) flowElement);
          if (foundFlowElement != null) {
            break;
          }
        }
      }
    }
    return foundFlowElement;
	}
	
	public void addGraphicInfo(String key, GraphicInfo graphicInfo) {
		locationMap.put(key, graphicInfo);
	}
	
	public GraphicInfo getGraphicInfo(String key) {
		return locationMap.get(key);
	}
	
	public List<GraphicInfo> getFlowLocationGraphicInfo(String key) {
    return flowLocationMap.get(key);
  }
	
	public Map<String, GraphicInfo> getLocationMap() {
		return locationMap;
	}
	
	public Map<String, List<GraphicInfo>> getFlowLocationMap() {
    return flowLocationMap;
  }
	
	public void addLabelGraphicInfo(String key, GraphicInfo graphicInfo) {
		labelLocationMap.put(key, graphicInfo);
	}
	
	public void addFlowGraphicInfoList(String key, List<GraphicInfo> graphicInfoList) {
		flowLocationMap.put(key, graphicInfoList);
	}
	
  public Collection<Signal> getSignals() {
    return signalMap.values();
  }
  
  public void addSignal(String id, String name) {
    if (StringUtils.isNotEmpty(id)) {
      signalMap.put(id, new Signal(id, name));
    }
  }
  
  public boolean containsSignalId(String signalId) {
    return signalMap.containsKey(signalId);
  }

  public Collection<Message> getMessages() {
    return messageMap.values();
  }

  public void addMessage(String id, String name) {
    if (StringUtils.isNotEmpty(id)) {
      messageMap.put(id, new Message(id, name));
    }
  }
  
  public boolean containsMessageId(String messageId) {
    return messageMap.containsKey(messageId);
  }

  public List<Pool> getPools() {
    return pools;
  }
  
  public void setPools(List<Pool> pools) {
    this.pools = pools;
  }
  
  public String getTargetNamespace() {
    return targetNamespace;
  }

  public void setTargetNamespace(String targetNamespace) {
    this.targetNamespace = targetNamespace;
  }
}
