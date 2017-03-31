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
package org.activiti.dmn.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.DmnElement;
import org.activiti.dmn.model.DmnExtensionAttribute;
import org.activiti.dmn.model.DmnExtensionElement;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.OutputClause;
import org.junit.Test;

public class ExtensionElementConverterTest extends AbstractConverterTest {

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
    
    @Test
    public void convertXMLToModel() throws Exception {
        DmnDefinition definition = readXMLFile();
        validateModel(definition);
    }

    @Test
    public void convertModelToXML() throws Exception {
        DmnDefinition bpmnModel = readXMLFile();
        DmnDefinition parsedModel = exportAndReadXMLFile(bpmnModel);
        validateModel(parsedModel);
    }

    protected String getResource() {
        return "extensionElements.dmn";
    }

    private void validateModel(DmnDefinition model) {
        assertEquals("DMN description", model.getDescription());
        
        /*
         * Verify attributes extension
         */
        Map<String, String> attributes = getAttributes(model);
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
         * Verify localization extension
         */
        Localization localization = getLocalization(model);
        assertEquals("rbkfn-2", localization.getResourceBundleKeyForName());
        assertEquals("rbkfd-2", localization.getResourceBundleKeyForDescription());
        assertEquals("leifn-2", localization.getLabeledEntityIdForName());
        assertEquals("leifd-2", localization.getLabeledEntityIdForDescription());
        
        List<Decision> decisions = model.getDecisions();
        assertEquals(1, decisions.size());
        
        DecisionTable decisionTable = (DecisionTable) decisions.get(0).getExpression();
        assertNotNull(decisionTable);
        
        assertEquals("Decision table description", decisionTable.getDescription());
        
        /*
         * Verify decision table localization extension
         */
        localization = getLocalization(decisionTable);
        assertEquals("rbkfn-3", localization.getResourceBundleKeyForName());
        assertEquals("rbkfd-3", localization.getResourceBundleKeyForDescription());
        assertEquals("leifn-3", localization.getLabeledEntityIdForName());
        assertEquals("leifd-3", localization.getLabeledEntityIdForDescription());
        
        attributes = getAttributes(decisionTable);
        assertEquals(2, attributes.size());
        for (String key : attributes.keySet()) {
          if (key.equals("Attr5")) {
            assertTrue("5".equals(attributes.get(key)));
          } else if (key.equals("Attr6")) {
            assertTrue("6".equals(attributes.get(key)));
          } else {
            fail("Unknown key value");
          }
        }
        
        List<InputClause> inputClauses = decisionTable.getInputs();
        assertEquals(3, inputClauses.size());

        List<OutputClause> outputClauses = decisionTable.getOutputs();
        assertEquals(1, outputClauses.size());
        
    }
    
    protected Map<String, String> getAttributes(DmnElement bObj) {
        Map<String, String> attributes = null;

        if (null != bObj) {
          List<DmnExtensionElement> attributesExtension = bObj.getExtensionElements().get(ELEMENT_ATTRIBUTES);

          if (null != attributesExtension && !attributesExtension.isEmpty()) {
            attributes = new HashMap<String, String>();
            List<DmnExtensionElement> attributeExtensions =
                    attributesExtension.get(0).getChildElements().get(ELEMENT_ATTRIBUTE);
            
            for (DmnExtensionElement attributeExtension : attributeExtensions) {
              attributes.put(attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME),
                      attributeExtension.getAttributeValue(YOURCO_EXTENSIONS_NAMESPACE, ATTRIBUTE_VALUE));
            }
          }
        }
        return attributes;
      }
    
    protected Localization getLocalization(DmnElement bObj) {
        Localization localization = new Localization();
        List<DmnExtensionElement> i18lnExtension = bObj.getExtensionElements().get(ELEMENT_I18LN_LOCALIZATION);

        if (!i18lnExtension.isEmpty()) {
          Map<String, List<DmnExtensionAttribute>> extensionAttributes = i18lnExtension.get(0).getAttributes();
          localization.setLabeledEntityIdForName(extensionAttributes.get(ATTRIBUTE_LABELED_ENTITY_ID_FOR_NAME)
                  .get(0).getValue());
          localization.setLabeledEntityIdForDescription(extensionAttributes.get(ATTRIBUTE_LABELED_ENTITY_ID_FOR_DESCRIPTION)
                  .get(0).getValue());
          localization.setResourceBundleKeyForName(extensionAttributes.get(ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_NAME)
                  .get(0).getValue());
          localization.setResourceBundleKeyForDescription(extensionAttributes.get(ATTRIBUTE_RESOURCE_BUNDLE_KEY_FOR_DESCRIPTION)
                  .get(0).getValue());
        }
        return localization;
      }
    
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
}
