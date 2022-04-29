/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

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
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(StartEvent.class);
    assertThat(flowElement.getId()).isEqualTo("start1");

    // verify the main process data objects
    List<ValuedDataObject> dataObjects = model.getMainProcess().getDataObjects();
    assertThat(dataObjects).hasSize(1);

    Map<String, ValuedDataObject> objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    ValuedDataObject dataObj = objectMap.get("dObj1");
    assertThat(dataObj.getId()).isEqualTo("dObj1");
    assertThat(dataObj.getName()).isEqualTo("StringTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:string");
    assertThat(dataObj.getValue()).isInstanceOf(String.class);
    assertThat(dataObj.getValue()).isEqualTo("Testing123");

    /*
     * Verify DataObject attributes extension
     */
    Map<String, String> attributes = getDataObjectAttributes(dataObj);
    assertThat(attributes).hasSize(2);
    assertThat(attributes).containsOnlyKeys("Attr1", "Attr2");
    assertThat(attributes.get("Attr1")).isEqualTo("1");
    assertThat(attributes.get("Attr2")).isEqualTo("2");

    /*
     * Verify DataObject localization extension
     */
    Localization localization = getLocalization(dataObj);
    assertThat(localization.getResourceBundleKeyForName()).isEqualTo("rbkfn-1");
    assertThat(localization.getResourceBundleKeyForDescription()).isEqualTo("rbkfd-1");
    assertThat(localization.getLabeledEntityIdForName()).isEqualTo("leifn-1");
    assertThat(localization.getLabeledEntityIdForDescription()).isEqualTo("leifd-1");

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("subprocess1");
    SubProcess subProcess = (SubProcess) flowElement;
    assertThat(subProcess.getFlowElements()).hasSize(6);

    // verify the sub process data objects
    dataObjects = subProcess.getDataObjects();
    assertThat(dataObjects).hasSize(1);

    objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    dataObj = objectMap.get("dObj2");
    assertThat(dataObj.getId()).isEqualTo("dObj2");
    assertThat(dataObj.getName()).isEqualTo("BooleanTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:boolean");
    assertThat(dataObj.getValue()).isInstanceOf(Boolean.class);
    assertThat(dataObj.getValue()).isEqualTo(Boolean.TRUE);

    /*
     * Verify DataObject attributes extension
     */
    attributes = getDataObjectAttributes(dataObj);
    assertThat(attributes).hasSize(2);
    assertThat(attributes).containsOnlyKeys("Attr3", "Attr4");
    assertThat(attributes.get("Attr3")).isEqualTo("3");
    assertThat(attributes.get("Attr4")).isEqualTo("4");

    /*
     * Verify DataObject localization extension
     */
    localization = getLocalization(dataObj);
    assertThat(localization.getResourceBundleKeyForName()).isEqualTo("rbkfn-2");
    assertThat(localization.getResourceBundleKeyForDescription()).isEqualTo("rbkfd-2");
    assertThat(localization.getLabeledEntityIdForName()).isEqualTo("leifn-2");
    assertThat(localization.getLabeledEntityIdForDescription()).isEqualTo("leifd-2");
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
        List<ExtensionElement> attributeExtensions = attributesExtension.get(0).getChildElements().get(ELEMENT_DATA_ATTRIBUTE);

        for (ExtensionElement attributeExtension : attributeExtensions) {
          attributes.put(attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME), attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_VALUE));
        }
      }
    }
    return attributes;
  }

  protected Localization getLocalization(BaseElement dObj) {
    List<ExtensionElement> i18lnExtension = dObj.getExtensionElements().get(ELEMENT_I18LN_LOCALIZATION);

    if (!i18lnExtension.isEmpty()) {
      Map<String, List<ExtensionAttribute>> extensionAttributes = i18lnExtension.get(0).getAttributes();
      localization.setLabeledEntityIdForName(extensionAttributes.get(ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_NAME).get(0).getValue());
      localization.setLabeledEntityIdForDescription(extensionAttributes.get(ATTRIBUTE_DATA_LABELED_ENTITY_ID_FOR_DESCRIPTION).get(0).getValue());
      localization.setResourceBundleKeyForName(extensionAttributes.get(ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_NAME).get(0).getValue());
      localization.setResourceBundleKeyForDescription(extensionAttributes.get(ATTRIBUTE_DATA_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION).get(0).getValue());
    }
    return localization;
  }
}
