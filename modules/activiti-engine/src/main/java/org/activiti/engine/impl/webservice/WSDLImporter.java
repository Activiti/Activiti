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
package org.activiti.engine.impl.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.bpmn.SimpleStructure;
import org.activiti.engine.impl.bpmn.Structure;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parse;
import org.activiti.engine.impl.util.xml.Parser;

/**
 * A WSDL importer
 * 
 * @author Esteban Robles Luna
 */
public class WSDLImporter implements XMLImporter {

  private static Map<String, Class< ? >> mappings = new HashMap<String, Class< ? >>();

  static {
    mappings.put("xs:int", Integer.class);
    mappings.put("xs:string", String.class);
  };
  
  protected Map<String, WSService> wsServices = new HashMap<String, WSService>();

  protected Map<String, WSOperation> wsOperations = new HashMap<String, WSOperation>();

  protected Map<String, Structure> structures = new HashMap<String, Structure>();
  
  protected String wsdlLocation;

  public void importFrom(Element element, BpmnParse parse) {
    this.wsdlLocation = element.attribute("location");

    try {
      URL url = new URL(this.wsdlLocation);
      this.importFrom(url, parse);
    } catch (MalformedURLException e) {
      parse.addProblem("Invalid URL " + this.wsdlLocation, element);
    }
  }
  
  public void importFrom(URL url, BpmnParse parse) {
    this.wsServices.clear();
    this.wsOperations.clear();
    this.structures.clear();
    
    Parse importParse = Parser.INSTANCE.createParse();
    Element root = importParse.sourceUrl(url).execute().getRootElement();

    this.importServices(root);
    this.importOperations(root);
    this.importTypes(root);
    
    this.transferImportsToParse(parse);
  }

  private void transferImportsToParse(BpmnParse parse) {
    if (parse != null) {
      for (Structure structure : this.structures.values()) {
         parse.addStructure(structure);
      }

      for (WSService service : this.wsServices.values()) {
        parse.addService(service);
      }

      for (WSOperation operation : this.wsOperations.values()) {
        parse.addOperation(operation);
      }
    }
  }

  private void importServices(Element root) {
    List<Element> serviceElements = root.elements("wsdl:service");
    for (Element serviceElement : serviceElements) {
      Element addressElement = serviceElement.element("wsdl:port").element("soap:address");
      String location = addressElement.attribute("location");
      String name = serviceElement.attribute("name");
      WSService service = new WSService(name, location, this.wsdlLocation);
      this.wsServices.put(name, service);
    }
  }

  private void importOperations(Element root) {
    List<Element> portElements = root.elements("wsdl:portType");
    for (Element portElement : portElements) {
      String serviceName = portElement.attribute("name");
      WSService service = this.wsServices.get(serviceName);
      List<Element> operationElements = portElement.elements("wsdl:operation");
      for (Element operationElement : operationElements) {
        String operationName = operationElement.attribute("name");
        WSOperation operation = new WSOperation(operationName, service);

        service.addOperation(operation);
        this.wsOperations.put(operationName, operation);
      }
    }
  }

  private void importTypes(Element root) {
    List<Element> typeElements = root
      .element("wsdl:types")
      .element("xs:schema")
      .elements("xs:complexType");

    for (Element typeElement : typeElements) {
      Structure structure = this.createStructureFrom(typeElement);
      this.structures.put(structure.getId(), structure);
    }
  }

  private Structure createStructureFrom(Element element) {
    String id = element.attribute("name");
    SimpleStructure structure = new SimpleStructure(id);

    List<Element> arguments = element
      .element("xs:sequence")
      .elements("xs:element");

    int index = 0;
    for (Element argument : arguments) {
      String fieldName = argument.attribute("name");
      Class< ? > type = mappings.get(argument.attribute("type"));
      structure.setFieldName(index, fieldName, type);
      index++;
    }

    return structure;
  }

  public Collection<Structure> getStructures() {
    return this.structures.values();
  }
}
