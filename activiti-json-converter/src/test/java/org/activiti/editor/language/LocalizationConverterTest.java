package org.activiti.editor.language;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocalizationConverterTest extends AbstractConverterTest {

    @Test
    public void convertJsonToModel() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        validateModel(bpmnModel);
    }

    @Test
    public void doubleConversionValidation() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        bpmnModel = convertToJsonAndBack(bpmnModel);
        validateModel(bpmnModel);
    }

    protected String getResource() {
        return "test.usertaskmodel.json";
    }

    private void validateModel(BpmnModel model) {
        final UserTask userTask = model.getMainProcess().findFlowElementsOfType(UserTask.class).get(0);
        final List<ExtensionElement> localization = userTask.getExtensionElements().get("localization");
        assertNotNull(localization);
        for (ExtensionElement extensionElement : localization) {
            final String locale = extensionElement.getAttributeValue(null,
                                                                     "locale");
            final String name = extensionElement.getAttributeValue(null,
                                                                   "name");
            final List<ExtensionElement> docs = extensionElement.getChildElements().get("documentation");
            assertNotNull(name);
            assertNotNull(docs);
            assertEquals(docs.size(),
                         1);
            if (locale.equals("fa")) {
                assertEquals(name,
                             "ثبت نام");
            }
            if (locale.equals("en")) {
                assertEquals(name,
                             "registration");
            }
            if (locale.equals("fa")) {
                assertEquals(docs.get(0).getElementText(),
                             "توضیحات ثبت نام");
            }
            if (locale.equals("en")) {
                assertEquals(docs.get(0).getElementText(),
                             "registration description");
            }
        }
    }
}
