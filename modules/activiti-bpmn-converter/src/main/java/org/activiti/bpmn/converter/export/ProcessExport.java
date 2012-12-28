package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.ActivitiListenerUtil;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang.StringUtils;

public class ProcessExport implements BpmnXMLConstants {

  public static void writeProcess(Process process, XMLStreamWriter xtw) throws Exception {
    // start process element
    xtw.writeStartElement(ELEMENT_PROCESS);
    xtw.writeAttribute(ATTRIBUTE_ID, process.getId());
    
    if (StringUtils.isNotEmpty(process.getName())) {
      xtw.writeAttribute(ATTRIBUTE_NAME, process.getName());
    }
    
    if (StringUtils.isNotEmpty(process.getTargetNamespace())) {
      xtw.writeAttribute(TARGET_NAMESPACE_ATTRIBUTE, process.getTargetNamespace());
    }
    
    xtw.writeAttribute(ATTRIBUTE_PROCESS_EXECUTABLE, ATTRIBUTE_VALUE_TRUE);
    
    if (process.getCandidateStarterUsers().size() > 0) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_USERS, 
          BpmnXMLUtil.convertToDelimitedString(process.getCandidateStarterUsers()));
    }
    
    if (process.getCandidateStarterGroups().size() > 0) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_GROUPS, 
          BpmnXMLUtil.convertToDelimitedString(process.getCandidateStarterGroups()));
    }
    
    if (StringUtils.isNotEmpty(process.getDocumentation())) {

      xtw.writeStartElement(ELEMENT_DOCUMENTATION);
      xtw.writeCharacters(process.getDocumentation());
      xtw.writeEndElement();
    }
    
    LaneExport.writeLanes(process, xtw);
    
    boolean wroteListener = ActivitiListenerUtil.writeListeners(process, false, xtw);
    if (wroteListener) {
      // closing extensions element
      xtw.writeEndElement();
    }
  }
}
