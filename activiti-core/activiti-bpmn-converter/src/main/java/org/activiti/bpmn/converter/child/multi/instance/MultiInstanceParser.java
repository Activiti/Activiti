/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.bpmn.converter.child.multi.instance;

import static java.util.Arrays.asList;

import java.util.List;
import javax.xml.stream.XMLStreamReader;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.child.ElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;

public class MultiInstanceParser extends BaseChildElementParser {

    private final List<ElementParser<MultiInstanceLoopCharacteristics>> multiInstanceElementParsers;

    public MultiInstanceParser() {
        this(asList(new LoopCardinalityParser(),
            new MultiInstanceDataInputParser(),
            new MultiInstanceInputDataItemParser(),
            new MultiInstanceCompletionConditionParser(),
            new LoopDataOutputRefParser(),
            new MultiInstanceOutputDataItemParser(),
            new MultiInstanceAttributesParser()
        ));
    }

    public MultiInstanceParser(List<ElementParser<MultiInstanceLoopCharacteristics>> multiInstanceElementParsers) {
        this.multiInstanceElementParsers = multiInstanceElementParsers;
    }

    public String getElementName() {
        return ELEMENT_MULTIINSTANCE;
    }

    public void parseChildElement(XMLStreamReader xtr,
                                  BaseElement parentElement,
                                  BpmnModel model) throws Exception {
        if (!(parentElement instanceof Activity)) {
            return;
        }
        MultiInstanceLoopCharacteristics multiInstanceDef = new MultiInstanceLoopCharacteristics();
        BpmnXMLUtil.addXMLLocation(multiInstanceDef, xtr);

        parseMultiInstanceProperties(xtr, multiInstanceDef);

        ((Activity) parentElement).setLoopCharacteristics(multiInstanceDef);
    }

    private void parseMultiInstanceProperties(XMLStreamReader xtr,
        MultiInstanceLoopCharacteristics multiInstanceDef) {
        boolean readyWithMultiInstance = false;
        try {
            do {
                ElementParser<MultiInstanceLoopCharacteristics> matchingParser = multiInstanceElementParsers
                    .stream()
                    .filter(elementParser -> elementParser.canParseCurrentElement(xtr))
                    .findFirst()
                    .orElse(null);
                if (matchingParser != null) {
                    matchingParser.setInformation(xtr, multiInstanceDef);
                }
                if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
                    readyWithMultiInstance = true;
                }
                if (xtr.hasNext()) {
                    xtr.next();
                }
            } while (!readyWithMultiInstance && xtr.hasNext());
        } catch (Exception e) {
            LOGGER.warn("Error parsing multi instance definition",
                        e);
        }
    }

}
