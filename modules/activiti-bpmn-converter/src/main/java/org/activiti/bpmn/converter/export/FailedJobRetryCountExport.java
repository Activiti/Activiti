package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.Activity;
import org.apache.commons.lang3.StringUtils;

public class FailedJobRetryCountExport implements BpmnXMLConstants{
  public static void writeFailedJobRetryCount(Activity activity, XMLStreamWriter xtw) throws Exception {
	 String failedJobRetryCycle = activity.getFailedJobRetryTimeCycleValue();
     if (failedJobRetryCycle != null) {
        
		if (StringUtils.isNotEmpty(failedJobRetryCycle)) {
			xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, FAILED_JOB_RETRY_TIME_CYCLE, ACTIVITI_EXTENSIONS_NAMESPACE);    		  
	        xtw.writeCharacters(failedJobRetryCycle);
		    xtw.writeEndElement();
		   
		}
	 }
  }

}
