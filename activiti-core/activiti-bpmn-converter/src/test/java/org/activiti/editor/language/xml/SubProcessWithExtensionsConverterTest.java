package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.junit.Test;

/**
 * @see <a href="https://activiti.atlassian.net/browse/ACT-2055">https://activiti.atlassian.net/browse/ACT-2055</a>
 */
public class SubProcessWithExtensionsConverterTest extends AbstractConverterTest {

  protected static final String YOURCO_EXTENSIONS_NAMESPACE = "http://yourco/bpmn";
  protected static final String YOURCO_EXTENSIONS_PREFIX = "yourco";

  protected static final String ELEMENT_ATTRIBUTES = "attributes";
  protected static final String ELEMENT_ATTRIBUTE = "attribute";
  protected static final String ATTRIBUTE_NAME = "name";
  protected static final String ATTRIBUTE_VALUE = "value";

  protected static final String ELEMENT_I18LN_LOCALIZATION = "i18ln";
  protected static final String ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_NAME = "resourceBundleKeyForName";
  protected static final String ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION = "resourceBundleKeyForDescription";
  protected static final String ATTRIBUTE_LABELED_ENTITY_ID_FOR_NAME = "labeledEntityIdForName";
  protected static final String ATTRIBUTE_LABELED_ENTITY_ID_FOR_DESCRIPTION = "labeledEntityIdForDescription";

  private Localization localization = new Localization();

  /*
   * Inner class used to hold localization DataObject extension values
   */
  public class Localization {

    private String resourceBundleKeyForName;
    private String resourceBundleKeyForDescription;
    private String labeledEntityIdForName;
    private String labeledEntityIdForDescription;

    public String getResourceBundleKeyForName() {
      return resourceBundleKeyForName;
    }

    public void setResourceBundleKeyForName(String resourceBundleKeyForName) {
      this.resourceBundleKeyForName = resourceBundleKeyForName;
    }

    public String getResourceBundleKeyForDescription() {
      return resourceBundleKeyForDescription;
    }

    public void setResourceBundleKeyForDescription(String resourceBundleKeyForDescription) {
      this.resourceBundleKeyForDescription = resourceBundleKeyForDescription;
    }

    public String getLabeledEntityIdForName() {
      return labeledEntityIdForName;
    }

    public void setLabeledEntityIdForName(String labeledEntityIdForName) {
      this.labeledEntityIdForName = labeledEntityIdForName;
    }

    public String getLabeledEntityIdForDescription() {
      return labeledEntityIdForDescription;
    }

    public void setLabeledEntityIdForDescription(String labeledEntityIdForDescription) {
      this.labeledEntityIdForDescription = labeledEntityIdForDescription;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(100);
      sb.append("Localization: [");
      sb.append("resourceBundleKeyForName=").append(resourceBundleKeyForName);
      sb.append(", resourceBundleKeyForDescription=").append(resourceBundleKeyForDescription);
      sb.append(", labeledEntityIdForName=").append(labeledEntityIdForName);
      sb.append(", labeledEntityIdForDescription=").append(labeledEntityIdForDescription);
      sb.append("]");
      return sb.toString();
    }
  }

  /*
   * End of inner classes
   */

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "subprocessmodel_with_extensions.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertTrue(subProcess.getLoopCharacteristics().isSequential());
    assertEquals("10", subProcess.getLoopCharacteristics().getLoopCardinality());
    assertEquals("${assignee == \"\"}", subProcess.getLoopCharacteristics().getCompletionCondition());
    assertTrue(subProcess.getFlowElements().size() == 5);

    /*
     * Verify Subprocess attributes extension
     */
    Map<String, String> attributes = getSubprocessAttributes(flowElement);
    assertEquals(2, attributes.size());
    for (String key : attributes.keySet()) {
      if (key.equals("Attr3")) {
        assertTrue("3".equals(attributes.get(key)));
      } else if (key.equals("Attr4")) {
        assertTrue("4".equals(attributes.get(key)));
      } else {
        fail("Unknown key value");
      }
    }

    /*
     * Verify Subprocess localization extension
     */
    localization = getLocalization(flowElement);
    assertEquals("rbkfn-2", localization.getResourceBundleKeyForName());
    assertEquals("rbkfd-2", localization.getResourceBundleKeyForDescription());
    assertEquals("leifn-2", localization.getLabeledEntityIdForName());
    assertEquals("leifd-2", localization.getLabeledEntityIdForDescription());
  }

  protected static String getExtensionValue(String key, ValuedDataObject dataObj) {
    Map<String, List<ExtensionElement>> extensionElements = dataObj.getExtensionElements();

    if (!extensionElements.isEmpty()) {
      return extensionElements.get(key).get(0).getElementText();
    }
    return null;
  }

  protected static ExtensionElement getExtensionElement(String key, ValuedDataObject dataObj) {
    Map<String, List<ExtensionElement>> extensionElements = dataObj.getExtensionElements();

    if (!extensionElements.isEmpty()) {
      return extensionElements.get(key).get(0);
    }
    return null;
  }

  protected Map<String, String> getSubprocessAttributes(BaseElement bObj) {
    Map<String, String> attributes = null;

    if (null != bObj) {
      List<ExtensionElement> attributesExtension = bObj.getExtensionElements().get(ELEMENT_ATTRIBUTES);

      if (null != attributesExtension && !attributesExtension.isEmpty()) {
        attributes = new HashMap<String, String>();
        List<ExtensionElement> attributeExtensions = attributesExtension.get(0).getChildElements().get(ELEMENT_ATTRIBUTE);

        for (ExtensionElement attributeExtension : attributeExtensions) {
          attributes.put(attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME), attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_VALUE));
        }
      }
    }
    return attributes;
  }

  protected Localization getLocalization(BaseElement bObj) {
    List<ExtensionElement> i18lnExtension = bObj.getExtensionElements().get(ELEMENT_I18LN_LOCALIZATION);

    if (!i18lnExtension.isEmpty()) {
      Map<String, List<ExtensionAttribute>> extensionAttributes = i18lnExtension.get(0).getAttributes();
      localization.setLabeledEntityIdForName(extensionAttributes.get(ATTRIBUTE_LABELED_ENTITY_ID_FOR_NAME).get(0).getValue());
      localization.setLabeledEntityIdForDescription(extensionAttributes.get(ATTRIBUTE_LABELED_ENTITY_ID_FOR_DESCRIPTION).get(0).getValue());
      localization.setResourceBundleKeyForName(extensionAttributes.get(ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_NAME).get(0).getValue());
      localization.setResourceBundleKeyForDescription(extensionAttributes.get(ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION).get(0).getValue());
    }
    return localization;
  }
}
