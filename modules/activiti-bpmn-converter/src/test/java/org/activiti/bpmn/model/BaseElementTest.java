package org.activiti.bpmn.model;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by francisco on 30/04/17.
 */
public class BaseElementTest {

    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String PREFIX = "prefix";

    private ExtensionAttribute extensionAttributeOne;
    private ExtensionAttribute extensionAttributeTwo;
    private ActivitiElement activitiElement;

    @Before
    public void setUp() {
        extensionAttributeOne = createExtensionAttribute("nameOne", "valueOne");
        extensionAttributeTwo = createExtensionAttribute(PREFIX, "nameTwo", "valueTwo");

        ExtensionElement extensionElement = createDefaultExtensionElement();
        extensionElement.addAttribute(extensionAttributeOne);

        activitiElement = new ActivitiElement();
        activitiElement.addExtensionElement(extensionElement);
        activitiElement.addAttribute(extensionAttributeTwo);
    }

    @Test
    public void testBaseElementCreation() {
        TestCase.assertEquals("nameOne=valueOne", extensionAttributeOne.toString());
        TestCase.assertEquals(PREFIX + ":nameTwo=valueTwo", extensionAttributeTwo.toString());

        TestCase.assertEquals(activitiElement.getId(), ActivitiElement.ACTIVITI_ELEMENT_ID);

        TestCase.assertEquals(activitiElement.getExtensionElements().size(), 1);
        TestCase.assertEquals(activitiElement.getAttributes().size(), 1);
        TestCase.assertEquals(
                activitiElement.getExtensionElements().get(NAME).get(0).getAttributes().get("nameOne").get(0),
                extensionAttributeOne
        );

        TestCase.assertEquals(activitiElement.getAttributes().get("nameTwo").get(0).getValue(), "valueTwo");
    }

    @Test
    public void testCloneBaseElement() {

        ActivitiElement activitiElementCloned = activitiElement.clone();
        TestCase.assertEquals(activitiElementCloned.getId(), ActivitiElement.ACTIVITI_ELEMENT_ID);
        TestCase.assertEquals(activitiElementCloned.getExtensionElements().size(), 1);
        TestCase.assertEquals(activitiElementCloned.getAttributes().size(), 1);

        ExtensionAttribute extensionAttributeOneFromCloned =
                activitiElementCloned.getExtensionElements().get(NAME).get(0).getAttributes().get("nameOne").get(0);

        ExtensionAttribute extensionAttributeTwoFromCloned = activitiElementCloned.getAttributes().get("nameTwo").get(0);

        TestCase.assertNotSame(activitiElement, activitiElementCloned);
        //cloned references should be different objects
        TestCase.assertFalse(extensionAttributeOneFromCloned==extensionAttributeOne);
        TestCase.assertFalse(extensionAttributeTwoFromCloned == extensionAttributeTwo);

        TestCase.assertEquals(extensionAttributeTwoFromCloned.getValue(), "valueTwo");

    }

    private ExtensionAttribute createExtensionAttribute(String name, String value) {
        return this.createExtensionAttribute(null, name, value);
    }

    private ExtensionAttribute createExtensionAttribute(String prefix, String name, String value) {
        ExtensionAttribute extensionAttribute = new ExtensionAttribute(name);
        extensionAttribute.setValue(value);
        if (prefix != null) {
            extensionAttribute.setNamespacePrefix(PREFIX);
        }
        return extensionAttribute;
    }

    private ExtensionElement createDefaultExtensionElement() {
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setName(NAME);
        extensionElement.setNamespace(NAMESPACE);
        extensionElement.setNamespacePrefix(PREFIX);
        return extensionElement;
    }


    private class ActivitiElement extends BaseElement {
        public static final String ACTIVITI_ELEMENT_ID = "ActivitiElementId";

        public ActivitiElement() {
            this.setId(ACTIVITI_ELEMENT_ID);
        }

        @Override
        public ActivitiElement clone() {
            ActivitiElement activitiElement = new ActivitiElement();
            activitiElement.setValues(this);
            return activitiElement;
        }
    }

}