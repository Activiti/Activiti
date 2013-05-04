package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.MessageFlow;

public class MessageFlowXMLConverter extends BaseBpmnXMLConverter {

	public static String getXMLType() {
		return ELEMENT_MESSAGE_FLOW;
	}

	public static Class<? extends BaseElement> getBpmnElementType() {
		return MessageFlow.class;
	}

	@Override
	protected String getXMLElementName() {
		return ELEMENT_MESSAGE_FLOW;
	}

	@Override
	protected BaseElement convertXMLToElement(XMLStreamReader xtr)
			throws Exception {
		MessageFlow messageFlow = new MessageFlow();
		BpmnXMLUtil.addXMLLocation(messageFlow, xtr);
		messageFlow.setSourceRef(xtr.getAttributeValue(null,
				ATTRIBUTE_FLOW_SOURCE_REF));
		messageFlow.setTargetRef(xtr.getAttributeValue(null,
				ATTRIBUTE_FLOW_TARGET_REF));
		messageFlow.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));

		parseChildElements(getXMLElementName(), messageFlow, xtr);

		return messageFlow;
	}

	@Override
	protected void writeAdditionalAttributes(BaseElement element,
			XMLStreamWriter xtw) throws Exception {
		MessageFlow messageFlow = (MessageFlow) element;
		writeDefaultAttribute(ATTRIBUTE_FLOW_SOURCE_REF,
				messageFlow.getSourceRef(), xtw);
		writeDefaultAttribute(ATTRIBUTE_FLOW_TARGET_REF,
				messageFlow.getTargetRef(), xtw);
	}

	@Override
	protected void writeExtensionChildElements(BaseElement element,
			XMLStreamWriter xtw) throws Exception {
	}

	@Override
	protected void writeAdditionalChildElements(BaseElement element,
			XMLStreamWriter xtw) throws Exception {
	}
}
