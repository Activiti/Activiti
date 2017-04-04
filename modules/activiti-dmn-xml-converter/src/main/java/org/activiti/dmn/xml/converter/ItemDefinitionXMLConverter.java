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
package org.activiti.dmn.xml.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.DmnElement;
import org.activiti.dmn.model.ItemDefinition;

/**
 * @author Yvo Swillens
 */
public class ItemDefinitionXMLConverter extends BaseDmnXMLConverter {

    public Class<? extends DmnElement> getDmnElementType() {
        return ItemDefinition.class;
    }

    @Override
    protected String getXMLElementName() {
        return ELEMENT_ITEM_DEFINITION;
    }

    @Override
    protected DmnElement convertXMLToElement(XMLStreamReader xtr, DmnDefinition model, DecisionTable decisionTable) throws Exception {
        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
        itemDefinition.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));

        boolean readyWithItemDefinition = false;
        try {
            while (readyWithItemDefinition == false && xtr.hasNext()) {
                xtr.next();
                if (xtr.isStartElement() && ELEMENT_TYPE_DEFINITION.equalsIgnoreCase(xtr.getLocalName())) {
                    itemDefinition.setTypeDefinition(xtr.getElementText());
                }
                else if (xtr.isEndElement() && getXMLElementName().equalsIgnoreCase(xtr.getLocalName())) {
                    readyWithItemDefinition = true;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error parsing input expression", e);
        }

        return itemDefinition;
    }

    @Override
    protected void writeAdditionalAttributes(DmnElement element, DmnDefinition model, XMLStreamWriter xtw) throws Exception {

    }

    @Override
    protected void writeAdditionalChildElements(DmnElement element, DmnDefinition model, XMLStreamWriter xtw) throws Exception {

    }

}
