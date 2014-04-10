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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Tijs Rademakers
 */
public class BpmnModel {
  
  protected Map<String, List<ExtensionAttribute>> definitionsAttributes = new LinkedHashMap<String, List<ExtensionAttribute>>();
	protected List<Process> processes = new ArrayList<Process>();
	protected Map<String, GraphicInfo> locationMap = new LinkedHashMap<String, GraphicInfo>();
	protected Map<String, GraphicInfo> labelLocationMap = new LinkedHashMap<String, GraphicInfo>();
	protected Map<String, List<GraphicInfo>> flowLocationMap = new LinkedHashMap<String, List<GraphicInfo>>();
	protected List<Signal> signals = new ArrayList<Signal>();
	protected Map<String, Message> messageMap = new LinkedHashMap<String, Message>();
	protected Map<String, String> errorMap = new LinkedHashMap<String, String>();
	protected Map<String, ItemDefinition> itemDefinitionMap = new LinkedHashMap<String, ItemDefinition>();
	protected List<Pool> pools = new ArrayList<Pool>();
	protected List<Import> imports = new ArrayList<Import>();
	protected List<Interface> interfaces = new ArrayList<Interface>();
	protected List<Artifact> globalArtifacts = new ArrayList<Artifact>();
	protected Map<String, String> namespaceMap = new LinkedHashMap<String, String>();
	protected String targetNamespace;
	protected List<String> userTaskFormTypes;
  protected List<String> startEventFormTypes;
	protected int nextFlowIdCounter = 1;
	
	
	public Map<String, List<ExtensionAttribute>> getDefinitionsAttributes() {
    return definitionsAttributes;
  }

  public String getDefinitionsAttributeValue(String namespace, String name) {
    List<ExtensionAttribute> attributes = getDefinitionsAttributes().get(name);
    if (attributes != null && !attributes.isEmpty()) {
      for (ExtensionAttribute attribute : attributes) {
        if ( namespace.equals(attribute.getNamespace()))
          return attribute.getValue();
      }
    }
    return null;
  }

  public void addDefinitionsAttribute(ExtensionAttribute attribute) {
    if (attribute != null && StringUtils.isNotEmpty(attribute.getName())) {
      List<ExtensionAttribute> attributeList = null;
      if (this.definitionsAttributes.containsKey(attribute.getName()) == false) {
        attributeList = new ArrayList<ExtensionAttribute>();
        this.definitionsAttributes.put(attribute.getName(), attributeList);
      }
      this.definitionsAttributes.get(attribute.getName()).add(attribute);
    }
  }

  public void setDefinitionsAttributes(Map<String, List<ExtensionAttribute>> attributes) {
    this.definitionsAttributes = attributes;
  }

	public Process getMainProcess() {
	  if (getPools().size() > 0) {
	    return getProcess(getPools().get(0).getId());
	  } else {
	    return getProcess(null);
	  }
	}

