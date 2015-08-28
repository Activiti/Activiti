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
 * @see <a href="https://activiti.atlassian.net/browse/ACT-1847">https://activiti.atlassian.net/browse/ACT-1847</a>
 */
public class ValuedDataObjectWithExtensionsConverterTest extends AbstractConverterTest {
  
  protected static final String YOURCO_EXTENSIONS_NAMESPACE = "http://yourco/bpmn";
  protected static final String YOURCO_EXTENSIONS_PREFIX = "yourco";

  protected static final String ELEMENT_DATA_ATTRIBUTES = "attributes";
  protected static final String ELEMENT_DATA_ATTRIBUTE = "attribute";
  protected static final String ATTRIBUTE_NAME = "name";
  protected static final String ATTRIBUTE_VALUE = "value";

  protected static final String ELEMENT_I18LN_LOCALIZATION = "i18ln";
  protected static final String ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_NAME = "resourceBundleKeyForName";
  protected static final String ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION = "resourceBundleKeyForDescription";
  protected static final String ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_NAME = "labeledEntityIdForName";
  protected static final String ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_DESCRIPTION = "labeledEntityIdForDescription";

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
    return "valueddataobjectmodel_with_extensions.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());

    // verify the main process data objects
    List<ValuedDataObject> dataObjects = model.getMainProcess().getDataObjects();
    assertEquals(1, dataObjects.size());

    Map<String, ValuedDataObject> objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    ValuedDataObject dataObj = objectMap.get("dObj1");
    assertEquals("dObj1", dataObj.getId());
    assertEquals("StringTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof String);
    assertEquals("Testing123", dataObj.getValue());
    
    /*
     * Verify DataObject attributes extension
     */
    Map<String, String> attributes = getDataObjectAttributes(dataObj);
    assertEquals(2, attributes.size());
    for (String key : attributes.keySet()) {
      if (key.equals("Attr1")) {
        assertTrue("1".equals(attributes.get(key)));
      } else if (key.equals("Attr2")) {
        assertTrue("2".equals(attributes.get(key)));
      } else {
        fail("Unknown key value");
      }
    }
    
    /*
     * Verify DataObject localization extension
     */
    Localization localization = getLocalization(dataObj);
    assertEquals("rbkfn-1", localization.getResourceBundleKeyForName());
    assertEquals("rbkfd-1", localization.getResourceBundleKeyForDescription());
    assertEquals("leifn-1", localization.getLabeledEntityIdForName());
    assertEquals("leifd-1", localization.getLabeledEntityIdForDescription());

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertEquals(6, subProcess.getFlowElements().size());

    // verify the sub process data objects
    dataObjects = subProcess.getDataObjects();
    assertEquals(1, dataObjects.size());

    objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    dataObj = objectMap.get("dObj2");
    assertEquals("dObj2", dataObj.getId());
    assertEquals("BooleanTest", dataObj.getName());
    assertEquals("xsd:boolean", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Boolean);
    assertEquals(Boolean.TRUE, dataObj.getValue());

    /*
     * Verify DataObject attributes extension
     */
    attributes = getDataObjectAttributes(dataObj);
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
     * Verify DataObject localization extension
     */
    localization = getLocalization(dataObj);
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

  protected Map<String, String> getDataObjectAttributes(BaseElement dObj) {
    Map<String, String> attributes = null;

    if (null != dObj) {
      List<ExtensionElement> attributesExtension = dObj.getExtensionElements().get(ELEMENT_DATA_ATTRIBUTES);

      if (null != attributesExtension && !attributesExtension.isEmpty()) {
        attributes = new HashMap<String, String>();
        List<ExtensionElement> attributeExtensions =
                attributesExtension.get(0).getChildElements().get(ELEMENT_DATA_ATTRIBUTE);
        
        for (ExtensionElement attributeExtension : attributeExtensions) {
          attributes.put(attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME),
                  attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_VALUE));
        }
      }
    }
    return attributes;
  }

  protected Localization getLocalization(BaseElement dObj) {
    List<ExtensionElement> i18lnExtension = dObj.getExtensionElements().get(ELEMENT_I18LN_LOCALIZATION);

    if (!i18lnExtension.isEmpty()) {
      Map<String, List<ExtensionAttribute>> extensionAttributes = i18lnExtension.get(0).getAttributes();
      localization.setLabeledEntityIdForName(extensionAttributes.get(ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_NAME)
              .get(0).getValue());
      localization.setLabeledEntityIdForDescription(extensionAttributes.get(ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_DESCRIPTION)
              .get(0).getValue());
      localization.setResourceBundleKeyForName(extensionAttributes.get(ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_NAME)
              .get(0).getValue());
      localization.setResourceBundleKeyForDescription(extensionAttributes.get(ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION)
              .get(0).getValue());
    }
    return localization;
  }
}
