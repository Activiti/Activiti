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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import org.activiti.bpmn.model.Import;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.data.PrimitiveStructureDefinition;
import org.activiti.engine.impl.bpmn.data.SimpleStructureDefinition;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.i18n.UncheckedException;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

/**
 * @author Esteban Robles Luna
 */
public class CxfWSDLImporter implements XMLImporter {
    
  protected static final String JAXB_BINDINGS_RESOURCE = "activiti-bindings.xjc";

  protected Map<String, WSService> wsServices = new HashMap<String, WSService>();
  protected Map<String, WSOperation> wsOperations = new HashMap<String, WSOperation>();
  protected Map<String, StructureDefinition> structures = new HashMap<String, StructureDefinition>();

  protected String wsdlLocation;
  protected String namespace;

  public CxfWSDLImporter() {
    this.namespace = "";
  }
  
  public void importFrom(Import theImport, BpmnParse parse) {
    this.namespace = theImport.getNamespace() == null ? "" : theImport.getNamespace() + ":";
    try {
      final URIResolver uriResolver = new URIResolver(parse.getSourceSystemId(), theImport.getLocation());
      if (uriResolver.isResolved()) {
          if (uriResolver.getURI() != null) {
              this.importFrom(uriResolver.getURI().toString());
          } else if (uriResolver.isFile()) {
              this.importFrom(uriResolver.getFile().getAbsolutePath());
          } else if (uriResolver.getURL() != null) {
              this.importFrom(uriResolver.getURL().toString());
          }
      } else {
          throw new UncheckedException(new Exception("Unresolved import against " + parse.getSourceSystemId()));
      }
      
      this.transferImportsToParse(parse);
      
    } catch (final IOException e) {
      throw new UncheckedException(e);
    }
  }
  
  protected void transferImportsToParse(BpmnParse parse) {
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
      final Enumeration<URL> xjcBindingUrls = Thread.currentThread().getContextClassLoader().getResources(JAXB_BINDINGS_RESOURCE);
      if (xjcBindingUrls.hasMoreElements()) {
          final URL xjcBindingUrl = xjcBindingUrls.nextElement(); 
          if (xjcBindingUrls.hasMoreElements()) {
              throw new ActivitiException("Several JAXB binding definitions found for activiti-cxf: " + JAXB_BINDINGS_RESOURCE);
          }
          DynamicClientFactory.newInstance(bus).createClient(url, Arrays.asList(new String[] { xjcBindingUrl.toString() }));
          WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);  
          Definition def = wsdlManager.getDefinition(url);
          WSDLServiceBuilder builder = new WSDLServiceBuilder(bus);
          List<ServiceInfo> services = builder.buildServices(def);
          
          for (ServiceInfo service : services) {
            WSService wsService = this.importService(service);
            this.wsServices.put(this.namespace + wsService.getName(), wsService);
          }
          
          if(def != null && def.getTypes() != null) {
            this.importTypes(def.getTypes());
          }
      } else {
          throw new ActivitiException("The JAXB binding definitions are not found for activiti-cxf: " + JAXB_BINDINGS_RESOURCE);
      }
    } catch (WSDLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        throw new ActivitiException("Error retrieveing the JAXB binding definitions", e);
    }
  }
  
  protected WSService importService(ServiceInfo service) {
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

  protected WSOperation importOperation(OperationInfo operation, WSService service) {
    WSOperation wsOperation = new WSOperation(this.namespace + operation.getName().getLocalPart(), operation.getName().getLocalPart(), service);
    return wsOperation;
  }
  
  protected void importTypes(Types types) {
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
  
  protected void importStructure(Mapping mapping) {
    QName qname = mapping.getElement();
    final JType type = mapping.getType().getTypeClass();
    if (type.isPrimitive()) {
        final Class<?> primitiveClass = ReflectUtil.loadClass(type.boxify().fullName());
        final StructureDefinition structure = new PrimitiveStructureDefinition(this.namespace + qname.getLocalPart(), primitiveClass);
        this.structures.put(structure.getId(), structure);
        
    } else if (type instanceof JDefinedClass) {
        JDefinedClass theClass = (JDefinedClass) type;
        SimpleStructureDefinition structure = new SimpleStructureDefinition(this.namespace + qname.getLocalPart());
        this.structures.put(structure.getId(), structure);
        
        importFields(theClass, structure);
        
    } else {
        final Class<?> referencedClass = ReflectUtil.loadClass(type.fullName());
        final StructureDefinition structure = new PrimitiveStructureDefinition(this.namespace + qname.getLocalPart(), referencedClass);
        this.structures.put(structure.getId(), structure);
    } 
  }
  
  protected static void importFields(final JDefinedClass theClass, final SimpleStructureDefinition structure) {
    final AtomicInteger index = new AtomicInteger(0);
    _importFields(theClass, index, structure);
  }
  
  protected static void _importFields(final JDefinedClass theClass, final AtomicInteger index, final SimpleStructureDefinition structure) {
      
    final JClass parentClass = theClass._extends();
    if (parentClass != null && parentClass instanceof JDefinedClass) {
      _importFields((JDefinedClass)parentClass, index, structure);
    }
    for (Entry<String, JFieldVar> entry : theClass.fields().entrySet()) {
      Class<?> fieldClass = ReflectUtil.loadClass(entry.getValue().type().boxify().erasure().fullName());

      String fieldName = entry.getKey();
      if (fieldName.startsWith("_")) {
        if (!JJavaName.isJavaIdentifier(fieldName.substring(1))) {
          fieldName = fieldName.substring(1); //it was prefixed with '_' so we should use the original name.
        }
      }

      structure.setFieldName(index.getAndIncrement(), fieldName, fieldClass);
    }
  }
  
  protected S2JJAXBModel compileModel(Types types, SchemaCompiler compiler, org.w3c.dom.Element rootTypes) {
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