	public Process getProcess(String poolRef) {
	  for (Process process : processes) {
	    boolean foundPool = false;
	    for (Pool pool : pools) {
        if (StringUtils.isNotEmpty(pool.getProcessRef()) && pool.getProcessRef().equalsIgnoreCase(process.getId())) {
          
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
	
	public Pool getPool(String id) {
	  Pool foundPool = null;
	  if (StringUtils.isNotEmpty(id)) {
  	  for (Pool pool : pools) {
        if (id.equals(pool.getId())) {
          foundPool = pool;
          break;
        }
      }
	  }
	  return foundPool;
	}
	
	public Lane getLane(String id) {
	  Lane foundLane = null;
    if (StringUtils.isNotEmpty(id)) {
      for (Process process : processes) {
        for (Lane lane : process.getLanes()) {
          if (id.equals(lane.getId())) {
            foundLane = lane;
            break;
          }
        }
        if (foundLane != null) {
          break;
        }
      }
    }
    return foundLane;
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
	      for (FlowElement flowElement : process.findFlowElementsOfType(SubProcess.class)) {
	        foundFlowElement = getFlowElementInSubProcess(id, (SubProcess) flowElement);
	        if (foundFlowElement != null) {
	          break;
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
	
	public Artifact getArtifact(String id) {
	  Artifact foundArtifact = null;
    for (Process process : processes) {
      foundArtifact = process.getArtifact(id);
      if (foundArtifact != null) {
        break;
      }
    }
    
    if (foundArtifact == null) {
      for (Process process : processes) {
        for (FlowElement flowElement : process.findFlowElementsOfType(SubProcess.class)) {
          foundArtifact = getArtifactInSubProcess(id, (SubProcess) flowElement);
          if (foundArtifact != null) {
            break;
          }
        }
        if (foundArtifact != null) {
          break;
        }
      }
    }
    
    return foundArtifact;
  }
  
  protected Artifact getArtifactInSubProcess(String id, SubProcess subProcess) {
    Artifact foundArtifact = subProcess.getArtifact(id);
    if (foundArtifact == null) {
      for (FlowElement flowElement : subProcess.getFlowElements()) {
        if (flowElement instanceof SubProcess) {
          foundArtifact = getArtifactInSubProcess(id, (SubProcess) flowElement);
          if (foundArtifact != null) {
            break;
          }
        }
      }
    }
    return foundArtifact;
  }
	
	public void addGraphicInfo(String key, GraphicInfo graphicInfo) {
		locationMap.put(key, graphicInfo);
	}
	
	public GraphicInfo getGraphicInfo(String key) {
		return locationMap.get(key);
	}
	
	public void removeGraphicInfo(String key) {
    locationMap.remove(key);
  }
	
	public List<GraphicInfo> getFlowLocationGraphicInfo(String key) {
    return flowLocationMap.get(key);
  }
	
	public void removeFlowGraphicInfoList(String key) {
	  flowLocationMap.remove(key);
  }
	
	public Map<String, GraphicInfo> getLocationMap() {
		return locationMap;
	}
	
	public Map<String, List<GraphicInfo>> getFlowLocationMap() {
    return flowLocationMap;
  }
	
	public GraphicInfo getLabelGraphicInfo(String key) {
    return labelLocationMap.get(key);
  }
	
	public void addLabelGraphicInfo(String key, GraphicInfo graphicInfo) {
		labelLocationMap.put(key, graphicInfo);
	}
	
	public void removeLabelGraphicInfo(String key) {
	  labelLocationMap.remove(key);
  }
	
	public Map<String, GraphicInfo> getLabelLocationMap() {
    return labelLocationMap;
  }
	
	public void addFlowGraphicInfoList(String key, List<GraphicInfo> graphicInfoList) {
		flowLocationMap.put(key, graphicInfoList);
	}
	
  public Collection<Signal> getSignals() {
  	return signals;
  }
  
  public void setSignals(Collection<Signal> signalList) {
    if (signalList != null) {
      signals.clear();
      signals.addAll(signalList);
    }
  }
  
  public void addSignal(Signal signal) {
    if (signal != null) {
    	signals.add(signal);
    }
  }
  
  public boolean containsSignalId(String signalId) {
    return getSignal(signalId) != null;
  }
  
  public Signal getSignal(String id) {
    for (Signal signal : signals) {
    	if (id.equals(signal.getId())) {
    		return signal;
    	}
    }
    return null;
  }

  public Collection<Message> getMessages() {
    return messageMap.values();
  }
  
  public void setMessages(Collection<Message> messageList) {
    if (messageList != null) {
      messageMap.clear();
      for (Message message : messageList) {
        addMessage(message);
      }
    }
  }

  public void addMessage(Message message) {
    if (message != null && StringUtils.isNotEmpty(message.getId())) {
      messageMap.put(message.getId(), message);
    }
  }
  
  public Message getMessage(String id) {
    return messageMap.get(id);
  }
  
  public boolean containsMessageId(String messageId) {
    return messageMap.containsKey(messageId);
  }
  
  public Map<String, String> getErrors() {
    return errorMap;
  }
  
  public void setErrors(Map<String, String> errorMap) {
    this.errorMap = errorMap;
  }

  public void addError(String errorRef, String errorCode) {
    if (StringUtils.isNotEmpty(errorRef)) {
      errorMap.put(errorRef, errorCode);
    }
  }
  
  public boolean containsErrorRef(String errorRef) {
    return errorMap.containsKey(errorRef);
  }
  
  public Map<String, ItemDefinition> getItemDefinitions() {
    return itemDefinitionMap;
  }
  
  public void setItemDefinitions(Map<String, ItemDefinition> itemDefinitionMap) {
    this.itemDefinitionMap = itemDefinitionMap;
  }

  public void addItemDefinition(String id, ItemDefinition item) {
    if (StringUtils.isNotEmpty(id)) {
      itemDefinitionMap.put(id, item);
    }
  }
  
  public boolean containsItemDefinitionId(String id) {
    return itemDefinitionMap.containsKey(id);
  }

  public List<Pool> getPools() {
    return pools;
  }
  
  public void setPools(List<Pool> pools) {
    this.pools = pools;
  }
  
  public List<Import> getImports() {
    return imports;
  }

  public void setImports(List<Import> imports) {
    this.imports = imports;
  }

  public List<Interface> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<Interface> interfaces) {
    this.interfaces = interfaces;
  }

  public List<Artifact> getGlobalArtifacts() {
    return globalArtifacts;
  }

  public void setGlobalArtifacts(List<Artifact> globalArtifacts) {
    this.globalArtifacts = globalArtifacts;
  }

  public void addNamespace(String prefix, String uri) {
    namespaceMap.put(prefix, uri);
  }
  
  public boolean containsNamespacePrefix(String prefix) {
    return namespaceMap.containsKey(prefix);
  }
  
  public String getNamespace(String prefix) {
    return namespaceMap.get(prefix);
  }
  
  public Map<String, String> getNamespaces() {
    return namespaceMap;
  }
  
  public String getTargetNamespace() {
    return targetNamespace;
  }

  public void setTargetNamespace(String targetNamespace) {
    this.targetNamespace = targetNamespace;
  }
  
  public List<String> getUserTaskFormTypes() {
    return userTaskFormTypes;
  }
  
  public void setUserTaskFormTypes(List<String> userTaskFormTypes) {
    this.userTaskFormTypes = userTaskFormTypes;
  }
  
  public List<String> getStartEventFormTypes() {
    return startEventFormTypes;
  }
  
  public void setStartEventFormTypes(List<String> startEventFormTypes) {
    this.startEventFormTypes = startEventFormTypes;
  }
}
