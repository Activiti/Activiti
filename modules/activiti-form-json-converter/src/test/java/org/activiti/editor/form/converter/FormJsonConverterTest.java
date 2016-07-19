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
package org.activiti.editor.form.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.activiti.form.model.FormDefinition;
import org.activiti.form.model.FormField;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class FormJsonConverterTest {

    private static final Logger logger = LoggerFactory.getLogger(FormJsonConverterTest.class);

    private static final String JSON_RESOURCE_1 = "org/activiti/editor/form/converter/form_1.json";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSimpleJsonForm() throws Exception {

        String testJsonResource = readJsonToString(JSON_RESOURCE_1);
        FormDefinition formDefinition = new FormJsonConverter().convertToForm(testJsonResource, "11", 1);

        assertNotNull(formDefinition);
        assertEquals("11", formDefinition.getId());
        assertEquals("form1", formDefinition.getKey());
        assertEquals("My first form", formDefinition.getName());

        assertNotNull(formDefinition.getFields());
        assertEquals(1, formDefinition.getFields().size());

        FormField formField = formDefinition.getFields().get(0);
        assertEquals("input1", formField.getId());
        assertEquals("Input1", formField.getName());
        assertEquals("text", formField.getType());
        assertEquals(false, formField.isRequired());
        assertEquals("empty", formField.getPlaceholder());
    }

    /* Helper methods */
    protected String readJsonToString(String resource) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(resource);
            return IOUtils.toString(is);
        } catch (IOException e) {
            fail("Could not read " + resource + " : " + e.getMessage());
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected JsonNode parseJson(String resource) {
        String jsonString = readJsonToString(resource);
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            fail("Could not parse " + resource + " : " + e.getMessage());
        }
        return null;
    }
}