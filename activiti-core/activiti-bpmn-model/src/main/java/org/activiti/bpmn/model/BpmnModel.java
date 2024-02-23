/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.model;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BpmnModel {

  protected Map<String, List<ExtensionAttribute>> definitionsAttributes = new LinkedHashMap<String, List<ExtensionAttribute>>();
  protected List<Process> processes = new ArrayList<>();
  protected Map<String, GraphicInfo> locationMap = new LinkedHashMap<>();
  protected Map<String, GraphicInfo> labelLocationMap = new LinkedHashMap<>();
  protected Map<String, List<GraphicInfo>> flowLocationMap = new LinkedHashMap<>();
  protected List<Signal> signals = new ArrayList<Signal>();
  protected Map<String, MessageFlow> messageFlowMap = new LinkedHashMap<>();
  protected Map<String, Message> messageMap = new LinkedHashMap<>();
  protected Map<String, Error> errorMap = new LinkedHashMap<>();
  protected Map<String, ItemDefinition> itemDefinitionMap = new LinkedHashMap<>();
  protected Map<String, DataStore> dataStoreMap = new LinkedHashMap<>();
  protected List<Pool> pools = new ArrayList<>();
  protected List<Import> imports = new ArrayList<>();
  protected List<Interface> interfaces = new ArrayList<>();
  protected List<Artifact> globalArtifacts = new ArrayList<>();
  protected List<Resource> resources = new ArrayList<>();
  protected Map<String, String> namespaceMap = new LinkedHashMap<>();
  protected String targetNamespace;
  protected String sourceSystemId;
  protected List<String> userTaskFormTypes;
  protected List<String> startEventFormTypes;
  protected Object eventSupport;

  public Map<String, List<ExtensionAttribute>> getDefinitionsAttributes() {
    return definitionsAttributes;
  }

  public String getDefinitionsAttributeValue(String namespace, String name) {
    var attributes = getDefinitionsAttributes().get(name);

    if (attributes == null || attributes.isEmpty()) {
      return null;
    }

    return attributes.stream()
      .filter(a -> namespace.equals(a.getNamespace()))
      .map(ExtensionAttribute::getValue)
      .findFirst()
      .orElse(null);
  }

  public void addDefinitionsAttribute(ExtensionAttribute attribute) {

    if (attribute == null || isEmpty(attribute.getName())) {
      return;
    }

    if (!this.definitionsAttributes.containsKey(attribute.getName())) {
      this.definitionsAttributes.put(attribute.getName(), new ArrayList<>());
    }

    this.definitionsAttributes.get(attribute.getName()).add(attribute);
  }

  public Process getMainProcess() {
    if (getPools().isEmpty()) {
      return getProcess(null);
    }

    return getProcess(getPools().getFirst().getId());
  }

  public Process getProcess(String poolRef) {
    for (Process process : processes) {
      boolean foundPool = false;
      for (Pool pool : pools) {
        if (isNotEmpty(pool.getProcessRef()) &&
          pool.getProcessRef().equalsIgnoreCase(process.getId())) {

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
      }

      if (poolRef != null && foundPool) {
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

    if (isEmpty(id)) {
      return null;
    }

    return pools.stream()
      .filter(p->id.equals(p.getId()))
      .findFirst()
      .orElse(null);
  }

  public Lane getLane(String id) {

    if (isEmpty(id)) {
      return null;
    }

    return processes.stream()
      .flatMap(p-> p.getLanes().stream())
      .filter(l-> id.equals(l.getId()))
      .findFirst()
      .orElse(null);
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
        for (SubProcess flowElement : process.findFlowElementsOfType(SubProcess.class)) {
          foundFlowElement = getFlowElementInSubProcess(id, flowElement);
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

  public List<GraphicInfo> getFlowLocationGraphicInfo(String key) {
    return flowLocationMap.get(key);
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

  public void addMessageFlow(MessageFlow messageFlow) {
    if (messageFlow != null && isNotEmpty(messageFlow.getId())) {
      messageFlowMap.put(messageFlow.getId(), messageFlow);
    }
  }

  public MessageFlow getMessageFlow(String id) {
    return messageFlowMap.get(id);
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
    if (message != null && isNotEmpty(message.getId())) {
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
    if (isNotEmpty(errorRef)) {
      errorMap.put(errorRef, new Error(errorRef, errorName, errorCode));
    }
  }

  public boolean containsErrorRef(String errorRef) {
    return errorMap.containsKey(errorRef);
  }

  public Map<String, ItemDefinition> getItemDefinitions() {
    return itemDefinitionMap;
  }

  public void addItemDefinition(String id, ItemDefinition item) {
    if (isNotEmpty(id)) {
      itemDefinitionMap.put(id, item);
    }
  }

  public Map<String, DataStore> getDataStores() {
    return dataStoreMap;
  }

  public DataStore getDataStore(String id) {
    DataStore dataStore = null;
    if (dataStoreMap.containsKey(id)) {
      dataStore = dataStoreMap.get(id);
    }
    return dataStore;
  }

  public void addDataStore(String id, DataStore dataStore) {
    if (isNotEmpty(id)) {
      dataStoreMap.put(id, dataStore);
    }
  }

  public List<Pool> getPools() {
    return pools;
  }

  public List<Import> getImports() {
    return imports;
  }

  public List<Interface> getInterfaces() {
    return interfaces;
  }

  public List<Artifact> getGlobalArtifacts() {
    return globalArtifacts;
  }

  public void addNamespace(String prefix, String uri) {
    namespaceMap.put(prefix, uri);
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
    var initialFlowElement = getProcessById(processId).getInitialFlowElement();

    if (initialFlowElement instanceof StartEvent startEvent) {
      return startEvent.getFormKey();
    }

    return null;
  }
}
