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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.activiti.dmn.converter.util.DmnXMLUtil;
import org.activiti.dmn.converter.util.InputStreamProvider;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionRule;
import org.activiti.dmn.model.DecisionTable;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.model.DmnElement;
import org.activiti.dmn.model.DmnExtensionElement;
import org.activiti.dmn.model.HitPolicy;
import org.activiti.dmn.model.InputClause;
import org.activiti.dmn.model.ItemDefinition;
import org.activiti.dmn.model.OutputClause;
import org.activiti.dmn.model.RuleInputClauseContainer;
import org.activiti.dmn.model.RuleOutputClauseContainer;
import org.activiti.dmn.xml.constants.DmnXMLConstants;
import org.activiti.dmn.xml.exception.DmnXMLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Tijs Rademakers
 * @author Yvo Swillens
 * @author Bassam Al-Sarori
 */
public class DmnXMLConverter implements DmnXMLConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DmnXMLConverter.class);

    protected static final String DMN_XSD = "org/activiti/impl/dmn/parser/dmn.xsd";
    protected static final String DEFAULT_ENCODING = "UTF-8";

    protected static Map<String, BaseDmnXMLConverter> convertersToDmnMap = new HashMap<String, BaseDmnXMLConverter>();
    protected static Map<Class<? extends DmnElement>, BaseDmnXMLConverter> convertersToXMLMap = 
        new HashMap<Class<? extends DmnElement>, BaseDmnXMLConverter>();

    protected ClassLoader classloader;

    static {
        addConverter(new InputClauseXMLConverter());
        addConverter(new DecisionRuleXMLConverter());
    }

    public static void addConverter(BaseDmnXMLConverter converter) {
        addConverter(converter, converter.getDmnElementType());
    }

    public static void addConverter(BaseDmnXMLConverter converter, Class<? extends DmnElement> elementType) {
        convertersToDmnMap.put(converter.getXMLElementName(), converter);
        convertersToXMLMap.put(elementType, converter);
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public void validateModel(InputStreamProvider inputStreamProvider) throws Exception {
        Schema schema = createSchema();

        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(inputStreamProvider.getInputStream()));
    }

    public void validateModel(XMLStreamReader xmlStreamReader) throws Exception {
        Schema schema = createSchema();

        Validator validator = schema.newValidator();
        validator.validate(new StAXSource(xmlStreamReader));
    }

    protected Schema createSchema() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        if (classloader != null) {
            schema = factory.newSchema(classloader.getResource(DMN_XSD));
        }

        if (schema == null) {
            schema = factory.newSchema(DmnXMLConverter.class.getClassLoader().getResource(DMN_XSD));
        }

        if (schema == null) {
            throw new DmnXMLException("DMN XSD could not be found");
        }
        return schema;
    }

    public DmnDefinition convertToDmnModel(InputStreamProvider inputStreamProvider, boolean validateSchema, boolean enableSafeBpmnXml) {
        return convertToDmnModel(inputStreamProvider, validateSchema, enableSafeBpmnXml, DEFAULT_ENCODING);
    }

    public DmnDefinition convertToDmnModel(InputStreamProvider inputStreamProvider, boolean validateSchema, boolean enableSafeBpmnXml, String encoding) {
        XMLInputFactory xif = XMLInputFactory.newInstance();

        if (xif.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
            xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.SUPPORT_DTD)) {
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        }

        InputStreamReader in = null;
        try {
            in = new InputStreamReader(inputStreamProvider.getInputStream(), encoding);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);

            try {
                if (validateSchema) {

                    if (!enableSafeBpmnXml) {
                        validateModel(inputStreamProvider);
                    } else {
                        validateModel(xtr);
                    }

                    // The input stream is closed after schema validation
                    in = new InputStreamReader(inputStreamProvider.getInputStream(), encoding);
                    xtr = xif.createXMLStreamReader(in);
                }

            } catch (Exception e) {
                throw new DmnXMLException(e.getMessage(), e);
            }

            // XML conversion
            return convertToDmnModel(xtr);
            
        } catch (UnsupportedEncodingException e) {
            throw new DmnXMLException("The dmn xml is not UTF8 encoded", e);
        } catch (XMLStreamException e) {
            throw new DmnXMLException("Error while reading the BPMN 2.0 XML", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.debug("Problem closing DMN input stream", e);
                }
            }
        }
    }

    public DmnDefinition convertToDmnModel(XMLStreamReader xtr) {
        DmnDefinition model = new DmnDefinition();
        DmnElement parentElement = null;
        DecisionTable currentDecisionTable = null;
        
        try {
            while (xtr.hasNext()) {
                try {
                    xtr.next();
                } catch (Exception e) {
                    LOGGER.debug("Error reading XML document", e);
                    throw new DmnXMLException("Error reading XML", e);
                }

                if (xtr.isStartElement() == false) {
                    continue;
                }

                if (ELEMENT_DEFINITIONS.equals(xtr.getLocalName())) {
                    model.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
                    model.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
                    model.setNamespace(MODEL_NAMESPACE);
                    parentElement = model;
                } else if (ELEMENT_DECISION.equals(xtr.getLocalName())) {
                    Decision decision = new Decision();
                    model.addDecision(decision);
                    decision.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
                    decision.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
                    parentElement = decision;
                } else if (ELEMENT_DECISION_TABLE.equals(xtr.getLocalName())) {
                    currentDecisionTable = new DecisionTable();
                    currentDecisionTable.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));

                    if (xtr.getAttributeValue(null, ATTRIBUTE_HIT_POLICY) != null) {
                        currentDecisionTable.setHitPolicy(HitPolicy.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_HIT_POLICY)));
                    } else {
                        currentDecisionTable.setHitPolicy(HitPolicy.FIRST);
                    }

                    model.getDecisions().get(model.getDecisions().size() - 1).setExpression(currentDecisionTable);
                    parentElement = currentDecisionTable;
                } else if (ELEMENT_OUTPUT_CLAUSE.equals(xtr.getLocalName())) {
                    OutputClause outputClause = new OutputClause();
                    currentDecisionTable.addOutput(outputClause);
                    outputClause.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
                    outputClause.setLabel(xtr.getAttributeValue(null, ATTRIBUTE_LABEL));
                    outputClause.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
                    outputClause.setTypeRef(xtr.getAttributeValue(null, ATTRIBUTE_TYPE_REF));
                    parentElement = outputClause;
                } else if (ELEMENT_DESCRIPTION.equals(xtr.getLocalName())) {
                    parentElement.setDescription(xtr.getElementText());
                } else if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
                    while (xtr.hasNext()) {
                        xtr.next();
                        if (xtr.isStartElement()) {
                            DmnExtensionElement extensionElement = DmnXMLUtil.parseExtensionElement(xtr);
                            parentElement.addExtensionElement(extensionElement);
                        } else if (xtr.isEndElement()) {
                            if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
                                break;
                            }
                        }
                    }

                } else if (convertersToDmnMap.containsKey(xtr.getLocalName())) {
                    BaseDmnXMLConverter converter = convertersToDmnMap.get(xtr.getLocalName());
                    converter.convertToDmnModel(xtr, model, currentDecisionTable);
                }
            }
            
        } catch (DmnXMLException e) {
            throw e;

        } catch (Exception e) {
            LOGGER.error("Error processing DMN document", e);
            throw new DmnXMLException("Error processing DMN document", e);
        }
        return model;
    }


    public byte[] convertToXML(DmnDefinition model) {
        return convertToXML(model, DEFAULT_ENCODING);
    }

    public byte[] convertToXML(DmnDefinition model, String encoding) {
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            OutputStreamWriter out = new OutputStreamWriter(outputStream, encoding);

            XMLStreamWriter writer = xof.createXMLStreamWriter(out);
            XMLStreamWriter xtw = new IndentingXMLStreamWriter(writer);

            xtw.writeStartElement(ELEMENT_DEFINITIONS);
            xtw.writeDefaultNamespace(DMN_NAMESPACE);
            xtw.writeAttribute(ATTRIBUTE_ID, model.getId());
            if (StringUtils.isNotEmpty(model.getName())) {
                xtw.writeAttribute(ATTRIBUTE_NAME, model.getName());
            }
            xtw.writeAttribute(ATTRIBUTE_NAMESPACE, MODEL_NAMESPACE);
            
            DmnXMLUtil.writeElementDescription(model, xtw);
            DmnXMLUtil.writeExtensionElements(model, xtw);

            for (ItemDefinition itemDefinition : model.getItemDefinitions()) {
                xtw.writeStartElement(ELEMENT_ITEM_DEFINITION);
                xtw.writeAttribute(ATTRIBUTE_ID, itemDefinition.getId());
                if (StringUtils.isNotEmpty(itemDefinition.getName())) {
                    xtw.writeAttribute(ATTRIBUTE_NAME, itemDefinition.getName());
                }
                
                DmnXMLUtil.writeElementDescription(itemDefinition, xtw);
                DmnXMLUtil.writeExtensionElements(itemDefinition, xtw);

                xtw.writeStartElement(ELEMENT_TYPE_DEFINITION);
                xtw.writeCharacters(itemDefinition.getTypeDefinition());
                xtw.writeEndElement();

                xtw.writeEndElement();
            }
            
            for (Decision decision : model.getDecisions()) {
                xtw.writeStartElement(ELEMENT_DECISION);
                xtw.writeAttribute(ATTRIBUTE_ID, decision.getId());
                if (StringUtils.isNotEmpty(decision.getName())) {
                    xtw.writeAttribute(ATTRIBUTE_NAME, decision.getName());
                }
                
                DmnXMLUtil.writeElementDescription(decision, xtw);
                DmnXMLUtil.writeExtensionElements(decision, xtw);
                
                DecisionTable decisionTable = (DecisionTable) decision.getExpression();
                xtw.writeStartElement(ELEMENT_DECISION_TABLE);
                xtw.writeAttribute(ATTRIBUTE_ID, decisionTable.getId());

                if (decisionTable.getHitPolicy() != null) {
                    xtw.writeAttribute(ATTRIBUTE_HIT_POLICY, decisionTable.getHitPolicy().toString());
                }
                
                DmnXMLUtil.writeElementDescription(decisionTable, xtw);
                DmnXMLUtil.writeExtensionElements(decisionTable, xtw);
                
                for (InputClause clause : decisionTable.getInputs()) {
                    xtw.writeStartElement(ELEMENT_INPUT_CLAUSE);
                    if (StringUtils.isNotEmpty(clause.getId())) {
                        xtw.writeAttribute(ATTRIBUTE_ID, clause.getId());
                    }
                    if (StringUtils.isNotEmpty(clause.getLabel())) {
                        xtw.writeAttribute(ATTRIBUTE_LABEL, clause.getLabel());
                    }

                    DmnXMLUtil.writeElementDescription(clause, xtw);
                    DmnXMLUtil.writeExtensionElements(clause, xtw);
                    
                    if (clause.getInputExpression() != null) {
                        xtw.writeStartElement(ELEMENT_INPUT_EXPRESSION);
                        xtw.writeAttribute(ATTRIBUTE_ID, clause.getInputExpression().getId());

                        if (StringUtils.isNotEmpty(clause.getInputExpression().getTypeRef())) {
                            xtw.writeAttribute(ATTRIBUTE_TYPE_REF, clause.getInputExpression().getTypeRef());
                        }

                        if (StringUtils.isNotEmpty(clause.getInputExpression().getText())) {
                            xtw.writeStartElement(ELEMENT_TEXT);
                            xtw.writeCharacters(clause.getInputExpression().getText());
                            xtw.writeEndElement();
                        }

                        xtw.writeEndElement();
                    }

                    xtw.writeEndElement();
                }


                for (OutputClause clause : decisionTable.getOutputs()) {
                    xtw.writeStartElement(ELEMENT_OUTPUT_CLAUSE);
                    if (StringUtils.isNotEmpty(clause.getId())) {
                        xtw.writeAttribute(ATTRIBUTE_ID, clause.getId());
                    }
                    if (StringUtils.isNotEmpty(clause.getLabel())) {
                        xtw.writeAttribute(ATTRIBUTE_LABEL, clause.getLabel());
                    }
                    if (StringUtils.isNotEmpty(clause.getName())) {
                        xtw.writeAttribute(ATTRIBUTE_NAME, clause.getName());
                    }
                    if (StringUtils.isNotEmpty(clause.getTypeRef())) {
                        xtw.writeAttribute(ATTRIBUTE_TYPE_REF, clause.getTypeRef());
                    }
                    
                    DmnXMLUtil.writeElementDescription(clause, xtw);
                    DmnXMLUtil.writeExtensionElements(clause, xtw);

                    xtw.writeEndElement();
                }

                for (DecisionRule rule : decisionTable.getRules()) {
                    xtw.writeStartElement(ELEMENT_RULE);
                    if (StringUtils.isNotEmpty(rule.getId())) {
                        xtw.writeAttribute(ATTRIBUTE_ID, rule.getId());
                    }
                    
                    DmnXMLUtil.writeElementDescription(rule, xtw);
                    DmnXMLUtil.writeExtensionElements(rule, xtw);

                    for (RuleInputClauseContainer container : rule.getInputEntries()) {
                        xtw.writeStartElement(ELEMENT_INPUT_ENTRY);
                        xtw.writeAttribute(ATTRIBUTE_ID, container.getInputEntry().getId());

                        xtw.writeStartElement(ELEMENT_TEXT);
                        xtw.writeCharacters(container.getInputEntry().getText());
                        xtw.writeEndElement();

                        xtw.writeEndElement();
                    }

                    for (RuleOutputClauseContainer container : rule.getOutputEntries()) {
                        xtw.writeStartElement(ELEMENT_OUTPUT_ENTRY);
                        xtw.writeAttribute(ATTRIBUTE_ID, container.getOutputEntry().getId());

                        xtw.writeStartElement(ELEMENT_TEXT);
                        xtw.writeCharacters(container.getOutputEntry().getText());
                        xtw.writeEndElement();

                        xtw.writeEndElement();
                    }

                    xtw.writeEndElement();
                }
                
                xtw.writeEndElement();
                
                xtw.writeEndElement();
            }

            // end definitions root element
            xtw.writeEndElement();
            xtw.writeEndDocument();

            xtw.flush();

            outputStream.close();

            xtw.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            LOGGER.error("Error writing BPMN XML", e);
            throw new DmnXMLException("Error writing BPMN XML", e);
        }
    }

}
