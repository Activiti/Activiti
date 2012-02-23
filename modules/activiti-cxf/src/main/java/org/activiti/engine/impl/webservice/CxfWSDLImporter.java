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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import org.activiti.engine.impl.bpmn.data.SimpleStructureDefinition;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;

/**
 * @author Esteban Robles Luna
 */
public class CxfWSDLImporter implements XMLImporter {

  protected Map<String, WSService> wsServices = new HashMap<String, WSService>();
  protected Map<String, WSOperation> wsOperations = new HashMap<String, WSOperation>();
  protected Map<String, StructureDefinition> structures = new HashMap<String, StructureDefinition>();

  protected String wsdlLocation;
  protected String namespace;

  public CxfWSDLImporter() {
    this.namespace = "";
  }
  
  public void importFrom(Element element, BpmnParse parse) {
    this.namespace = element.attribute("namespace") == null ? "" : element.attribute("namespace") + ":";
    this.importFrom(element.attribute("location"));
    this.transferImportsToParse(parse);
  }
  
  private void transferImportsToParse(BpmnParse parse) {
    if (parse != null) {
      for (StructureDefinition structure : this.structures.values()) {
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

  public void importFrom(String url) {
    this.wsServices.clear();
    this.wsOperations.clear();
    this.structures.clear();

    this.wsdlLocation = url;

    try {
      Bus bus = BusFactory.getDefaultBus();
      WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);
      Definition def = wsdlManager.getDefinition(url);
      WSDLServiceBuilder builder = new WSDLServiceBuilder(bus);
      List<ServiceInfo> services = builder.buildServices(def);
      
      for (ServiceInfo service : services) {
        WSService wsService = this.importService(service);
        this.wsServices.put(this.namespace + wsService.getName(), wsService);
      }
      
      this.importTypes(def.getTypes());
    } catch (WSDLException e) {
      e.printStackTrace();
    }
  }
  
  private WSService importService(ServiceInfo service) {
    String name = service.getName().getLocalPart();
    String location = "";
    
    for (EndpointInfo endpoint : service.getEndpoints()) {
      location = endpoint.getAddress();
    }
    
    WSService wsService = new WSService(this.namespace + name, location, this.wsdlLocation);
    for (OperationInfo operation : service.getInterface().getOperations()) {
      WSOperation wsOperation = this.importOperation(operation, wsService);
      wsService.addOperation(wsOperation);

      this.wsOperations.put(this.namespace + operation.getName().getLocalPart(), wsOperation);
    }
    return wsService;
  }

  private WSOperation importOperation(OperationInfo operation, WSService service) {
    WSOperation wsOperation = new WSOperation(this.namespace + operation.getName().getLocalPart(), operation.getName().getLocalPart(), service);
    return wsOperation;
  }
  
  private void importTypes(Types types) {
    SchemaCompiler compiler = XJC.createSchemaCompiler();
    ErrorListener elForRun = new ConsoleErrorReporter();
    compiler.setErrorListener(elForRun);

    SchemaImpl impl = (SchemaImpl) types.getExtensibilityElements().get(0);
    
    S2JJAXBModel intermediateModel = this.compileModel(types, compiler, impl.getElement());
    Collection<? extends Mapping> mappings = intermediateModel.getMappings();

    for (Mapping mapping : mappings){
      this.importStructure(mapping);
    }
  }
  
  private void importStructure(Mapping mapping) {
    QName qname = mapping.getElement();
    JDefinedClass theClass = (JDefinedClass) mapping.getType().getTypeClass();
    SimpleStructureDefinition structure = new SimpleStructureDefinition(this.namespace + qname.getLocalPart());
    this.structures.put(structure.getId(), structure);
    
    Map<String, JFieldVar> fields = theClass.fields();
    int index = 0;
    for (Entry<String, JFieldVar> entry : fields.entrySet()) {
      Class<?> fieldClass = ReflectUtil.loadClass(entry.getValue().type().boxify().fullName());
      structure.setFieldName(index, entry.getKey(), fieldClass);
      index++;
    }
  }
  
  private S2JJAXBModel compileModel(Types types, SchemaCompiler compiler, org.w3c.dom.Element rootTypes) {
    Schema schema = (Schema) types.getExtensibilityElements().get(0);
    compiler.parseSchema(schema.getDocumentBaseURI() + "#types1", rootTypes);
    S2JJAXBModel intermediateModel = compiler.bind();
    return intermediateModel;
  }


  public Collection<StructureDefinition> getStructures() {
    return this.structures.values();
  }

  public Collection<WSService> getServices() {
    return this.wsServices.values();
  }

  public Collection<WSOperation> getOperations() {
    return this.wsOperations.values();
  }
}
