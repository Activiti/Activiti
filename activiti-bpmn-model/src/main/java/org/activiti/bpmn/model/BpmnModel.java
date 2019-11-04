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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BpmnModel {

  protected Map<String, List<ExtensionAttribute>> definitionsAttributes = new LinkedHashMap<String, List<ExtensionAttribute>>();
  protected List<Process> processes = new ArrayList<Process>();
  protected Map<String, GraphicInfo> locationMap = new LinkedHashMap<String, GraphicInfo>();
  protected Map<String, GraphicInfo> labelLocationMap = new LinkedHashMap<String, GraphicInfo>();
  protected Map<String, List<GraphicInfo>> flowLocationMap = new LinkedHashMap<String, List<GraphicInfo>>();
  protected List<Signal> signals = new ArrayList<Signal>();
  protected Map<String, MessageFlow> messageFlowMap = new LinkedHashMap<String, MessageFlow>();
  protected Map<String, Message> messageMap = new LinkedHashMap<String, Message>();
  protected Map<String, Error> errorMap = new LinkedHashMap<String, Error>();
  protected Map<String, ItemDefinition> itemDefinitionMap = new LinkedHashMap<String, ItemDefinition>();
  protected Map<String, DataStore> dataStoreMap = new LinkedHashMap<String, DataStore>();
  protected List<Pool> pools = new ArrayList<Pool>();
  protected List<Import> imports = new ArrayList<Import>();
  protected List<Interface> interfaces = new ArrayList<Interface>();
  protected List<Artifact> globalArtifacts = new ArrayList<Artifact>();
  protected List<Resource> resources = new ArrayList<Resource>();
  protected Map<String, String> namespaceMap = new LinkedHashMap<String, String>();
  protected String targetNamespace;
  protected String sourceSystemId;
  protected List<String> userTaskFormTypes;
  protected List<String> startEventFormTypes;
  protected int nextFlowIdCounter = 1;
  protected Object eventSupport;

  public Map<String, List<ExtensionAttribute>> getDefinitionsAttributes() {
    return definitionsAttributes;
  }

  public String getDefinitionsAttributeValue(String namespace, String name) {
    List<ExtensionAttribute> attributes = getDefinitionsAttributes().get(name);
    if (attributes != null && !attributes.isEmpty()) {
      for (ExtensionAttribute attribute : attributes) {
        if (namespace.equals(attribute.getNamespace()))
          return attribute.getValue();
      }
    }
    return null;
  }

  public void addDefinitionsAttribute(ExtensionAttribute attribute) {
    if (attribute != null && StringUtils.isNotEmpty(attribute.getName())) {
      List<ExtensionAttribute> attributeList = null;
      if ( !this.definitionsAttributes.containsKey(attribute.getName())) {
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
    if (!getPools().isEmpty()) {
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

          if (poolRef != null) {
            if (pool.getId().equalsIgnoreCase(poolRef)) {
              foundPool = true;
            }
          } else {
            foundPool = true;
          }
        }
      }

      if (poolRef == null && !foundPool) {
        return process;
      } else if (poolRef != null && foundPool) {
        return process;
      }
    }

    return null;
  }

  public Process getProcessById(String id) {
    for (Process process : processes) {
      if (process.getId().equals(id)) {
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

  public boolean hasDiagramInterchangeInfo() {
    return !locationMap.isEmpty();
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

  public Collection<Resource> getResources() {
    return resources;
  }

  public void setResources(Collection<Resource> resourceList) {
    if (resourceList != null) {
      resources.clear();
      resources.addAll(resourceList);
    }
  }

  public void addResource(Resource resource) {
    if (resource != null) {
      resources.add(resource);
    }
  }

  public boolean containsResourceId(String resourceId) {
    return getResource(resourceId) != null;
  }

  public Resource getResource(String id) {
    for (Resource resource : resources) {
      if (id.equals(resource.getId())) {
        return resource;
      }
    }
    return null;
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

  public Map<String, MessageFlow> getMessageFlows() {
    return messageFlowMap;
  }

  public void setMessageFlows(Map<String, MessageFlow> messageFlows) {
    this.messageFlowMap = messageFlows;
  }

  public void addMessageFlow(MessageFlow messageFlow) {
    if (messageFlow != null && StringUtils.isNotEmpty(messageFlow.getId())) {
      messageFlowMap.put(messageFlow.getId(), messageFlow);
    }
  }

  public MessageFlow getMessageFlow(String id) {
    return messageFlowMap.get(id);
  }

  public boolean containsMessageFlowId(String messageFlowId) {
    return messageFlowMap.containsKey(messageFlowId);
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
    Message result = messageMap.get(id);
    if (result == null) {
      int indexOfNS = id.indexOf(":");
      if (indexOfNS > 0) {
        String idNamespace = id.substring(0, indexOfNS);
        if (idNamespace.equalsIgnoreCase(this.getTargetNamespace())) {
          id = id.substring(indexOfNS + 1);
        }
        result = messageMap.get(id);
      }
    }
    return result;
  }

  public boolean containsMessageId(String messageId) {
    return messageMap.containsKey(messageId);
  }

  public Map<String, Error> getErrors() {
      return errorMap;
  }

  public void setErrors(Map<String, Error> errorMap) {
    this.errorMap = errorMap;
  }

  public void addError(String errorRef,
                       String errorName,
                       String errorCode) {
    if (StringUtils.isNotEmpty(errorRef)) {
      errorMap.put(errorRef, new Error(errorRef, errorName, errorCode));
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

  public Map<String, DataStore> getDataStores() {
    return dataStoreMap;
  }

  public void setDataStores(Map<String, DataStore> dataStoreMap) {
    this.dataStoreMap = dataStoreMap;
  }

  public DataStore getDataStore(String id) {
    DataStore dataStore = null;
    if (dataStoreMap.containsKey(id)) {
      dataStore = dataStoreMap.get(id);
    }
    return dataStore;
  }

  public void addDataStore(String id, DataStore dataStore) {
    if (StringUtils.isNotEmpty(id)) {
      dataStoreMap.put(id, dataStore);
    }
  }

  public boolean containsDataStore(String id) {
    return dataStoreMap.containsKey(id);
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

  public String getSourceSystemId() {
    return sourceSystemId;
  }

  public void setSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
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

  @JsonIgnore
  public Object getEventSupport() {
    return eventSupport;
  }

  public void setEventSupport(Object eventSupport) {
    this.eventSupport = eventSupport;
  }

  public String getStartFormKey(String processId) {
    FlowElement initialFlowElement = getProcessById(processId)
            .getInitialFlowElement();
    if (initialFlowElement instanceof StartEvent) {
      StartEvent startEvent = (StartEvent) initialFlowElement;
      return startEvent.getFormKey();
    }
    return null;
  }
}
